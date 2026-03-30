package com.example.consolidation.adapter;

import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Registry that maps each (RideEventType, RideType) combination to its
 * dedicated RecordAdapter.
 *
 * The registry stores type-erased adapters internally but exposes a type-safe
 * registration API. Each entry pairs an adapter with a converter that
 * deserializes the raw event map into the adapter's typed input.
 *
 * In a production pipeline the converter would call an Avro or JSON deserializer.
 * In this reference implementation it calls the event class's static fromMap()
 * factory method.
 *
 * Use {@link #withAllAdapters()} to obtain a registry pre-loaded with all 12
 * ride event adapters. To add a new ride variant, register a new adapter here
 * without touching any other class.
 */
public class AdapterRegistry implements Serializable {

    /**
     * Internal type that binds an adapter to its raw-event converter, hiding
     * the generic source type so adapters of different types can be stored
     * together in a single map.
     */
    @FunctionalInterface
    private interface BoundAdapter extends Serializable {
        ConsolidatedRecord adapt(String orgId, Map<String, Object> rawEvent);
    }

    private static class AdapterKey implements Serializable {
        private final RideEventType eventType;
        private final RideType rideType;

        AdapterKey(RideEventType eventType, RideType rideType) {
            this.eventType = eventType;
            this.rideType  = rideType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AdapterKey)) return false;
            AdapterKey that = (AdapterKey) o;
            return eventType == that.eventType && rideType == that.rideType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, rideType);
        }
    }

    private final Map<AdapterKey, BoundAdapter> adapters = new HashMap<>();

    /**
     * Registers an adapter for one (eventType, rideType) combination.
     *
     * @param eventType  discriminator for the ride lifecycle event
     * @param rideType   discriminator for the ride product variant
     * @param adapter    typed adapter with no Flink dependency
     * @param fromMap    converts the raw event map to the adapter's typed input
     * @param <S>        source event type the adapter expects
     */
    public <S> void register(RideEventType eventType,
                              RideType rideType,
                              RecordAdapter<S, ConsolidatedRecord> adapter,
                              Function<Map<String, Object>, S> fromMap) {
        adapters.put(new AdapterKey(eventType, rideType),
                     (orgId, raw) -> adapter.adapt(orgId, fromMap.apply(raw)));
    }

    /**
     * Looks up and invokes the adapter for the given discriminator combination.
     *
     * @throws IllegalArgumentException if no adapter is registered for the combination
     */
    public ConsolidatedRecord adapt(String orgId,
                                     RideEventType eventType,
                                     RideType rideType,
                                     Map<String, Object> rawEvent) {
        BoundAdapter bound = adapters.get(new AdapterKey(eventType, rideType));
        if (bound == null) {
            throw new IllegalArgumentException(
                "No adapter registered for eventType=" + eventType + ", rideType=" + rideType);
        }
        return bound.adapt(orgId, rawEvent);
    }

    /**
     * Returns a registry pre-loaded with all 12 ride event adapters — one per
     * (RideEventType, RideType) combination. Adding a new ride variant means
     * adding one adapter class and one line here.
     */
    public static AdapterRegistry withAllAdapters() {
        AdapterRegistry registry = new AdapterRegistry();

        // RIDE_ACCEPTED
        registry.register(RideEventType.RIDE_ACCEPTED, RideType.STANDARD,
                new StandardRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedStandardEvent::fromMap);
        registry.register(RideEventType.RIDE_ACCEPTED, RideType.SHARED,
                new SharedRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedSharedEvent::fromMap);
        registry.register(RideEventType.RIDE_ACCEPTED, RideType.SCHEDULED,
                new ScheduledRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedScheduledEvent::fromMap);

        // RIDE_STARTED
        registry.register(RideEventType.RIDE_STARTED, RideType.STANDARD,
                new StandardRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedStandardEvent::fromMap);
        registry.register(RideEventType.RIDE_STARTED, RideType.SHARED,
                new SharedRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedSharedEvent::fromMap);
        registry.register(RideEventType.RIDE_STARTED, RideType.SCHEDULED,
                new ScheduledRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedScheduledEvent::fromMap);

        // RIDE_COMPLETED
        registry.register(RideEventType.RIDE_COMPLETED, RideType.STANDARD,
                new StandardRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedStandardEvent::fromMap);
        registry.register(RideEventType.RIDE_COMPLETED, RideType.SHARED,
                new SharedRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedSharedEvent::fromMap);
        registry.register(RideEventType.RIDE_COMPLETED, RideType.SCHEDULED,
                new ScheduledRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedScheduledEvent::fromMap);

        // RIDE_CANCELLED
        registry.register(RideEventType.RIDE_CANCELLED, RideType.STANDARD,
                new StandardRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledStandardEvent::fromMap);
        registry.register(RideEventType.RIDE_CANCELLED, RideType.SHARED,
                new SharedRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledSharedEvent::fromMap);
        registry.register(RideEventType.RIDE_CANCELLED, RideType.SCHEDULED,
                new ScheduledRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledScheduledEvent::fromMap);

        return registry;
    }
}
