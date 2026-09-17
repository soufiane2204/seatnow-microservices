package com.soufiane.eventservice.service;

import com.soufiane.eventservice.dto.EventRequestDTO;
import com.soufiane.eventservice.dto.EventResponseDTO;
import com.soufiane.eventservice.exception.EventNotAvailableException;
import com.soufiane.eventservice.exception.EventNotFoundException;
import com.soufiane.eventservice.model.Event;
import com.soufiane.eventservice.model.EventStatus;
import com.soufiane.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponseDTO createEvent(EventRequestDTO dto, Long organizerId) {

        Event event = Event.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .venue(dto.getVenue())
                .eventDate(dto.getEventDate())
                .totalSeats(dto.getTotalSeats())
                .availableSeats(dto.getTotalSeats())
                .price(dto.getPrice())
                .status(EventStatus.DRAFT)
                .organizerId(organizerId)
                .build();

        return toResponseDTO(eventRepository.save(event));
    }

    public EventResponseDTO updateEvent(Long id, EventRequestDTO dto) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotAvailableException(
                    "Only DRAFT events can be edited"
            );
        }

        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setVenue(dto.getVenue());
        event.setEventDate(dto.getEventDate());
        event.setTotalSeats(dto.getTotalSeats());
        event.setAvailableSeats(dto.getTotalSeats());
        event.setPrice(dto.getPrice());

        return toResponseDTO(eventRepository.save(event));
    }

    public EventResponseDTO getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));

        return toResponseDTO(event);
    }

    public List<EventResponseDTO> getAllPublishedEvents() {

        return eventRepository
                .findByStatusAndEventDateAfter(
                        EventStatus.PUBLISHED,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EventResponseDTO> getEventByOrganizer(Long organizerId) {

        return eventRepository
                .findByOrganizerId(organizerId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public EventResponseDTO publishEvent(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));

        if (event.getTotalSeats() <= 0) {
            throw new EventNotAvailableException(
                    "Cannot publish an event with 0 seats"
            );
        }

        event.setStatus(EventStatus.PUBLISHED);

        return toResponseDTO(eventRepository.save(event));
    }

    public EventResponseDTO cancelEvent(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));

        event.setStatus(EventStatus.CANCELLED);

        return toResponseDTO(eventRepository.save(event));
    }

    public int reserveSeats(Long eventId, int seats) {
        return eventRepository.reserveSeats(eventId, seats);
    }

    public void releaseSeats(Long eventId, int seats) {
        eventRepository.releaseSeats(eventId, seats);
    }

    public boolean eventExistAndPublished(Long id) {

        return eventRepository.findById(id)
                .map(event ->
                        event.getStatus() == EventStatus.PUBLISHED)
                .orElse(false);
    }

    private EventResponseDTO toResponseDTO(Event event) {

        return EventResponseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .price(event.getPrice())
                .status(event.getStatus().name())
                .organizerId(event.getOrganizerId())
                .build();
    }
}