package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedScheduledEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedScheduledEvent to the consolidated schema. */
public class ScheduledRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedScheduledEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideCompletedScheduledEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_COMPLETED);
        record.setRideType(RideType.SCHEDULED);
        record.setFareAmount(event.getFareAmount());
        record.setDurationMinutes(event.getDurationMinutes());
        record.setDistanceKm(event.getDistanceKm());

        ScheduledRideAttributes attrs = new ScheduledRideAttributes();
        attrs.setScheduledTime(event.getScheduledTime());
        attrs.setAdvanceBookingMinutes(event.getAdvanceBookingMinutes());
        record.setScheduledRideAttributes(attrs);
        return record;
    }
}
