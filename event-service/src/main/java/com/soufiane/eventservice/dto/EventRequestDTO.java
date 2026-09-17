package com.soufiane.eventservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventRequestDTO {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String venue;

    @NotNull
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;
}