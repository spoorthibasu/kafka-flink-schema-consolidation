package com.example.consolidation.adapter;

/**
 * Attribute block for pre-scheduled ride events.
 *
 * This object is populated only when rideType is SCHEDULED.
 * For STANDARD and SHARED rides, scheduledRideAttributes is null in the
 * consolidated record.
 */
public class ScheduledRideAttributes {

    /** The scheduled pickup time requested by the passenger, as a Unix timestamp in milliseconds. */
    private long scheduledTime;

    /** How many minutes in advance this ride was booked before the scheduled pickup time. */
    private int advanceBookingMinutes;

    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }

    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
    public void setAdvanceBookingMinutes(int advanceBookingMinutes) { this.advanceBookingMinutes = advanceBookingMinutes; }
}
