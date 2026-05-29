package com.example.consolidation.adapter;

/** Fields only SHARED rides carry. Null on the record for other ride types. */
public class SharedRideAttributes {

    /** Number of passengers in the shared ride pool. */
    private int passengerCount;

    /** Score (0-1) indicating how efficiently this ride was matched with pool participants. */
    private double poolingScore;

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    public double getPoolingScore() { return poolingScore; }
    public void setPoolingScore(double poolingScore) { this.poolingScore = poolingScore; }

    @Override
    public String toString() {
        return "{passengerCount=" + passengerCount + ", poolingScore=" + poolingScore + "}";
    }
}
