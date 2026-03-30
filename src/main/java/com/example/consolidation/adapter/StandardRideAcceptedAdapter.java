package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedStandardEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/**
 * Maps a DriverRideAcceptedStandardEvent to the consolidated schema.
 *
 * No Flink dependency. Pure transformation logic that can be unit tested
 * without any framework setup.
 */
public class StandardRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedStandardEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideAcceptedStandardEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.ACCEPTED);
        record.setRideType(RideType.STANDARD);
        StandardRideAttributes attrs = new StandardRideAttributes();
        attrs.setVehicleClass(event.getVehicleClass());
        attrs.setSurgeMultiplier(event.getSurgeMultiplier());
        record.setStandardRideAttributes(attrs);
        return record;
    }
}
