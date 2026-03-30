package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedSharedEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/**
 * Maps a DriverRideAcceptedSharedEvent to the consolidated schema.
 *
 * No Flink dependency. Pure transformation logic that can be unit tested
 * without any framework setup.
 */
public class SharedRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedSharedEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideAcceptedSharedEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getTimestamp());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.ACCEPTED);
        record.setRideType(RideType.SHARED);

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        // scheduledRideAttributes remains null
        return record;
    }
}
