package com.adamo.vrspfab.reservations;

import com.adamo.vrspfab.common.ResourceNotFoundException;
import com.adamo.vrspfab.slots.SlotConflictException;
import com.adamo.vrspfab.users.UserMapper;
import com.adamo.vrspfab.vehicles.VehicleMapper;
import com.adamo.vrspfab.vehicles.VehicleService;
import com.adamo.vrspfab.users.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public ReservationDto createReservation(ReservationDto reservationDto) {
        var user = userService.getUser(reservationDto.getUserId());
        var vehicle = vehicleService.getVehicleById(reservationDto.getVehicleId());

        // Simplified slot conflict check (expand with Slot entity logic if needed)
        if (reservationRepository.findOverlappingReservations(
                vehicle.getId(),
                reservationDto.getStartDate(),
                reservationDto.getEndDate()
        ).isPresent()) {
            throw new SlotConflictException("Slot is already booked for the selected dates");
        }

        Reservation reservation = reservationMapper.toEntity(reservationDto);
        reservation.setUser(userMapper.toEntity(user));
        reservation.setVehicle(vehicleMapper.toEntity(vehicle));
        reservation.setStatus(ReservationStatus.PENDING); // Default status
        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        return reservationMapper.toDto(reservation);
    }

    @Transactional
    public ReservationDto confirmReservation(Long id) {
        Reservation reservation = reservationRepository
                .findWithDetailsById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reservation not found")
                )
        ;

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidStateException("Reservation cannot be confirmed in current state");
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationDto cancelReservation(Long id) {
        Reservation reservation = reservationRepository
                .findWithDetailsById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Reservation not found")
                )
        ;

        // Check if the reservation can be cancelled based on its status
        if (
                reservation.getStatus() != ReservationStatus.PENDING &&
                reservation.getStatus() != ReservationStatus.CONFIRMED
        ) {
            throw new InvalidStateException("Reservation cannot be cancelled in current state");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationMapper.toDto(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        reservationRepository.delete(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getAllReservations(int page, int size, String status, Long userId) {
        List<Reservation> reservations = reservationRepository.findAllWithUserAndVehicle();
        if (status != null) {
            reservations = reservationRepository.findByStatus(status);
        }
        if (userId != null) {
            reservations = reservationRepository.findByUserId(userId);
        }
        return reservations.stream()
                .skip((long) page * size)
                .limit(size)
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }
}