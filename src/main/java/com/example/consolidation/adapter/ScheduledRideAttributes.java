package com.example.consolidation.adapter;

/** Fields only SCHEDULED rides carry. Null on the record for other ride types. */
public class ScheduledRideAttributes {

    /** Requested pickup time, epoch millis. */
    private long scheduledTime;

    /** Minutes between booking and the scheduled pickup. */
    private int advanceBookingMinutes;

    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }

    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
    public void setAdvanceBookingMinutes(int advanceBookingMinutes) { this.advanceBookingMinutes = advanceBookingMinutes; }

    @Override
    public String toString() {
        return "{scheduledTime=" + scheduledTime + ", advanceBookingMinutes=" + advanceBookingMinutes + "}";
    }
}
