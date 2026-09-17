package com.soufiane.bookingservice.controller;

import com.soufiane.bookingservice.dto.BookingResponseDTO;
import com.soufiane.bookingservice.model.BookingStatus;
import com.soufiane.bookingservice.service.BookingService;

import org.junit.jupiter.api.Test;
import com.soufiane.bookingservice.security.JwtAuthFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;


    @Test
    void createBooking_shouldReturn201() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.bookTicket(any(), eq(2L), eq("fake-token"))).thenReturn(response);

        Authentication authentication =
                new TestingAuthenticationToken
                        (2L, null, "ROLE_USER");

        mockMvc.perform(
                        post("/api/bookings")
                                .principal(authentication)
                                .header("Authorization", "Bearer fake-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "eventId": 1,
                                            "seats": 2
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.eventId").value(1L))
                .andExpect(jsonPath("$.seatsBooked").value(2))
                .andExpect(jsonPath("$.totalPrice").value(300.00))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.bookingReference").value("BK-12345678"));


        verify(bookingService).bookTicket(any(), eq(2L), eq("fake-token"));
    }


    @Test
    void getMyBookings_shouldReturn2O0() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.getMyBookings(2L))
                .thenReturn(List.of(response));

        Authentication authentication =
                new TestingAuthenticationToken(2L, null, "ROLE_USER");


        mockMvc.perform(
                        get("/api/bookings/me")
                                .principal(authentication)
                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L))
                .andExpect(jsonPath("$[0].eventId").value(1L))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(bookingService).getMyBookings(2L);
    }

    @Test
    void getBookingById_shouldReturn200() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.getBookingById(1L, 2L, "USER"))
                .thenReturn(response);

        Authentication authentication =
                new TestingAuthenticationToken(2L, null, "ROLE_USER");

        mockMvc.perform(
                        get("/api/bookings/1")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(jsonPath("$.eventId").value(1L));

        verify(bookingService).getBookingById(1L, 2L, "USER");
    }

    @Test
    void cancelBooking_shouldReturn204() throws Exception {

        Authentication authentication =
                new TestingAuthenticationToken(2L, null, "ROLE_USER");

        mockMvc.perform(
                        delete("/api/bookings/1")
                                .principal(authentication)
                                .header(
                                        "Authorization",
                                        "Bearer fake-token"
                                )
                        )
                .andExpect(status().isNoContent());
        verify(bookingService).cancelBooking(1L, 2L, "fake-token");

    }

    @Test
    void getAllBookings_shouldReturn200() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .userId(2L)
                .eventId(1L)
                .seatsBooked(2)
                .totalPrice(new BigDecimal("300.00"))
                .status(BookingStatus.CONFIRMED)
                .bookingReference("BK-12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(bookingService.getAllBookings())
                .thenReturn(List.of(response));

       Authentication authentication =
                new TestingAuthenticationToken(99L, null, "ROLE_ADMIN");

        mockMvc.perform(
                        get("/api/bookings")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(2L));
        verify(bookingService).getAllBookings();
    }
}

