package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedSharedEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedSharedEvent to the consolidated schema. */
public class SharedRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedSharedEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideCompletedSharedEvent event) {
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
        record.setRideType(RideType.SHARED);
        record.setFareAmount(event.getFareAmount());
        record.setDurationMinutes(event.getDurationMinutes());
        record.setDistanceKm(event.getDistanceKm());

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        return record;
    }
}
