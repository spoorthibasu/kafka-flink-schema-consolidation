package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCancelledStandardEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCancelledStandardEvent to the consolidated schema. */
public class StandardRideCancelledAdapter
        implements RecordAdapter<DriverRideCancelledStandardEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideCancelledStandardEvent event) {
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
        record.setRideType(RideType.STANDARD);
        StandardRideAttributes attrs = new StandardRideAttributes();
        attrs.setVehicleClass(event.getVehicleClass());
        attrs.setSurgeMultiplier(event.getSurgeMultiplier());
        record.setStandardRideAttributes(attrs);
        record.setCancellationReason(event.getCancellationReason());
        return record;
    }
}
