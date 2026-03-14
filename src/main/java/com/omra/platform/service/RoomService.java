package com.omra.platform.service;

import com.omra.platform.dto.GroupRoomAssignmentDto;
import com.omra.platform.dto.PageResponse;
import com.omra.platform.dto.RoomDto;
import com.omra.platform.entity.GroupHotel;
import com.omra.platform.entity.GroupRoomAssignment;
import com.omra.platform.entity.Hotel;
import com.omra.platform.entity.Room;
import com.omra.platform.exception.BadRequestException;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.GroupHotelRepository;
import com.omra.platform.repository.GroupRoomAssignmentRepository;
import com.omra.platform.repository.HotelRepository;
import com.omra.platform.repository.RoomRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final GroupHotelRepository groupHotelRepository;
    private final GroupRoomAssignmentRepository groupRoomAssignmentRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    private void ensureHotelAccess(Long hotelId) {
        Hotel h = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(h.getAgencyId())))
            throw new ForbiddenException("Access denied to this hotel");
    }

    @Transactional(readOnly = true)
    public PageResponse<RoomDto> getByHotel(Long hotelId, Pageable pageable) {
        ensureHotelAccess(hotelId);
        Page<Room> page = roomRepository.findByHotelIdAndDeletedAtIsNull(hotelId, pageable);
        List<RoomDto> content = page.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return PageResponse.<RoomDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public RoomDto getById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
        ensureHotelAccess(room.getHotelId());
        if (room.getDeletedAt() != null) throw new ResourceNotFoundException("Room", id);
        return toDto(room);
    }

    @Transactional
    public RoomDto create(RoomDto dto) {
        if (dto.getHotelId() == null) throw new BadRequestException("Hotel ID required");
        ensureHotelAccess(dto.getHotelId());
        if (roomRepository.existsByHotelIdAndRoomNumberAndDeletedAtIsNull(dto.getHotelId(), dto.getRoomNumber())) {
            throw new BadRequestException("Une chambre avec ce numéro existe déjà pour cet hôtel.");
        }
        Room room = Room.builder()
                .hotelId(dto.getHotelId())
                .roomNumber(dto.getRoomNumber())
                .capacity(dto.getCapacity() != null ? dto.getCapacity() : 2)
                .roomType(dto.getRoomType())
                .build();
        room = roomRepository.save(room);
        return toDto(room);
    }

    @Transactional
    public RoomDto update(Long id, RoomDto dto) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
        ensureHotelAccess(room.getHotelId());
        if (room.getDeletedAt() != null) throw new ResourceNotFoundException("Room", id);
        if (dto.getRoomNumber() != null) room.setRoomNumber(dto.getRoomNumber());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getRoomType() != null) room.setRoomType(dto.getRoomType());
        room = roomRepository.save(room);
        return toDto(room);
    }

    @Transactional
    public void delete(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Room", id));
        ensureHotelAccess(room.getHotelId());
        room.setDeletedAt(Instant.now());
        roomRepository.save(room);
    }

    @Transactional
    public GroupRoomAssignmentDto assignPilgrimToRoom(Long groupHotelId, Long roomId, Long pilgrimId) {
        Long agencyId = requireAgencyId();
        GroupHotel gh = groupHotelRepository.findById(groupHotelId).orElseThrow(() -> new ResourceNotFoundException("GroupHotel", groupHotelId));
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !gh.getGroup().getAgencyId().equals(agencyId)))
            throw new ForbiddenException("Access denied");
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        if (!room.getHotelId().equals(gh.getHotelId()))
            throw new BadRequestException("Room does not belong to this group hotel's hotel.");
        if (groupRoomAssignmentRepository.existsByGroupHotelIdAndPilgrimId(groupHotelId, pilgrimId))
            throw new BadRequestException("Pilgrim already assigned to a room for this group hotel.");
        GroupRoomAssignment a = GroupRoomAssignment.builder()
                .groupHotelId(groupHotelId)
                .roomId(roomId)
                .pilgrimId(pilgrimId)
                .build();
        a = groupRoomAssignmentRepository.save(a);
        return toAssignmentDto(a);
    }

    @Transactional(readOnly = true)
    public List<GroupRoomAssignmentDto> getAssignmentsByGroupHotel(Long groupHotelId) {
        ensureGroupHotelAccess(groupHotelId);
        return groupRoomAssignmentRepository.findByGroupHotelId(groupHotelId).stream().map(this::toAssignmentDto).collect(Collectors.toList());
    }

    private void ensureGroupHotelAccess(Long groupHotelId) {
        GroupHotel gh = groupHotelRepository.findById(groupHotelId).orElseThrow(() -> new ResourceNotFoundException("GroupHotel", groupHotelId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !gh.getGroup().getAgencyId().equals(agencyId)))
            throw new ForbiddenException("Access denied");
    }

    private RoomDto toDto(Room e) {
        return RoomDto.builder()
                .id(e.getId())
                .hotelId(e.getHotelId())
                .roomNumber(e.getRoomNumber())
                .capacity(e.getCapacity())
                .roomType(e.getRoomType())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private GroupRoomAssignmentDto toAssignmentDto(GroupRoomAssignment e) {
        return GroupRoomAssignmentDto.builder()
                .id(e.getId())
                .groupHotelId(e.getGroupHotelId())
                .roomId(e.getRoomId())
                .pilgrimId(e.getPilgrimId())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
