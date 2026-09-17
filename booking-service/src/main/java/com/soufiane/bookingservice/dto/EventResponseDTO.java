package com.soufiane.bookingservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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