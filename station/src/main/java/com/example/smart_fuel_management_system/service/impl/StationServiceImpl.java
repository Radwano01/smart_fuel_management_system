package com.example.smart_fuel_management_system.service.impl;

import com.example.smart_fuel_management_system.dto.pump.PumpResponse;
import com.example.smart_fuel_management_system.dto.station.*;
import com.example.smart_fuel_management_system.entity.Station;
import com.example.smart_fuel_management_system.enums.StationStatusType;
import com.example.smart_fuel_management_system.exceptions.BadRequestException;
import com.example.smart_fuel_management_system.repository.StationEmployeeRepository;
import com.example.smart_fuel_management_system.repository.StationRepository;
import com.example.smart_fuel_management_system.service.PumpHeartbeatService;
import com.example.smart_fuel_management_system.service.StationService;
import com.example.smart_fuel_management_system.service.impl.client.AuthClient;
import com.example.smart_fuel_management_system.service.impl.client.TransactionClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final StationEmployeeRepository stationEmployeeRepository;
    private final TransactionClient transactionClient;
    private final AuthClient authClient;
        private final PumpHeartbeatService pumpHeartbeatService;

    @Override
    public void create(CreateStationRequest request) {

        Station station = new Station(
                request.name(),
                request.city(),
                request.address(),
                StationStatusType.INACTIVE,
                request.contactInformation(),
                request.latitude(),
                request.longitude()
        );

        stationRepository.save(station);
    }

    @Transactional(readOnly = true)
    @Override
    public StationDetailsResponse getStationDetails(UUID stationId) {

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Station not found: " + stationId
                        )
                );

        TransactionStationResponse transactionStats =
                transactionClient.getTransactionsAndVehiclesCount(stationId)
                        .orElse(null);

        StationEmployeeResponse employee = station.getEmployee() == null
                ? null
                : authClient.getEmployeeInfo(station.getEmployee().getEmployeeId()).orElse(null);


        List<PumpResponse> pumps = station.getPumps()
                .stream()
                .map(pumpHeartbeatService::enrich)
                .toList();

        return new StationDetailsResponse(
                station.getId(),
                station.getName(),
                station.getCity(),
                station.getAddress(),
                station.getContactInformation(),
                station.getLatitude(),
                station.getLongitude(),
                station.getStatus(),
                station.getCreatedAt(),
                station.getUpdatedAt(),
                transactionStats,
                employee,
                pumps
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<StationTransactionResponse> getStationsByIds(List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return stationRepository.findStationsByIds(ids);
    }

    @Transactional
    @Override
    public void changeStatus(StationStatusType status, UUID employeeId) {
        if (status == null) {
            throw new IllegalArgumentException("Station status cannot be null");
        }

        Station station = getStationByEmployeeId(employeeId);

        if(station.getStatus() == StationStatusType.MAINTENANCE){
            throw new BadRequestException("only admin can change the status");
        }

        station.setStatus(status);
    }

    @Transactional(readOnly = true)
    @Override
    public StationTransactionResponse getDetailsForTransaction(UUID stationId) {
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("This station does not exist"));

        return new StationTransactionResponse(
                station.getId(),
                station.getName(),
                station.getCity(),
                station.getAddress()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public StationResponse getDetailsForStation(UUID employeeId) {
        Station station = getStationByEmployeeId(employeeId);

        return new StationResponse(
                station.getId(),
                station.getName(),
                station.getCity(),
                station.getAddress(),
                station.getStatus(),
                station.getPumps().stream().map(pumpHeartbeatService::enrich).toList()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public StationValidationResponse validateStation(
            UUID stationId) {

        boolean stationExists = stationRepository.existsById(stationId);

        if (!stationExists) {
            return new StationValidationResponse(false, false);
        }

        boolean stationAlreadyHasEmployee =
                stationEmployeeRepository.existsByStationId(stationId);

        return new StationValidationResponse(
                true,
                !stationAlreadyHasEmployee
        );
    }

    @Transactional(readOnly = true)
    public Station getStation(UUID stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("station not found!"));
    }

    @Transactional(readOnly = true)
    @Override
    public StationDashboardSummaryResponse getDashboardSummary() {

        long totalStations = stationRepository.count();
        long totalStationAccounts = stationEmployeeRepository.count();

        long activeStations =
                stationRepository.countByStatus(StationStatusType.ACTIVE);

        long inactiveStations =
                stationRepository.countByStatus(StationStatusType.INACTIVE);

        long maintenanceStations =
                stationRepository.countByStatus(StationStatusType.MAINTENANCE);

        return new StationDashboardSummaryResponse(
                totalStations,
                totalStationAccounts,
                activeStations,
                inactiveStations,
                maintenanceStations
        );
    }

    @Override
    @Transactional
    public void updateStation(UUID stationId, UpdateStationRequest request) {

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Station not found"));

        if (request.name() != null) {
            station.setName(request.name());
        }

        if (request.address() != null) {
            station.setAddress(request.address());
        }

        if (request.city() != null) {
            station.setCity(request.city());
        }

        if (request.contactInformation() != null) {
            station.setContactInformation(request.contactInformation());
        }

        if (request.latitude() != null) {
            station.setLatitude(request.latitude());
        }

        if (request.longitude() != null) {
            station.setLongitude(request.longitude());
        }

        if (request.status() != null) {
            station.setStatus(request.status());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StationSummaryResponse> searchAndFilterStations(
            String search,
            StationStatusType status,
            Pageable pageable
    ) {

        if (search != null) {
            search = search.trim();

            if (search.isBlank()) {
                search = null;
            }
        }

        Page<Station> stations = stationRepository.searchAndFilter(
                search,
                status,
                pageable
        );

        List<UUID> stationIds = stations.getContent()
                .stream()
                .map(Station::getId)
                .toList();

        List<TransactionStationResponse> transactionData =
                transactionClient.getTransactionsAndVehiclesCount(stationIds);

        Map<UUID, TransactionStationResponse> transactionDataByStation =
                transactionData.stream()
                        .collect(Collectors.toMap(
                                TransactionStationResponse::stationId,
                                response -> response
                        ));

        return stations.map(station ->
                new StationSummaryResponse(
                        station.getId(),
                        station.getName(),
                        station.getCity(),
                        station.getAddress(),
                        station.getContactInformation(),
                        station.getLatitude(),
                        station.getLongitude(),
                        station.getStatus(),
                        transactionDataByStation.get(station.getId())
                )
        );
    }

    private Station getStationByEmployeeId(UUID employeeId){
        return stationEmployeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(()-> new EntityNotFoundException("Employee does not have station"));
    }
}