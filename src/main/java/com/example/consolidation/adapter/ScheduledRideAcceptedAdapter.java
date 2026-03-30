package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedScheduledEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/**
 * Maps a DriverRideAcceptedScheduledEvent to the consolidated schema.
 *
 * No Flink dependency. Pure transformation logic that can be unit tested
 * without any framework setup.
 */
public class ScheduledRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedScheduledEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideAcceptedScheduledEvent event) {
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
        record.setRideType(RideType.SCHEDULED);

        ScheduledRideAttributes attrs = new ScheduledRideAttributes();
        attrs.setScheduledTime(event.getScheduledTime());
        attrs.setAdvanceBookingMinutes(event.getAdvanceBookingMinutes());
        record.setScheduledRideAttributes(attrs);
        // sharedRideAttributes remains null
        return record;
    }
}
