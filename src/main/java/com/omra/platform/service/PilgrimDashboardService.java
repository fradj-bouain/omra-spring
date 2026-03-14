package com.omra.platform.service;

import com.omra.platform.dto.*;
import com.omra.platform.entity.GroupPilgrim;
import com.omra.platform.entity.Hotel;
import com.omra.platform.entity.Pilgrim;
import com.omra.platform.entity.UmrahGroup;
import com.omra.platform.entity.User;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.*;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilgrimDashboardService {

    private final UserRepository userRepository;
    private final PilgrimRepository pilgrimRepository;
    private final GroupPilgrimRepository groupPilgrimRepository;
    private final UmrahGroupRepository umrahGroupRepository;
    private final FlightRepository flightRepository;
    private final GroupHotelRepository groupHotelRepository;
    private final HotelRepository hotelRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;
    private final PilgrimService pilgrimService;

    @Transactional(readOnly = true)
    public PilgrimDashboardDto getDashboard() {
        if (TenantContext.getUserRole() != com.omra.platform.entity.enums.UserRole.PILGRIM) {
            throw new ForbiddenException("Only pilgrims can access this dashboard");
        }
        Long userId = TenantContext.getUserId();
        if (userId == null) throw new ForbiddenException("Not authenticated");

        // Resolve pilgrim linked to this user (e.g. User with role PILGRIM might have pilgrimId or same email)
        // Spec says "Un pèlerin peut consulter ses informations" - we assume current user id might be used to find pilgrim
        // Option: User table could have pilgrim_id for PILGRIM role, or we find by email. For simplicity we use userId
        // as the pilgrim id if the app links user to pilgrim 1:1. Otherwise we need a User.pilgrimId.
        // Cahier says GET /api/pilgrim/dashboard - so the authenticated user is a pilgrim. We need to find Pilgrim by user.
        // Add optional pilgrim_id to User for PILGRIM role. For now: find first pilgrim with same agency as user and assume
        // one pilgrim per user - or we need User.pilgrimId. Let's add pilgrimId to User entity for PILGRIM role.
        Pilgrim pilgrim = findPilgrimForCurrentUser(userId);
        if (pilgrim == null) {
            return PilgrimDashboardDto.builder()
                    .pilgrim(null)
                    .group(null)
                    .flights(List.of())
                    .hotels(List.of())
                    .documents(List.of())
                    .notifications(List.of())
                    .build();
        }

        PilgrimDto pilgrimDto = pilgrimService.getById(pilgrim.getId());

        UmrahGroupDto groupDto = null;
        List<FlightDto> flights = new ArrayList<>();
        List<GroupHotelDto> hotels = new ArrayList<>();

        Long groupId = groupPilgrimRepository.findByPilgrimId(pilgrim.getId()).stream()
                .map(GroupPilgrim::getGroupId)
                .findFirst()
                .orElse(null);

        if (groupId != null) {
            groupDto = pilgrim.getAgencyId() != null ? null : null;
            var groupOpt = umrahGroupRepository.findById(groupId);
            if (groupOpt.isPresent()) {
                UmrahGroup g = groupOpt.get();
                groupDto = UmrahGroupDto.builder()
                        .id(g.getId())
                        .agencyId(g.getAgencyId())
                        .name(g.getName())
                        .description(g.getDescription())
                        .departureDate(g.getDepartureDate())
                        .returnDate(g.getReturnDate())
                        .maxCapacity(g.getMaxCapacity())
                        .price(g.getPrice())
                        .status(g.getStatus())
                        .createdAt(g.getCreatedAt())
                        .build();
            }
            flights = flightRepository.findByGroupIdAndDeletedAtIsNull(groupId).stream()
                    .map(f -> FlightDto.builder()
                            .id(f.getId())
                            .agencyId(f.getAgencyId())
                            .groupId(f.getGroupId())
                            .airline(f.getAirline())
                            .flightNumber(f.getFlightNumber())
                            .departureCity(f.getDepartureCity())
                            .arrivalCity(f.getArrivalCity())
                            .departureTime(f.getDepartureTime())
                            .arrivalTime(f.getArrivalTime())
                            .terminal(f.getTerminal())
                            .gate(f.getGate())
                            .createdAt(f.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
            hotels = groupHotelRepository.findByGroupId(groupId).stream()
                    .map(gh -> {
                        Hotel h = hotelRepository.findById(gh.getHotelId()).orElse(null);
                        return GroupHotelDto.builder()
                                .id(gh.getId())
                                .groupId(gh.getGroupId())
                                .hotelId(gh.getHotelId())
                                .city(gh.getCity())
                                .checkIn(gh.getCheckIn())
                                .checkOut(gh.getCheckOut())
                                .roomType(gh.getRoomType())
                                .hotel(h != null ? HotelDto.builder().id(h.getId()).name(h.getName()).city(h.getCity()).address(h.getAddress()).stars(h.getStars()).contactPhone(h.getContactPhone()).build() : null)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        List<DocumentDto> documents = documentRepository.findByPilgrimIdAndDeletedAtIsNull(pilgrim.getId()).stream()
                .map(d -> DocumentDto.builder()
                        .id(d.getId())
                        .agencyId(d.getAgencyId())
                        .pilgrimId(d.getPilgrimId())
                        .groupId(d.getGroupId())
                        .type(d.getType())
                        .fileUrl(d.getFileUrl())
                        .status(d.getStatus())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<NotificationDto> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20)).getContent().stream()
                .map(n -> NotificationDto.builder()
                        .id(n.getId())
                        .userId(n.getUserId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .channel(n.getChannel())
                        .read(n.getRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PilgrimDashboardDto.builder()
                .pilgrim(pilgrimDto)
                .group(groupDto)
                .flights(flights)
                .hotels(hotels)
                .documents(documents)
                .notifications(notifications)
                .build();
    }

    private Pilgrim findPilgrimForCurrentUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (user.getPilgrimId() != null) {
            return pilgrimRepository.findById(user.getPilgrimId()).orElse(null);
        }
        return pilgrimRepository.findByAgencyIdAndDeletedAtIsNull(user.getAgencyId(), PageRequest.of(0, 1))
                .getContent().stream()
                .filter(p -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(p.getEmail()))
                .findFirst()
                .orElse(null);
    }
}
