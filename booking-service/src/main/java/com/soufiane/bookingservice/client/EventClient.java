package com.soufiane.bookingservice.client;

import com.soufiane.bookingservice.dto.EventResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EventClient {

    private final RestClient restClient;

    public EventClient(
            @Value("${event.service.url}") String eventServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(eventServiceUrl)
                .build();
    }

    public EventResponseDTO getEventById(Long eventId) {

        return restClient.get()
                .uri("/api/events/{id}", eventId)
                .retrieve()
                .body(EventResponseDTO.class);
    }

    public boolean reserveSeats(Long eventId, int seats, String token) {

        Integer result = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/events/{id}/reserve")
                        .queryParam("seats", seats)
                        .build(eventId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Integer.class);

        return result != null && result > 0;
    }

    public void releaseSeats(Long eventId, int seats, String token) {

        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/events/{id}/release")
                        .queryParam("seats", seats)
                        .build(eventId))
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }
}