package com.example.smart_fuel_management_system.rateLimit;

import java.time.Duration;

public enum RateLimitRule {

    // VEHICLES (user-facing vehicle operations)
    CREATE_VEHICLE(10, Duration.ofMinutes(1)),
    LIST_VEHICLES(30, Duration.ofMinutes(1)),
    GET_VEHICLE(40, Duration.ofMinutes(1)),
    UPDATE_VEHICLE(10, Duration.ofMinutes(1)),
    ACTIVATE_VEHICLE(10, Duration.ofMinutes(1)),
    DEACTIVATE_VEHICLE(10, Duration.ofMinutes(1)),

    // ADMIN RFID (sensitive operations)
    ASSIGN_RFID(5, Duration.ofMinutes(1)),
    FIND_BY_RFID(60, Duration.ofMinutes(1)),
    REMOVE_RFID(5, Duration.ofMinutes(1)),

    // ADMIN VEHICLE MANAGEMENT
    ADMIN_VEHICLE_STATUS(10, Duration.ofMinutes(1)),
    ADMIN_VEHICLE_LOOKUP(30, Duration.ofMinutes(1)),
    ADMIN_VEHICLE_DELETE(5, Duration.ofMinutes(1)),
    ADMIN_VEHICLE_RFID_ASSIGN(5, Duration.ofMinutes(1));

    public final int capacity;
    public final Duration duration;

    RateLimitRule(int capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public static RateLimitRule fromPath(String path, String method) {

        // VEHICLES
        if (path.equals("/api/v1/vehicles") && method.equals("POST")) return CREATE_VEHICLE;
        if (path.equals("/api/v1/vehicles") && method.equals("GET")) return LIST_VEHICLES;

        if (path.matches("/api/v1/vehicles/") && method.equals("GET")) return GET_VEHICLE;
        if (path.matches("/api/v1/vehicles") && method.equals("PATCH")) return UPDATE_VEHICLE;

        if (path.endsWith("/activate")) return ACTIVATE_VEHICLE;
        if (path.endsWith("/deactivate")) return DEACTIVATE_VEHICLE;

        // ADMIN RFID
        if (path.equals("/api/v1/admin/vehicles/rfid/assign")) return ASSIGN_RFID;

        if (path.matches("/api/v1/admin/vehicles/rfid/[^/]+") && method.equals("GET"))
            return FIND_BY_RFID;

        if (path.matches("/api/v1/admin/vehicles/rfid/[^/]+") && method.equals("DELETE"))
            return REMOVE_RFID;

        if (path.matches("/api/v1/admin/vehicles/[^/]+/status") && method.equals("PATCH"))
            return ADMIN_VEHICLE_STATUS;

        if (path.matches("/api/v1/admin/vehicles/plate/[^/]+") && method.equals("GET"))
            return ADMIN_VEHICLE_LOOKUP;

        if (path.matches("/api/v1/admin/vehicles/[^/]+") && method.equals("DELETE"))
            return ADMIN_VEHICLE_DELETE;

        if (path.matches("/api/v1/admin/vehicles/[^/]+/rfid") && method.equals("PATCH"))
            return ADMIN_VEHICLE_RFID_ASSIGN;

        return null;
    }
}