package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideStartedScheduledEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideStartedScheduledEvent to the consolidated schema. */
public class ScheduledRideStartedAdapter
        implements RecordAdapter<DriverRideStartedScheduledEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideStartedScheduledEvent event) {
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
        record.setRideType(RideType.SCHEDULED);

        ScheduledRideAttributes attrs = new ScheduledRideAttributes();
        attrs.setScheduledTime(event.getScheduledTime());
        attrs.setAdvanceBookingMinutes(event.getAdvanceBookingMinutes());
        record.setScheduledRideAttributes(attrs);
        return record;
    }
}
