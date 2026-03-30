package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCancelledSharedEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCancelledSharedEvent to the consolidated schema. */
public class SharedRideCancelledAdapter
        implements RecordAdapter<DriverRideCancelledSharedEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideCancelledSharedEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.CANCELLED);
        record.setRideType(RideType.SHARED);
        record.setCancellationReason(event.getCancellationReason());

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        return record;
    }
}
