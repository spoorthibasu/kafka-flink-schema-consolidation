package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps each (EventType, RideType) pair to its adapter.
 *
 * register() is type-safe; internally the adapters are stored with their source
 * type erased so they can share one map. Each entry also keeps a fromMap function
 * that builds the adapter's typed input from the raw event. Here fromMap is the
 * event class's static factory; a real pipeline would plug in an Avro or JSON
 * deserializer instead.
 *
 * {@link #withAllAdapters()} returns a registry with all 12 wired up.
 */
public class AdapterRegistry implements Serializable {

    /** An adapter with its source type erased, so different adapters fit one map. */
    @FunctionalInterface
    private interface BoundAdapter extends Serializable {
        DriverRideActivityRecord adapt(String orgId, Map<String, Object> rawEvent);
    }

    /** Builds an adapter's typed input from the raw event map. Serializable so the registry ships to Flink. */
    @FunctionalInterface
    public interface EventFactory<S> extends Serializable {
        S fromMap(Map<String, Object> rawEvent);
    }

    private static class AdapterKey implements Serializable {
        private final EventType eventType;
        private final RideType rideType;

        AdapterKey(EventType eventType, RideType rideType) {
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
     * Registers the adapter for one (eventType, rideType) pair.
     *
     * @param fromMap builds the adapter's typed input from the raw event map
     * @param <S>     source event type the adapter expects
     */
    public <S> void register(EventType eventType,
                              RideType rideType,
                              RecordAdapter<S, DriverRideActivityRecord> adapter,
                              EventFactory<S> fromMap) {
        adapters.put(new AdapterKey(eventType, rideType),
                     (orgId, raw) -> adapter.adapt(orgId, fromMap.fromMap(raw)));
    }

    /** @throws IllegalArgumentException if nothing is registered for this pair */
    public DriverRideActivityRecord adapt(String orgId,
                                     EventType eventType,
                                     RideType rideType,
                                     Map<String, Object> rawEvent) {
        BoundAdapter bound = adapters.get(new AdapterKey(eventType, rideType));
        if (bound == null) {
            throw new IllegalArgumentException(
                "No adapter registered for eventType=" + eventType + ", rideType=" + rideType);
        }
        return bound.adapt(orgId, rawEvent);
    }

    /** All 12 adapters, wired up. A new variant is one adapter class and one line here. */
    public static AdapterRegistry withAllAdapters() {
        AdapterRegistry registry = new AdapterRegistry();

        // RIDE_ACCEPTED
        registry.register(EventType.ACCEPTED, RideType.STANDARD,
                new StandardRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedStandardEvent::fromMap);
        registry.register(EventType.ACCEPTED, RideType.SHARED,
                new SharedRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedSharedEvent::fromMap);
        registry.register(EventType.ACCEPTED, RideType.SCHEDULED,
                new ScheduledRideAcceptedAdapter(),
                com.example.consolidation.events.DriverRideAcceptedScheduledEvent::fromMap);

        // RIDE_STARTED
        registry.register(EventType.STARTED, RideType.STANDARD,
                new StandardRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedStandardEvent::fromMap);
        registry.register(EventType.STARTED, RideType.SHARED,
                new SharedRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedSharedEvent::fromMap);
        registry.register(EventType.STARTED, RideType.SCHEDULED,
                new ScheduledRideStartedAdapter(),
                com.example.consolidation.events.DriverRideStartedScheduledEvent::fromMap);

        // RIDE_COMPLETED
        registry.register(EventType.COMPLETED, RideType.STANDARD,
                new StandardRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedStandardEvent::fromMap);
        registry.register(EventType.COMPLETED, RideType.SHARED,
                new SharedRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedSharedEvent::fromMap);
        registry.register(EventType.COMPLETED, RideType.SCHEDULED,
                new ScheduledRideCompletedAdapter(),
                com.example.consolidation.events.DriverRideCompletedScheduledEvent::fromMap);

        // RIDE_CANCELLED
        registry.register(EventType.CANCELLED, RideType.STANDARD,
                new StandardRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledStandardEvent::fromMap);
        registry.register(EventType.CANCELLED, RideType.SHARED,
                new SharedRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledSharedEvent::fromMap);
        registry.register(EventType.CANCELLED, RideType.SCHEDULED,
                new ScheduledRideCancelledAdapter(),
                com.example.consolidation.events.DriverRideCancelledScheduledEvent::fromMap);

        return registry;
    }
}
