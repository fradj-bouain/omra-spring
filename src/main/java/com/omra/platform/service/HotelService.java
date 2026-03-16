package com.omra.platform.service;

import com.omra.platform.dto.HotelDto;
import com.omra.platform.dto.GroupHotelDto;
import com.omra.platform.entity.GroupHotel;
import com.omra.platform.entity.Hotel;
import com.omra.platform.exception.ForbiddenException;
import com.omra.platform.exception.ResourceNotFoundException;
import com.omra.platform.repository.GroupHotelRepository;
import com.omra.platform.repository.HotelRepository;
import com.omra.platform.repository.UmrahGroupRepository;
import com.omra.platform.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final GroupHotelRepository groupHotelRepository;
    private final UmrahGroupRepository umrahGroupRepository;

    private Long requireAgencyId() {
        Long agencyId = TenantContext.getAgencyId();
        if (agencyId == null && !TenantContext.isSuperAdmin()) throw new ForbiddenException("Agency context required");
        return agencyId;
    }

    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        Long agencyId = TenantContext.getAgencyId();
        if (TenantContext.isSuperAdmin() && agencyId == null) {
            return hotelRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
        }
        if (agencyId == null) throw new ForbiddenException("Agency context required");
        return hotelRepository.findByAgencyId(agencyId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(hotel.getAgencyId()))) {
            throw new ForbiddenException("Access denied to this hotel");
        }
        return toDto(hotel);
    }

    @Transactional
    public HotelDto createHotel(HotelDto dto) {
        Long agencyId = requireAgencyId();
        Hotel hotel = Hotel.builder()
                .agencyId(agencyId)
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .country(dto.getCountry())
                .stars(dto.getStars())
                .contactImportant(dto.getContactImportant())
                .contactPhone(dto.getContactPhone())
                .receptionPhone(dto.getReceptionPhone())
                .email(dto.getEmail())
                .build();
        hotel = hotelRepository.save(hotel);
        return toDto(hotel);
    }

    @Transactional(readOnly = true)
    public List<GroupHotelDto> getHotelsByGroup(Long groupId) {
        ensureGroupAccess(groupId);
        return groupHotelRepository.findByGroupId(groupId).stream()
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
                            .hotel(h != null ? toDto(h) : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupHotelDto assignHotelToGroup(GroupHotelDto dto) {
        ensureGroupAccess(dto.getGroupId());
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", dto.getHotelId()));
        var group = umrahGroupRepository.findById(dto.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", dto.getGroupId()));
        if (hotel.getAgencyId() != null && !hotel.getAgencyId().equals(group.getAgencyId())) {
            throw new ForbiddenException("Hotel and group must belong to the same agency");
        }
        GroupHotel gh = GroupHotel.builder()
                .groupId(dto.getGroupId())
                .hotelId(dto.getHotelId())
                .city(dto.getCity())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .roomType(dto.getRoomType())
                .build();
        gh = groupHotelRepository.save(gh);
        return GroupHotelDto.builder()
                .id(gh.getId())
                .groupId(gh.getGroupId())
                .hotelId(gh.getHotelId())
                .city(gh.getCity())
                .checkIn(gh.getCheckIn())
                .checkOut(gh.getCheckOut())
                .roomType(gh.getRoomType())
                .build();
    }

    private void ensureGroupAccess(Long groupId) {
        requireAgencyId();
        var group = umrahGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        Long agencyId = TenantContext.getAgencyId();
        if (!TenantContext.isSuperAdmin() && (agencyId == null || !agencyId.equals(group.getAgencyId()))) {
            throw new ForbiddenException("Access denied to this group");
        }
    }

    private HotelDto toDto(Hotel e) {
        return HotelDto.builder()
                .id(e.getId())
                .name(e.getName())
                .city(e.getCity())
                .address(e.getAddress())
                .country(e.getCountry())
                .stars(e.getStars())
                .contactImportant(e.getContactImportant())
                .contactPhone(e.getContactPhone())
                .receptionPhone(e.getReceptionPhone())
                .email(e.getEmail())
                .build();
    }
}
