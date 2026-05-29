package com.example.consolidation.job;

import com.example.consolidation.adapter.AdapterRegistry;
import com.example.consolidation.adapter.ConsolidationAdapter;
import com.example.consolidation.adapter.DriverRideActivityRecord;
import com.example.consolidation.adapter.RawRideEvent;
import com.example.consolidation.adapter.RawRideEventDeserializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * The runnable job. Reads ride-events off Kafka, routes each one through
 * ConsolidationAdapter into a DriverRideActivityRecord, and writes the single
 * stream out.
 *
 * Checkpointing is exactly-once, so a restart replays from the last checkpoint
 * without duplicate records.
 */
public class RideActivityConsolidationJob {

    public static void main(String[] args) throws Exception {
        String bootstrapServers = getArg(args, "--kafka-bootstrap-servers", "localhost:9092");
        String outputPath       = getArg(args, "--output-path", "s3://your-bucket/driver_ride_activity");
        long checkpointInterval = Long.parseLong(getArg(args, "--checkpoint-interval", "60000"));

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(checkpointInterval, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30_000);
        env.getCheckpointConfig().setCheckpointTimeout(120_000);

        KafkaSource<RawRideEvent> source = KafkaSource.<RawRideEvent>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics("ride-events")
                .setGroupId("ride-consolidation-consumer")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new RawRideEventDeserializer())
                .build();

        AdapterRegistry adapterRegistry = AdapterRegistry.withAllAdapters();

        DataStream<DriverRideActivityRecord> consolidatedRecords = env
                .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka ride events")
                .map(new ConsolidationAdapter(adapterRegistry))
                .name("ConsolidationAdapter");

        // Prints so the job runs anywhere. In production, swap this for an Iceberg
        // sink writing to outputPath, e.g. consolidatedRecords.sinkTo(icebergSink).
        System.out.println("Consolidating 'ride-events' -> " + outputPath);
        consolidatedRecords.print();

        env.execute("Ride Activity Schema Consolidation");
    }

    private static String getArg(String[] args, String key, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) return args[i + 1];
        }
        return defaultValue;
    }
}
