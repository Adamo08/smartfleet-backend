package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.slots.DynamicSlotService;
import com.adamo.vrspfab.slots.SlotRepository;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.users.User;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.vehicles.mappers.VehicleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private VehicleService vehicleService;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private SecurityUtilsService securityUtils;
    @Mock private NotificationService notificationService;
    @Mock private ReservationMapper reservationMapper;
    @Mock private SlotRepository slotRepository;
    @Mock private DynamicSlotService dynamicSlotService;
    @Mock private SecurityUtilsService securityUtilsService; // for methods using this field
    @Mock private com.adamo.vrspfab.vehicles.VehicleCategoryRepository vehicleCategoryRepository;
    @Mock private com.adamo.vrspfab.vehicles.VehicleBrandRepository vehicleBrandRepository;
    @Mock private com.adamo.vrspfab.vehicles.VehicleModelRepository vehicleModelRepository;
    @Mock private com.adamo.vrspfab.dashboard.ActivityEventListener activityEventListener;

    @InjectMocks private ReservationService reservationService;

    @Test
    void getReservationByIdForCurrentUser_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        User current = User.builder().id(1L).role(Role.CUSTOMER).build();
        User owner = User.builder().id(2L).role(Role.CUSTOMER).build();
        Reservation reservation = Reservation.builder().id(10L).user(owner).build();
        given(securityUtils.getCurrentAuthenticatedUser()).willReturn(current);
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(current);
        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class, () -> reservationService.getReservationByIdForCurrentUser(10L));
    }

    @Test
    void getReservationByIdForCurrentUser_whenOwner_returnsDto() {
        User current = User.builder().id(1L).role(Role.CUSTOMER).build();
        Reservation reservation = Reservation.builder().id(10L).user(current).build();
        given(securityUtils.getCurrentAuthenticatedUser()).willReturn(current);
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(current);
        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(reservationMapper.toDetailedDto(reservation)).willReturn(new DetailedReservationDto());

        DetailedReservationDto dto = reservationService.getReservationByIdForCurrentUser(10L);
        assertNotNull(dto);
    }

    @Test
    void getReservationsForCurrentUser_returnsMappedPage() {
        User current = User.builder().id(1L).role(Role.CUSTOMER).build();
        given(securityUtils.getCurrentAuthenticatedUser()).willReturn(current);
        given(securityUtilsService.getCurrentAuthenticatedUser()).willReturn(current);
        given(reservationRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(Reservation.builder().id(1L).user(current).build())));
        given(reservationMapper.toSummaryDto(any())).willReturn(new ReservationSummaryDto());

        Page<ReservationSummaryDto> page = reservationService.getReservationsForCurrentUser(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }
}
