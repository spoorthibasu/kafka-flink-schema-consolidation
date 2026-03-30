package com.example.consolidation.adapter;

/**
 * Attributes specific to standard (single-passenger) rides.
 * Null for SHARED and SCHEDULED rides.
 */
public class StandardRideAttributes {

    private String vehicleClass;
    private double surgeMultiplier;

    public String getVehicleClass()             { return vehicleClass; }
    public void setVehicleClass(String v)       { this.vehicleClass = v; }

    public double getSurgeMultiplier()          { return surgeMultiplier; }
    public void setSurgeMultiplier(double s)    { this.surgeMultiplier = s; }
}
