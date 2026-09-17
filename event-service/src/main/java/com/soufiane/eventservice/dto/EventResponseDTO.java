package com.soufiane.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventDate;
    private int totalSeats;
    private int availableSeats;
    private BigDecimal price;
    private String status;

    private Long organizerId;
}