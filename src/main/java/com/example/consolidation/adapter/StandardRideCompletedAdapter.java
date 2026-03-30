package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCompletedStandardEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCompletedStandardEvent to the consolidated schema. */
public class StandardRideCompletedAdapter
        implements RecordAdapter<DriverRideCompletedStandardEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideCompletedStandardEvent event) {
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
        record.setRideType(RideType.STANDARD);
        StandardRideAttributes attrs = new StandardRideAttributes();
        attrs.setVehicleClass(event.getVehicleClass());
        attrs.setSurgeMultiplier(event.getSurgeMultiplier());
        record.setStandardRideAttributes(attrs);
        record.setFareAmount(event.getFareAmount());
        record.setDurationMinutes(event.getDurationMinutes());
        record.setDistanceKm(event.getDistanceKm());
        return record;
    }
}
