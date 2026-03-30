package com.example.consolidation.adapter;

/**
 * Contract for mapping a typed source event to a consolidated output record.
 *
 * Implementations are pure transformation logic with no dependency on the Flink
 * framework, making them straightforward to unit test without any framework setup.
 *
 * @param <S> source event type (e.g. DriverRideAcceptedSharedEvent)
 * @param <T> output record type (e.g. ConsolidatedRecord)
 */
public interface RecordAdapter<S, T> {

    /**
     * Maps a typed source event to the consolidated output schema.
     *
     * @param orgId  organizational identifier for multi-tenant pipelines
     * @param event  the typed source event to transform
     * @return       the populated consolidated record
     */
    T adapt(String orgId, S event);
}
