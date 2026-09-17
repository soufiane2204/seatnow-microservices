package com.soufiane.eventservice.repository;

import com.soufiane.eventservice.model.Event;
import com.soufiane.eventservice.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusAndEventDateAfter(
            EventStatus status,
            LocalDateTime date
    );

    List<Event> findByOrganizerId(Long organizerId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Event e
            SET e.availableSeats = e.availableSeats - :seats
            WHERE e.id = :eventId
            AND e.availableSeats >= :seats
            """)
    int reserveSeats(
            @Param("eventId") Long eventId,
            @Param("seats") int seats
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Event e
            SET e.availableSeats = e.availableSeats + :seats
            WHERE e.id = :eventId
            """)
    int releaseSeats(
            @Param("eventId") Long eventId,
            @Param("seats") int seats
    );
}