package com.soufiane.bookingservice.integration;

import com.soufiane.bookingservice.client.EventClient;
import com.soufiane.bookingservice.dto.EventResponseDTO;
import com.soufiane.bookingservice.model.Booking;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.repository.BookingRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class BookingApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookingRepository bookingRepository;
    @MockitoBean
    private EventClient eventClient;

    @Test
    void createBooking_shouldCreateBookingAndSaveItInDatabase() throws Exception {

        EventResponseDTO event = new EventResponseDTO();
        event.setId(1L);
        event.setStatus("PUBLISHED");
        event.setPrice(new BigDecimal("150.00"));
        event.setAvailableSeats(100);

        when(eventClient.getEventById(1L)).thenReturn(event);
        when(eventClient.reserveSeats(1L, 2, "fake-token")).thenReturn(true);

        Authentication authentication =
                new TestingAuthenticationToken(50L, null, "ROLE_USER");


        mockMvc.perform(
                        post("/api/bookings")
                                .principal(authentication)
                                .header(
                                        "Authorization",
                                        "Bearer fake-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "eventId": 1,
                                    "seats": 2
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(50))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.seatsBooked").value(2))
                .andExpect(jsonPath("$.totalPrice").value(300.00))
                .andExpect(
                        jsonPath("$.status")
                                .value(BookingStatus.CONFIRMED.name())
                );


        List<Booking> bookings =
                bookingRepository.findByUserId(50L);

        assertFalse(bookings.isEmpty());

        Booking savedBooking = bookings.stream()
                .filter(b ->
                        b.getEventId().equals(1L)
                                && b.getSeatsBooked() == 2
                                && b.getStatus() == BookingStatus.CONFIRMED
                )
                .findFirst()
                .orElseThrow();



        assertEquals(
                new BigDecimal("300.00"),
                savedBooking.getTotalPrice()
        );

        assertNotNull(
                savedBooking.getBookingReference()
        );
        assertNotNull(
                savedBooking.getCreatedAt()
        );

        verify(eventClient).reserveSeats(1L, 2, "fake-token");
    }

}
