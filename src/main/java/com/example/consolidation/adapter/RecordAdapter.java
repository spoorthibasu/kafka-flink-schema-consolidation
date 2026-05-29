package com.example.consolidation.adapter;

import java.io.Serializable;

/**
 * Maps one typed source event to the consolidated record. Plain Java, no Flink,
 * so each implementation is easy to unit test on its own. Serializable so Flink
 * can ship the adapters out to its task slots.
 *
 * @param <S> source event type (e.g. DriverRideAcceptedSharedEvent)
 * @param <T> output record type (e.g. DriverRideActivityRecord)
 */
public interface RecordAdapter<S, T> extends Serializable {

    /** @param orgId tenant id, for multi-tenant pipelines */
    T adapt(String orgId, S event);
}
