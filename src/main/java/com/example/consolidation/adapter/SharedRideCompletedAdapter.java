package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedSharedEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedSharedEvent to the consolidated schema. */
public class SharedRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedSharedEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideCompletedSharedEvent event) {
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
