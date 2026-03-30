package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedStandardEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedStandardEvent to the consolidated schema. */
public class StandardRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedStandardEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideCompletedStandardEvent event) {
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
        record.setRideType(RideType.STANDARD);
        record.setFareAmount(event.getFareAmount());
        record.setDurationMinutes(event.getDurationMinutes());
        record.setDistanceKm(event.getDistanceKm());
        return record;
    }
}
