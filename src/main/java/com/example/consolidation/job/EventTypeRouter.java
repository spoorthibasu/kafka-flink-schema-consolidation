package com.example.consolidation.job;

import org.apache.flink.api.common.functions.MapFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes incoming raw Kafka events by enriching them with event type and ride type metadata.
 *
 * In a real pipeline, the event type and ride type would typically be determined by:
 * - The Kafka topic the event came from (e.g., "driver-ride-accepted-shared")
 * - A type field embedded in the event payload
 * - A header in the Kafka message
 *
 * This router extracts that information and adds it as metadata fields (_eventType, _rideType)
 * so the ConsolidationAdapter can apply the correct mapping logic without needing to
 * re-inspect the raw event structure.
 *
 * Separating routing logic from consolidation logic keeps each class focused on
 * a single responsibility and makes both easier to test independently.
 */
public class EventTypeRouter implements MapFunction<String, Map<String, Object>> {

    @Override
    public Map<String, Object> map(String rawEventJson) throws Exception {
        // In production: deserialize JSON/Avro into a typed object here
        // For this reference implementation, we simulate the parsed event
        Map<String, Object> event = parseEvent(rawEventJson);

        // Determine event type and ride type from the raw event
        // and add as routing metadata for the ConsolidationAdapter
        String eventType = determineEventType(event);
        String rideType = determineRideType(event);

        event.put("_eventType", eventType);
        event.put("_rideType", rideType);

        return event;
    }

    private String determineEventType(Map<String, Object> event) {
        // In production, this would read from the Kafka topic name or a type field
        // embedded in the event payload
        return (String) event.getOrDefault("eventType", "RIDE_ACCEPTED");
    }

    private String determineRideType(Map<String, Object> event) {
        return (String) event.getOrDefault("rideType", "STANDARD");
    }

    private Map<String, Object> parseEvent(String rawEventJson) {
        // Placeholder: in production use Jackson or Avro deserializer
        // This simulates a parsed event for the reference implementation
        Map<String, Object> event = new HashMap<>();
        event.put("eventTime", System.currentTimeMillis());
        event.put("driverId", "driver-123");
        event.put("rideId", "ride-456");
        event.put("cityId", "NYC");
        event.put("pickupLat", 40.7128);
        event.put("pickupLng", -74.0060);
        event.put("estimatedDurationMinutes", 12);
        event.put("estimatedFare", 18.50);
        return event;
    }
}
