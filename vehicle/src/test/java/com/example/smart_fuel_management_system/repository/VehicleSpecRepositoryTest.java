package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.entity.VehicleSpec;
import com.example.smart_fuel_management_system.enums.FuelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleSpecRepositoryTest {

    @Autowired
    private VehicleSpecRepository vehicleSpecRepository;

    @BeforeEach
    void setUp() {
        vehicleSpecRepository.save(buildSpec("Toyota", "Corolla", 2020, FuelType.GASOLINE, new BigDecimal("50.00")));
        vehicleSpecRepository.save(buildSpec("Toyota", "Corolla", 2020, FuelType.DIESEL, new BigDecimal("50.00")));
        vehicleSpecRepository.save(buildSpec("Honda", "Civic", 2021, FuelType.GASOLINE, new BigDecimal("47.00")));
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldReturnSpec_whenAllFieldsMatch() {
        // given
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2020;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBrand()).isEqualTo("Toyota");
        assertThat(result.get().getModel()).isEqualTo("Corolla");
        assertThat(result.get().getYear()).isEqualTo(2020);
        assertThat(result.get().getFuelType()).isEqualTo(FuelType.GASOLINE);
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldIgnoreBrandCase_whenBrandCaseDiffers() {
        // given
        String brand = "TOYOTA";
        String model = "Corolla";
        int year = 2020;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBrand()).isEqualTo("Toyota");
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldIgnoreModelCase_whenModelCaseDiffers() {
        // given
        String brand = "Toyota";
        String model = "COROLLA";
        int year = 2020;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getModel()).isEqualTo("Corolla");
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldDistinguishByFuelType_whenSpecsShareBrandModelYear() {
        // given
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2020;
        FuelType fuelType = FuelType.DIESEL;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFuelType()).isEqualTo(FuelType.DIESEL);
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldReturnEmpty_whenBrandDoesNotMatch() {
        // given
        String brand = "Nissan";
        String model = "Corolla";
        int year = 2020;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldReturnEmpty_whenModelDoesNotMatch() {
        // given
        String brand = "Toyota";
        String model = "Camry";
        int year = 2020;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldReturnEmpty_whenYearDoesNotMatch() {
        // given
        String brand = "Toyota";
        String model = "Corolla";
        int year = 2025;
        FuelType fuelType = FuelType.GASOLINE;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType_shouldReturnEmpty_whenFuelTypeDoesNotMatch() {
        // given
        String brand = "Honda";
        String model = "Civic";
        int year = 2021;
        FuelType fuelType = FuelType.DIESEL;

        // when
        Optional<VehicleSpec> result = vehicleSpecRepository
                .findByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndFuelType(
                        brand, model, year, fuelType);

        // then
        assertThat(result).isEmpty();
    }

    private VehicleSpec buildSpec(String brand, String model, int year, FuelType fuelType, BigDecimal tankCapacity) {
        return VehicleSpec.builder()
                .brand(brand)
                .model(model)
                .year(year)
                .fuelType(fuelType)
                .tankCapacity(tankCapacity)
                .build();
    }
}