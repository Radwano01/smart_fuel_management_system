package com.example.smart_fuel_management_system.repository;

import com.example.smart_fuel_management_system.dto.VehicleTransactionResponse;
import com.example.smart_fuel_management_system.entity.Vehicle;
import com.example.smart_fuel_management_system.enums.FuelType;
import com.example.smart_fuel_management_system.enums.VehicleStatusType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String createdAtColumn;
    private static String vehicleTable;

    private UUID userOneId;
    private UUID userTwoId;
    private UUID vehicleOneId;
    private UUID vehicleTwoId;
    private UUID vehicleThreeId;
    private String plateOne;
    private String plateTwo;
    private String plateThree;
    private String rfidOne;
    private String rfidTwo;
    private String rfidThree;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2026, 9, 10, 10, 0);

        userOneId = UUID.randomUUID();
        userTwoId = UUID.randomUUID();
        plateOne = "34ABC123";
        plateTwo = "34DEF456";
        plateThree = "06GHI789";
        rfidOne = "RFID-1";
        rfidTwo = "RFID-2";
        rfidThree = "RFID-3";

        Vehicle v1 = vehicleRepository.save(buildVehicle(
                userOneId, plateOne, rfidOne, "Toyota", "Corolla", 2020,
                FuelType.GASOLINE, new BigDecimal("50.00"),
                VehicleStatusType.ACTIVE));
        vehicleOneId = v1.getId();

        Vehicle v2 = vehicleRepository.save(buildVehicle(
                userOneId, plateTwo, rfidTwo, "Honda", "Civic", 2021,
                FuelType.GASOLINE, new BigDecimal("45.00"),
                VehicleStatusType.ACTIVE));
        vehicleTwoId = v2.getId();

        Vehicle v3 = vehicleRepository.save(buildVehicle(
                userTwoId, plateThree, rfidThree, "Ford", "Focus", 2022,
                FuelType.DIESEL, new BigDecimal("55.00"),
                VehicleStatusType.INACTIVE));
        vehicleThreeId = v3.getId();

        entityManager.flush();
        forceCreatedAt(vehicleOneId, baseTime);
        forceCreatedAt(vehicleTwoId, baseTime.plusHours(1));
        forceCreatedAt(vehicleThreeId, baseTime.plusHours(2));
        entityManager.clear();
    }

    @Test
    void existsByPlateNumber_shouldReturnTrue_whenPlateExists() {
        // given
        String searchPlate = plateOne;

        // when
        boolean exists = vehicleRepository.existsByPlateNumber(searchPlate);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPlateNumber_shouldReturnFalse_whenPlateDoesNotExist() {
        // given
        String searchPlate = "99XYZ999";

        // when
        boolean exists = vehicleRepository.existsByPlateNumber(searchPlate);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void findAllByUserId_shouldReturnAllVehiclesForUser_whenUserHasVehicles() {
        // given
        UUID searchUserId = userOneId;

        // when
        List<Vehicle> result = vehicleRepository.findAllByUserId(searchUserId);

        // then
        assertThat(result)
                .hasSize(2)
                .allMatch(v -> v.getUserId().equals(searchUserId));
    }

    @Test
    void findAllByUserId_shouldReturnEmptyList_whenUserHasNoVehicles() {
        // given
        UUID unknownUserId = UUID.randomUUID();

        // when
        List<Vehicle> result = vehicleRepository.findAllByUserId(unknownUserId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndId_shouldReturnVehicle_whenBothMatch() {
        // given
        UUID searchUserId = userOneId;
        UUID searchVehicleId = vehicleOneId;

        // when
        Optional<Vehicle> result =
                vehicleRepository.findByUserIdAndId(searchUserId, searchVehicleId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(searchVehicleId);
        assertThat(result.get().getUserId()).isEqualTo(searchUserId);
    }

    @Test
    void findByUserIdAndId_shouldReturnEmpty_whenUserDoesNotOwnVehicle() {
        // given
        UUID wrongUserId = userTwoId;
        UUID searchVehicleId = vehicleOneId;

        // when
        Optional<Vehicle> result =
                vehicleRepository.findByUserIdAndId(wrongUserId, searchVehicleId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPlateNumber_shouldReturnVehicle_whenPlateExists() {
        // given
        String searchPlate = plateTwo;

        // when
        Optional<Vehicle> result = vehicleRepository.findByPlateNumber(searchPlate);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getPlateNumber()).isEqualTo(searchPlate);
    }

    @Test
    void findByPlateNumber_shouldReturnEmpty_whenPlateDoesNotExist() {
        // given
        String searchPlate = "00NONE00";

        // when
        Optional<Vehicle> result = vehicleRepository.findByPlateNumber(searchPlate);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByRfidTag_shouldReturnVehicle_whenRfidExists() {
        // given
        String searchRfid = rfidOne;

        // when
        Optional<Vehicle> result = vehicleRepository.findByRfidTag(searchRfid);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRfidTag()).isEqualTo(searchRfid);
    }

    @Test
    void findByRfidTag_shouldReturnEmpty_whenRfidDoesNotExist() {
        // given
        String searchRfid = "RFID-NONE";

        // when
        Optional<Vehicle> result = vehicleRepository.findByRfidTag(searchRfid);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByRfidTagAndPlateNumber_shouldReturnVehicle_whenBothMatch() {
        // given
        String searchRfid = rfidOne;
        String searchPlate = plateOne;

        // when
        Optional<Vehicle> result =
                vehicleRepository.findByRfidTagAndPlateNumber(searchRfid, searchPlate);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRfidTag()).isEqualTo(searchRfid);
        assertThat(result.get().getPlateNumber()).isEqualTo(searchPlate);
    }

    @Test
    void findByRfidTagAndPlateNumber_shouldReturnEmpty_whenOnlyRfidMatches() {
        // given
        String searchRfid = rfidOne;
        String wrongPlate = plateTwo;

        // when
        Optional<Vehicle> result =
                vehicleRepository.findByRfidTagAndPlateNumber(searchRfid, wrongPlate);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void countByUserId_shouldReturnCount_whenUserHasVehicles() {
        // given
        UUID searchUserId = userOneId;

        // when
        long count = vehicleRepository.countByUserId(searchUserId);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByUserId_shouldReturnZero_whenUserHasNoVehicles() {
        // given
        UUID unknownUserId = UUID.randomUUID();

        // when
        long count = vehicleRepository.countByUserId(unknownUserId);

        // then
        assertThat(count).isZero();
    }

    @Test
    void findVehicleForTransaction_shouldReturnResponse_whenVehicleExists() {
        // given
        UUID searchId = vehicleOneId;

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleRepository.findVehicleForTransaction(searchId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(vehicleOneId);
        assertThat(result.get().plateNumber()).isEqualTo(plateOne);
        assertThat(result.get().brand()).isEqualTo("Toyota");
        assertThat(result.get().model()).isEqualTo("Corolla");
        assertThat(result.get().year()).isEqualTo(2020);
    }

    @Test
    void findVehicleForTransaction_shouldReturnEmpty_whenVehicleDoesNotExist() {
        // given
        UUID unknownId = UUID.randomUUID();

        // when
        Optional<VehicleTransactionResponse> result =
                vehicleRepository.findVehicleForTransaction(unknownId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void countByStatus_shouldReturnCount_whenStatusMatches() {
        // given
        VehicleStatusType searchStatus = VehicleStatusType.ACTIVE;

        // when
        long count = vehicleRepository.countByStatus(searchStatus);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByStatus_shouldReturnZero_whenNoVehicleHasStatus() {
        // given
        VehicleStatusType searchStatus = VehicleStatusType.INACTIVE;

        // when
        long count = vehicleRepository.countByStatus(searchStatus);

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findTop10ByOrderByCreatedAtDesc_shouldReturnVehiclesNewestFirst() {
        // given
        // when
        List<Vehicle> result = vehicleRepository.findTop10ByOrderByCreatedAtDesc();

        // then
        assertThat(result)
                .extracting(Vehicle::getId)
                .containsExactly(vehicleThreeId, vehicleTwoId, vehicleOneId);
    }

    @Test
    void findVehiclesByIds_shouldReturnMatchingResponses_whenIdsExist() {
        // given
        List<UUID> searchIds = List.of(vehicleOneId, vehicleThreeId);

        // when
        List<VehicleTransactionResponse> result =
                vehicleRepository.findVehiclesByIds(searchIds);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(VehicleTransactionResponse::id)
                .containsExactlyInAnyOrder(vehicleOneId, vehicleThreeId);
    }

    @Test
    void findVehiclesByIds_shouldReturnEmptyList_whenNoIdsMatch() {
        // given
        List<UUID> searchIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        List<VehicleTransactionResponse> result =
                vehicleRepository.findVehiclesByIds(searchIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findVehiclesByIds_shouldReturnEmptyList_whenIdsListIsEmpty() {
        // given
        List<UUID> searchIds = List.of();

        // when
        List<VehicleTransactionResponse> result =
                vehicleRepository.findVehiclesByIds(searchIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_shouldReturnAllVehiclesForUser_whenUserHasVehicles() {
        // given
        UUID searchUserId = userOneId;

        // when
        List<Vehicle> result = vehicleRepository.findByUserId(searchUserId);

        // then
        assertThat(result)
                .hasSize(2)
                .allMatch(v -> v.getUserId().equals(searchUserId));
    }

    @Test
    void findByUserId_shouldReturnEmptyList_whenUserHasNoVehicles() {
        // given
        UUID unknownUserId = UUID.randomUUID();

        // when
        List<Vehicle> result = vehicleRepository.findByUserId(unknownUserId);

        // then
        assertThat(result).isEmpty();
    }

    private Vehicle buildVehicle(
            UUID userId,
            String plateNumber,
            String rfidTag,
            String brand,
            String model,
            int year,
            FuelType fuelType,
            BigDecimal tankCapacity,
            VehicleStatusType status) {

        return Vehicle.builder()
                .userId(userId)
                .plateNumber(plateNumber)
                .rfidTag(rfidTag)
                .brand(brand)
                .model(model)
                .year(year)
                .fuelType(fuelType)
                .tankCapacity(tankCapacity)
                .status(status)
                .build();
    }

    private void forceCreatedAt(UUID id, LocalDateTime ts) {
        entityManager.createNativeQuery(
                        "UPDATE " + tableName() + " SET " + createdAtColumn() + " = :ts WHERE id = :id")
                .setParameter("ts", ts)
                .setParameter("id", id)
                .executeUpdate();
    }

    private String tableName() {
        if (vehicleTable == null) {
            List<String> names = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables " +
                            "WHERE LOWER(table_name) IN ('vehicle', 'vehicles')",
                    String.class);
            vehicleTable = names.isEmpty() ? "vehicles" : names.get(0);
        }
        return vehicleTable;
    }

    private String createdAtColumn() {
        if (createdAtColumn == null) {
            List<String> names = jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns " +
                            "WHERE LOWER(table_name) IN ('vehicle', 'vehicles') " +
                            "  AND LOWER(column_name) IN ('created_at', 'createdat')",
                    String.class);
            createdAtColumn = names.isEmpty() ? "created_at" : names.get(0);
        }
        return createdAtColumn;
    }
}