package com.example.consolidation.adapter;

import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;

/**
 * Core implementation of the consolidated event flattening pattern.
 *
 * This adapter is the heart of the schema consolidation approach. It receives raw
 * Kafka events (represented here as generic Maps for clarity) and maps them to a
 * single {@link ConsolidatedRecord} that covers all event type and ride type variants.
 *
 * The key design decisions implemented here:
 *
 * 1. Discriminator fields (eventType, rideType) are set explicitly on every record
 *    so consumers never need to infer the variant from context.
 *
 * 2. Shared fields common to all variants are populated unconditionally.
 *
 * 3. Type-specific attribute blocks are populated only for the relevant variant.
 *    All other attribute blocks are left null.
 *
 * 4. Completion-only fields (fareAmount, durationMinutes, distanceKm) are populated
 *    only for RIDE_COMPLETED events and null for all others.
 *
 * This class is intentionally kept simple and readable. In a production system, you
 * would typically deserialize the Kafka event into a typed class before passing it
 * to the adapter.
 */
public class ConsolidationAdapter implements MapFunction<Map<String, Object>, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord map(Map<String, Object> rawEvent) throws Exception {
        RideEventType eventType = resolveEventType(rawEvent);
        RideType rideType = resolveRideType(rawEvent);

        ConsolidatedRecord record = new ConsolidatedRecord();

        // Shared fields: present in every record regardless of variant
        record.setEventTime((Long) rawEvent.get("eventTime"));
        record.setDriverId((String) rawEvent.get("driverId"));
        record.setRideId((String) rawEvent.get("rideId"));
        record.setCityId((String) rawEvent.get("cityId"));
        record.setPickupLat((Double) rawEvent.get("pickupLat"));
        record.setPickupLng((Double) rawEvent.get("pickupLng"));
        record.setEstimatedDurationMinutes((Integer) rawEvent.get("estimatedDurationMinutes"));
        record.setEstimatedFare((Double) rawEvent.get("estimatedFare"));

        // Discriminator fields: identify the variant explicitly
        record.setEventType(eventType);
        record.setRideType(rideType);

        // Event-type-specific fields: only populated for relevant event types
        populateEventTypeSpecificFields(record, rawEvent, eventType);

        // Ride-type-specific attribute blocks: only one is populated, rest are null
        populateRideTypeAttributeBlock(record, rawEvent, rideType);

        return record;
    }

    /**
     * Populates fields that are specific to certain event types.
     * All other event-type-specific fields remain null.
     */
    private void populateEventTypeSpecificFields(
            ConsolidatedRecord record,
            Map<String, Object> rawEvent,
            RideEventType eventType) {

        switch (eventType) {
            case RIDE_COMPLETED:
                record.setFareAmount((Double) rawEvent.get("fareAmount"));
                record.setDurationMinutes((Integer) rawEvent.get("durationMinutes"));
                record.setDistanceKm((Double) rawEvent.get("distanceKm"));
                break;

            case RIDE_CANCELLED:
                record.setCancellationReason((String) rawEvent.get("cancellationReason"));
                break;

            case RIDE_ACCEPTED:
            case RIDE_STARTED:
                // No event-type-specific fields for these variants.
                // fareAmount, durationMinutes, distanceKm, cancellationReason remain null.
                break;
        }
    }

    /**
     * Populates the appropriate ride-type-specific attribute block.
     * Only one attribute block is populated per record. All others are null.
     *
     * This is the discriminator-based routing step. Instead of routing to
     * a different schema or table, we route to a different attribute block
     * within the single consolidated schema.
     */
    private void populateRideTypeAttributeBlock(
            ConsolidatedRecord record,
            Map<String, Object> rawEvent,
            RideType rideType) {

        switch (rideType) {
            case SHARED:
                SharedRideAttributes sharedAttrs = new SharedRideAttributes();
                sharedAttrs.setPassengerCount((Integer) rawEvent.get("passengerCount"));
                sharedAttrs.setPoolingScore((Double) rawEvent.get("poolingScore"));
                record.setSharedRideAttributes(sharedAttrs);
                // scheduledRideAttributes remains null
                break;

            case SCHEDULED:
                ScheduledRideAttributes scheduledAttrs = new ScheduledRideAttributes();
                scheduledAttrs.setScheduledTime((Long) rawEvent.get("scheduledTime"));
                scheduledAttrs.setAdvanceBookingMinutes((Integer) rawEvent.get("advanceBookingMinutes"));
                record.setScheduledRideAttributes(scheduledAttrs);
                // sharedRideAttributes remains null
                break;

            case STANDARD:
                // Both attribute blocks remain null for standard rides.
                break;
        }
    }

    private RideEventType resolveEventType(Map<String, Object> rawEvent) {
        String topicOrType = (String) rawEvent.getOrDefault("_eventType", "");
        if (topicOrType.contains("COMPLETED")) return RideEventType.RIDE_COMPLETED;
        if (topicOrType.contains("CANCELLED")) return RideEventType.RIDE_CANCELLED;
        if (topicOrType.contains("STARTED")) return RideEventType.RIDE_STARTED;
        return RideEventType.RIDE_ACCEPTED;
    }

    private RideType resolveRideType(Map<String, Object> rawEvent) {
        String rideTypeStr = (String) rawEvent.getOrDefault("_rideType", "STANDARD");
        try {
            return RideType.valueOf(rideTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RideType.STANDARD;
        }
    }
}
