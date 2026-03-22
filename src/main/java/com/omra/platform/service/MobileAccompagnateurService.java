package com.omra.platform.service;

import com.omra.platform.dto.AgencyDto;
import com.omra.platform.dto.PlanningDto;
import com.omra.platform.dto.UserDto;
import com.omra.platform.dto.mobile.*;
import com.omra.platform.entity.GroupCompanion;
import com.omra.platform.entity.GroupPilgrim;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.entity.User;
import com.omra.platform.entity.enums.TripType;
import com.omra.platform.entity.enums.UserRole;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.mapper.AgencyMapper;
import com.omra.platform.repository.AgencyRepository;
import com.omra.platform.repository.GroupCompanionRepository;
import com.omra.platform.repository.GroupPilgrimRepository;
import com.omra.platform.repository.PilgrimRepository;
import com.omra.platform.entity.Notification;
import com.omra.platform.repository.NotificationRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.repository.UserRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileAccompagnateurService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    private final GroupCompanionRepository groupCompanionRepository;
    private final UmrahGroupRepository umrahGroupRepository;
    private final GroupPilgrimRepository groupPilgrimRepository;
    private final PilgrimRepository pilgrimRepository;
    private final PlanningService planningService;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    private void requireCompanionRole() {
        if (TenantContext.getUserId() == null) {
            throw new ForbiddenException("Not authenticated");
        }
        if (TenantContext.getUserRole() != UserRole.PILGRIM_COMPANION) {
            throw new ForbiddenException("Accès réservé aux accompagnateurs de pèlerinage");
        }
    }

    private User requireCompanionUser() {
        requireCompanionRole();
        Long userId = TenantContext.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getDeletedAt() != null) throw new ResourceNotFoundException("User", userId);
        if (user.getRole() != UserRole.PILGRIM_COMPANION) {
            throw new ForbiddenException("Accès réservé aux accompagnateurs de pèlerinage");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public MobileAccompagnateurProfileDto getMe() {
        return buildProfile(requireCompanionUser());
    }

    @Transactional(readOnly = true)
    public MobileAccompagnateurProfileDto getProfile() {
        return buildProfile(requireCompanionUser());
    }

    private MobileAccompagnateurProfileDto buildProfile(User user) {
        AgencyDto agencyDto = null;
        if (user.getAgencyId() != null) {
            agencyDto = agencyRepository.findById(user.getAgencyId())
                    .map(agencyMapper::toDto)
                    .orElse(null);
        }
        return MobileAccompagnateurProfileDto.builder()
                .id(user.getId())
                .agencyId(user.getAgencyId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .avatar(user.getAvatar())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .agency(agencyDto)
                .build();
    }

    @Transactional
    public MobileAccompagnateurProfileDto updateProfile(MobileAccompagnateurProfileUpdateDto dto) {
        User user = requireCompanionUser();
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone().isBlank() ? null : dto.getPhone().trim());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar().isBlank() ? null : dto.getAvatar().trim());
        }
        user = userRepository.save(user);
        return buildProfile(user);
    }

    @Transactional(readOnly = true)
    public List<MobileAccompagnateurGroupSummaryDto> listMyGroups() {
        User user = requireCompanionUser();
        Long agencyId = user.getAgencyId();
        if (agencyId == null) {
            throw new ForbiddenException("Aucune agence associée");
        }
        List<Long> groupIds = groupCompanionRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .map(GroupCompanion::getGroupId)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return umrahGroupRepository.findAllById(groupIds).stream()
                .filter(g -> g.getDeletedAt() == null && agencyId.equals(g.getAgencyId()))
                .sorted(Comparator.comparing(UmrahGroup::getDepartureDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(UmrahGroup::getId))
                .map(this::toGroupSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MobileAccompagnateurGroupDetailDto getGroupDetail(Long groupId, boolean includePilgrims, boolean includePlanning) {
        User user = requireCompanionUser();
        Long agencyId = user.getAgencyId();
        if (agencyId == null) throw new ForbiddenException("Aucune agence associée");

        boolean assigned = groupCompanionRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .anyMatch(gc -> gc.getGroupId().equals(groupId));
        if (!assigned) {
            throw new ForbiddenException("Vous n'êtes pas affecté à ce groupe");
        }

        UmrahGroup g = umrahGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        if (g.getDeletedAt() != null) throw new ResourceNotFoundException("Group", groupId);
        if (!agencyId.equals(g.getAgencyId())) throw new ForbiddenException("Access denied");

        MobileAccompagnateurGroupSummaryDto summary = toGroupSummary(g);

        MobileAccompagnateurGroupDetailDto.MobileAccompagnateurGroupDetailDtoBuilder builder = MobileAccompagnateurGroupDetailDto.builder()
                .summary(summary)
                .description(g.getDescription())
                .planningId(g.getPlanningId());

        if (includePlanning && g.getPlanningId() != null) {
            PlanningDto program = planningService.getByIdForAgency(g.getPlanningId(), agencyId);
            builder.program(program);
        }

        if (includePilgrims) {
            List<MobilePilgrimBriefDto> pilgrims = groupPilgrimRepository.findByGroupId(groupId).stream()
                    .map(GroupPilgrim::getPilgrimId)
                    .map(pid -> pilgrimRepository.findById(pid).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(p -> p.getDeletedAt() == null)
                    .map(this::toPilgrimBrief)
                    .collect(Collectors.toList());
            builder.pilgrims(pilgrims);
        }

        return builder.build();
    }

    private MobileAccompagnateurGroupSummaryDto toGroupSummary(UmrahGroup g) {
        long count = groupPilgrimRepository.countByGroupId(g.getId());
        TripType tt = g.getTripType() != null ? g.getTripType() : TripType.OMRRA;
        return MobileAccompagnateurGroupSummaryDto.builder()
                .id(g.getId())
                .name(g.getName())
                .tripType(tt)
                .departureDate(g.getDepartureDate())
                .returnDate(g.getReturnDate())
                .maxCapacity(g.getMaxCapacity())
                .price(g.getPrice())
                .status(g.getStatus())
                .pilgrimsCount(count)
                .build();
    }

    private MobilePilgrimBriefDto toPilgrimBrief(Pilgrim p) {
        return MobilePilgrimBriefDto.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .phone(p.getPhone())
                .build();
    }

    @Transactional(readOnly = true)
    public MobileApiResponse<List<MobileNotificationItemDto>> listNotifications(int page, int size) {
        User user = requireCompanionUser();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.min(100, Math.max(1, size)));
        Page<Notification> raw = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        List<MobileNotificationItemDto> items = raw.getContent().stream()
                .map(n -> MobileNotificationItemDto.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .read(n.getRead())
                        .createdAt(n.getCreatedAt())
                        .entityType(n.getEntityType())
                        .entityId(n.getEntityId())
                        .build())
                .collect(Collectors.toList());
        return MobileApiResponse.<List<MobileNotificationItemDto>>builder()
                .success(true)
                .data(items)
                .message(null)
                .build();
    }

    @Transactional
    public void markNotificationRead(Long notificationId) {
        requireCompanionUser();
        notificationService.markAsRead(notificationId);
    }

    /** Build login payload for mobile (tokens + user + agency). */
    public MobileAccompagnateurLoginDataDto toLoginData(com.omra.platform.dto.AuthResponse auth) {
        UserDto u = auth.getUser();
        AgencyDto a = auth.getAgency();
        return MobileAccompagnateurLoginDataDto.builder()
                .accessToken(auth.getAccessToken())
                .refreshToken(auth.getRefreshToken())
                .tokenType("Bearer")
                .user(u)
                .agency(a)
                .build();
    }
}
