package com.example.consolidation.adapter;

/**
 * Attribute block for shared (pooled) ride events.
 *
 * This object is populated only when rideType is SHARED.
 * For STANDARD and SCHEDULED rides, sharedRideAttributes is null in the
 * consolidated record.
 *
 * Adding new fields to this class is a backward-compatible Avro schema change
 * as long as new fields are nullable with a default of null.
 */
public class SharedRideAttributes {

    /** Number of passengers in the shared ride pool. */
    private int passengerCount;

    /** Score (0-1) indicating how efficiently this ride was matched with pool participants. */
    private double poolingScore;

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    public double getPoolingScore() { return poolingScore; }
    public void setPoolingScore(double poolingScore) { this.poolingScore = poolingScore; }
}
