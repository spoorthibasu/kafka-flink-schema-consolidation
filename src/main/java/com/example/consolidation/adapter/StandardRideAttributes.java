package com.example.consolidation.adapter;

/** Fields only STANDARD rides carry. Null on the record for other ride types. */
public class StandardRideAttributes {

    private String vehicleClass;
    private double surgeMultiplier;

    public String getVehicleClass()             { return vehicleClass; }
    public void setVehicleClass(String v)       { this.vehicleClass = v; }

    public double getSurgeMultiplier()          { return surgeMultiplier; }
    public void setSurgeMultiplier(double s)    { this.surgeMultiplier = s; }

    @Override
    public String toString() {
        return "{vehicleClass=" + vehicleClass + ", surgeMultiplier=" + surgeMultiplier + "}";
    }
}
