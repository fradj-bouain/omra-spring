package com.omra.platform.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilgrimDashboardDto {

    private PilgrimDto pilgrim;
    private UmrahGroupDto group;
    private List<FlightDto> flights;
    private List<GroupHotelDto> hotels;
    private List<DocumentDto> documents;
    private List<NotificationDto> notifications;
}
