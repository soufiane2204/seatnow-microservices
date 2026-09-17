package com.soufiane.eventservice.integration;

import com.soufiane.eventservice.model.Event;
import com.soufiane.eventservice.model.EventStatus;
import com.soufiane.eventservice.repository.EventRepository;
import com.soufiane.eventservice.service.EventService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class EventConcurrencyIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;


    @Test
    void onlyOneReservationShouldSucceed_whenTwoUsersTryLastSeat()
            throws Exception {



        Event event = Event.builder()
                .name("Concurrency Test Event")
                .description("Test last seat")
                .venue("Casablanca")
                .eventDate(LocalDateTime.now().plusDays(10))
                .totalSeats(1)
                .availableSeats(1)
                .price(new BigDecimal("150.00"))
                .status(EventStatus.PUBLISHED)
                .organizerId(1L)
                .build();

        Event savedEvent = eventRepository.saveAndFlush(event);

        Long eventId = savedEvent.getId();


        ExecutorService executor =
                Executors.newFixedThreadPool(2);


        CountDownLatch startLatch =
                new CountDownLatch(1);


        Future<Integer> user1 = executor.submit(() -> {

            startLatch.await();

            return eventService.reserveSeats(
                    eventId,
                    1
            );
        });


        Future<Integer> user2 = executor.submit(() -> {

            startLatch.await();

            return eventService.reserveSeats(
                    eventId,
                    1
            );
        });


        startLatch.countDown();




        int result1 = user1.get();
        int result2 = user2.get();

        executor.shutdown();




        int totalSuccessfulReservations =
                result1 + result2;

        assertEquals(
                1,
                totalSuccessfulReservations
        );


        Event finalEvent =
                eventRepository.findById(eventId)
                        .orElseThrow();

        assertEquals(
                0,
                finalEvent.getAvailableSeats()
        );
    }
}