package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.entity.VehicleSpec;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.repository.VehicleSpecRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleSpecServiceTest {

    @Mock
    private VehicleSpecRepository vehicleSpecRepository;

    @Mock
    private MissingVehicleSpecService missingVehicleSpecService;

    @InjectMocks
    private VehicleSpecService vehicleSpecService;

    private static final BigDecimal DEFAULT_TANK_CAPACITY = BigDecimal.valueOf(50);

    private String brand;
    private String model;
    private int year;
    private FuelType fuelType;

    @BeforeEach
    void setUp() {
        brand = "Toyota";
        model = "Corolla";
        year = 2020;
        fuelType = FuelType.GASOLINE;
    }

    private VehicleSpec buildSpec(BigDecimal tankCapacity) {
        VehicleSpec spec = mock(VehicleSpec.class);
        when(spec.getTankCapacity()).thenReturn(tankCapacity);
        return spec;
    }

    @Test
    void getTankCapacity_shouldReturnSpecTankCapacity_whenSpecExists() {
        // given
        BigDecimal expected = new BigDecimal("47.50");
        VehicleSpec spec = buildSpec(expected);
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.of(spec));

        // when
        BigDecimal result = vehicleSpecService.getTankCapacity(brand, model, year, fuelType);

        // then
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void getTankCapacity_shouldNotCallMissingService_whenSpecExists() {
        // given
        VehicleSpec spec = buildSpec(new BigDecimal("47.50"));
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.of(spec));

        // when
        vehicleSpecService.getTankCapacity(brand, model, year, fuelType);

        // then
        verify(missingVehicleSpecService, never())
                .save(anyString(), anyString(), anyInt(), any());
    }

    @Test
    void getTankCapacity_shouldReturnDefaultCapacity_whenSpecDoesNotExist() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.empty());

        // when
        BigDecimal result = vehicleSpecService.getTankCapacity(brand, model, year, fuelType);

        // then
        assertThat(result).isEqualByComparingTo(DEFAULT_TANK_CAPACITY);
    }

    @Test
    void getTankCapacity_shouldSaveMissingSpec_whenSpecDoesNotExist() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.empty());

        // when
        vehicleSpecService.getTankCapacity(brand, model, year, fuelType);

        // then
        verify(missingVehicleSpecService).save(brand, model, year, fuelType);
    }

    @Test
    void getTankCapacity_shouldReturnDefault_whenMissingServiceThrowsDataIntegrityViolation() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(missingVehicleSpecService)
                .save(brand, model, year, fuelType);

        // when
        BigDecimal result = vehicleSpecService.getTankCapacity(brand, model, year, fuelType);

        // then
        assertThat(result).isEqualByComparingTo(DEFAULT_TANK_CAPACITY);
    }

    @Test
    void getTankCapacity_shouldNotPropagateDataIntegrityViolation_whenMissingServiceThrows() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(missingVehicleSpecService)
                .save(brand, model, year, fuelType);

        // when / then
        assertThatCode(() ->
                vehicleSpecService.getTankCapacity(brand, model, year, fuelType))
                .doesNotThrowAnyException();
    }

    @Test
    void getTankCapacity_shouldPropagateOtherExceptions_whenMissingServiceThrows() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("boom"))
                .when(missingVehicleSpecService)
                .save(brand, model, year, fuelType);

        // when / then
        assertThatThrownBy(() ->
                vehicleSpecService.getTankCapacity(brand, model, year, fuelType))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }

    @Test
    void getTankCapacity_shouldPassArgumentsToMissingService_whenSpecDoesNotExist() {
        // given
        when(vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        "Honda", "Civic", 2021, FuelType.DIESEL))
                .thenReturn(Optional.empty());

        // when
        vehicleSpecService.getTankCapacity("Honda", "Civic", 2021, FuelType.DIESEL);

        // then
        verify(missingVehicleSpecService).save("Honda", "Civic", 2021, FuelType.DIESEL);
    }
}