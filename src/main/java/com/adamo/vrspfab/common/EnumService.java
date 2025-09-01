package com.adamo.vrspfab.common;

import com.adamo.vrspfab.reservations.ReservationStatus;
import com.adamo.vrspfab.users.Role;
import com.adamo.vrspfab.vehicles.FuelType;
import com.adamo.vrspfab.vehicles.VehicleStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnumService {

    public List<EnumValue> getFuelTypes() {
        return Arrays.stream(FuelType.values())
                .map(type -> new EnumValue(type.name(), type.name()))
                .collect(Collectors.toList());
    }

    public List<EnumValue> getVehicleStatuses() {
        return Arrays.stream(VehicleStatus.values())
                .map(status -> new EnumValue(status.name(), status.name()))
                .collect(Collectors.toList());
    }

    public List<EnumValue> getReservationStatuses() {
        return Arrays.stream(ReservationStatus.values())
                .map(status -> new EnumValue(status.name(), status.name()))
                .collect(Collectors.toList());
    }

    public List<EnumValue> getUserRoles() {
        return Arrays.stream(Role.values())
                .map(role -> new EnumValue(role.name(), role.name()))
                .collect(Collectors.toList());
    }

    public static class EnumValue {
        private final String value;
        private final String label;

        public EnumValue(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}
