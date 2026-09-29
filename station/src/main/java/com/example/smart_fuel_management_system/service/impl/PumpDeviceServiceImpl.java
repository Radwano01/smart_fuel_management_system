package com.example.smart_fuel_management_system.service.impl;


import com.example.smart_fuel_management_system.dto.pump.PumpDeviceResponse;
import com.example.smart_fuel_management_system.entity.Pump;
import com.example.smart_fuel_management_system.entity.PumpDevice;
import com.example.smart_fuel_management_system.enums.DeviceStatusType;
import com.example.smart_fuel_management_system.repository.PumpDeviceRepository;
import com.example.smart_fuel_management_system.repository.PumpRepository;
import com.example.smart_fuel_management_system.service.PumpDeviceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PumpDeviceServiceImpl implements PumpDeviceService {

    private final PumpDeviceRepository pumpDeviceRepository;
    private final PumpRepository pumpRepository;

    @Override
    @Transactional
        public UUID registerDevice(String deviceId) {
                if (deviceId == null || deviceId.isBlank()) {
                        throw new IllegalArgumentException("Device ID is required");
                }

                String normalizedDeviceId = deviceId.trim();

        PumpDevice device = pumpDeviceRepository
                                .findByDeviceId(normalizedDeviceId)
                .orElseGet(() -> {

                    PumpDevice newDevice = PumpDevice.builder()
                            .deviceId(normalizedDeviceId)
                            .status(DeviceStatusType.UNASSIGNED)
                            .build();

                    return pumpDeviceRepository.save(newDevice);
                });

        return toResponse(device).pumpId();
    }

    @Override
    public PumpDeviceResponse getByDeviceId(String deviceId) {

        PumpDevice device = pumpDeviceRepository
                .findByDeviceId(deviceId.trim())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Pump device not found: " + deviceId
                        )
                );

        return toResponse(device);
    }

    @Override
    @Transactional
    public PumpDeviceResponse assignToPump(
            UUID pumpId,
            String deviceId
    ) {

        Pump pump = pumpRepository
                .findById(pumpId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Pump not found: " + pumpId
                        )
                );

        PumpDevice device = pumpDeviceRepository
                .findByDeviceId(deviceId.trim())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Device not found: " + deviceId
                        )
                );

        if (device.getPump() != null) {
            throw new IllegalStateException(
                    "Device is already assigned to another pump"
            );
        }

        if (pumpDeviceRepository.existsByPump_Id(pumpId)) {
            throw new IllegalStateException(
                    "Pump already has an assigned device"
            );
        }

        device.setPump(pump);
        device.setStatus(DeviceStatusType.ASSIGNED);

        PumpDevice savedDevice = pumpDeviceRepository.save(device);

        return toResponse(savedDevice);
    }

    @Override
    @Transactional
    public void unassignFromPump(UUID pumpId) {

        PumpDevice device = pumpDeviceRepository
                .findByPump_Id(pumpId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No device assigned to pump: " + pumpId
                        )
                );

        device.setPump(null);
        device.setStatus(DeviceStatusType.UNASSIGNED);

        pumpDeviceRepository.save(device);
    }

    private PumpDeviceResponse toResponse(PumpDevice device) {

        UUID pumpId = device.getPump() != null
                ? device.getPump().getId()
                : null;

        return new PumpDeviceResponse(
                device.getId(),
                device.getDeviceId(),
                device.getStatus(),
                pumpId
        );
    }
}