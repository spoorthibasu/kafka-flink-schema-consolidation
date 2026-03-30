package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideStartedSharedEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideStartedSharedEvent to the consolidated schema. */
public class SharedRideStartedAdapter
        implements RecordAdapter<DriverRideStartedSharedEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideStartedSharedEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_STARTED);
        record.setRideType(RideType.SHARED);

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        return record;
    }
}
