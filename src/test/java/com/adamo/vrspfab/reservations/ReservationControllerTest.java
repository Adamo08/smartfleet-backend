package com.adamo.vrspfab.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.adamo.vrspfab.auth.JwtAuthenticationFilter.class)
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @Test
    void createReservation_whenValidRequest_returnsCreated() throws Exception {
        // Arrange
        CreateReservationRequest request = new CreateReservationRequest();
        request.setVehicleId(1L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(2));

        DetailedReservationDto responseDto = new DetailedReservationDto();
        responseDto.setId(1L);
        responseDto.setStatus(ReservationStatus.PENDING);

        given(reservationService.createReservation(any(CreateReservationRequest.class)))
                .willReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReservation_whenInvalidRequest_returnsBadRequest() throws Exception {
        // Arrange - missing required fields
        CreateReservationRequest request = new CreateReservationRequest();
        // vehicleId is null

        // Act & Assert
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReservationsForCurrentUser_returnsPaginatedResults() throws Exception {
        // Arrange
        ReservationSummaryDto summaryDto = new ReservationSummaryDto();
        summaryDto.setId(1L);
        summaryDto.setStatus(ReservationStatus.PENDING);

        Page<ReservationSummaryDto> page = new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1);
        given(reservationService.getReservationsForCurrentUser(any()))
                .willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/reservations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getReservationById_whenExists_returnsReservation() throws Exception {
        // Arrange
        DetailedReservationDto dto = new DetailedReservationDto();
        dto.setId(1L);
        dto.setStatus(ReservationStatus.CONFIRMED);

        given(reservationService.getReservationByIdForCurrentUser(1L))
                .willReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelReservation_whenValid_returnsUpdatedReservation() throws Exception {
        // Arrange
        DetailedReservationDto cancelledDto = new DetailedReservationDto();
        cancelledDto.setId(1L);
        cancelledDto.setStatus(ReservationStatus.CANCELLED);

        given(reservationService.cancelReservation(1L))
                .willReturn(cancelledDto);

        // Act & Assert
        mockMvc.perform(post("/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getFilteredReservations_returnsFilteredResults() throws Exception {
        // Arrange
        ReservationSummaryDto summaryDto = new ReservationSummaryDto();
        summaryDto.setId(1L);
        summaryDto.setStatus(ReservationStatus.PENDING);

        Page<ReservationSummaryDto> page = new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1);
        given(reservationService.getUserReservationsWithFilter(any(), any()))
                .willReturn(page);

        // Act & Assert
        mockMvc.perform(get("/reservations/filtered")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }
}

