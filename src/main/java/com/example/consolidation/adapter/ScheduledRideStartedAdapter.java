package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideStartedScheduledEvent;
import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideStartedScheduledEvent to the consolidated schema. */
public class ScheduledRideStartedAdapter
        implements RecordAdapter<DriverRideStartedScheduledEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideStartedScheduledEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.STARTED);
        record.setRideType(RideType.SCHEDULED);

        ScheduledRideAttributes attrs = new ScheduledRideAttributes();
        attrs.setScheduledTime(event.getScheduledTime());
        attrs.setAdvanceBookingMinutes(event.getAdvanceBookingMinutes());
        record.setScheduledRideAttributes(attrs);
        return record;
    }
}
