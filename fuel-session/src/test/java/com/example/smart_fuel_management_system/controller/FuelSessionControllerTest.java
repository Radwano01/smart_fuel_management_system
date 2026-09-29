//package com.example.smart_fuel_management_system.controller;
//
//import com.example.smart_fuel_management_system.dto.*;
//import com.example.smart_fuel_management_system.enums.FuelStatusType;
//import com.example.smart_fuel_management_system.enums.FuelType;
//import com.example.smart_fuel_management_system.service.FuelSessionService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class FuelSessionControllerTest {
//
//    @Mock
//    private FuelSessionService fuelSessionService;
//
//    @InjectMocks
//    private FuelSessionController fuelSessionController;
//
//    @Test
//    void startSession_shouldReturnFuelSessionStartResponse() {
//        StartFuelSessionDTO request = StartFuelSessionDTO.builder()
//                .rfidTag("RFID-123")
//                .plateNumber("12ABC34")
//                .stationId(UUID.randomUUID())
//                .pumpId(UUID.randomUUID())
//                .fuelType(FuelType.REGULAR)
//                .build();
//
//        FuelSessionStartResponse expected = new FuelSessionStartResponse(
//                UUID.randomUUID(),
//                FuelStatusType.STARTED,
//                true
//        );
//
//        when(fuelSessionService.startSession(request)).thenReturn(expected);
//
//        ResponseEntity<FuelSessionStartResponse> response = fuelSessionController.startSession(request);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(expected, response.getBody());
//        verify(fuelSessionService).startSession(request);
//    }
//
//    @Test
//    void stopSession_shouldReturnFuelSessionDTO() {
//        UUID sessionId = UUID.randomUUID();
//        StopFuelSessionDTO request = StopFuelSessionDTO.builder()
//                .liters(BigDecimal.valueOf(30))
//                .pricePerLiter(BigDecimal.valueOf(40))
//                .totalCost(BigDecimal.valueOf(1200))
//                .build();
//
//        FuelSessionDTO expected = FuelSessionDTO.builder()
//                .sessionId(sessionId)
//                .status(FuelStatusType.COMPLETED)
//                .liters(BigDecimal.valueOf(30))
//                .totalCost(BigDecimal.valueOf(1200))
//                .build();
//
//        when(fuelSessionService.stopSession(sessionId, request)).thenReturn(expected);
//
//        ResponseEntity<FuelSessionDTO> response = fuelSessionController.stopSession(sessionId, request);
//
//        assertEquals(200, response.getStatusCode().value());
//        assertEquals(expected, response.getBody());
//        verify(fuelSessionService).stopSession(sessionId, request);
//    }
//}