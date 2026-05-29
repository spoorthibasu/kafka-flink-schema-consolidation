package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedScheduledEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedScheduledEvent to the consolidated schema. */
public class ScheduledRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedScheduledEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideCompletedScheduledEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.COMPLETED);
        record.setRideType(RideType.SCHEDULED);
        record.setFareAmount(event.getFareAmount());
        record.setDurationMins(event.getDurationMins());
        record.setDistanceKm(event.getDistanceKm());

        ScheduledRideAttributes attrs = new ScheduledRideAttributes();
        attrs.setScheduledTime(event.getScheduledTime());
        attrs.setAdvanceBookingMinutes(event.getAdvanceBookingMinutes());
        record.setScheduledRideAttributes(attrs);
        return record;
    }
}
