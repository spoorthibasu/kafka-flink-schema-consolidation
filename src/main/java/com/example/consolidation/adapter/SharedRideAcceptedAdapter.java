package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedSharedEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/**
 * Maps a DriverRideAcceptedSharedEvent to the consolidated schema.
 *
 * No Flink dependency. Pure transformation logic that can be unit tested
 * without any framework setup.
 */
public class SharedRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedSharedEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideAcceptedSharedEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_ACCEPTED);
        record.setRideType(RideType.SHARED);

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        // scheduledRideAttributes remains null
        return record;
    }
}
