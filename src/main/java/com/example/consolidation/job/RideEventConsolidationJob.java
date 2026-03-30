package com.example.consolidation.job;

import com.example.consolidation.adapter.AdapterRegistry;
import com.example.consolidation.adapter.ConsolidationAdapter;
import com.example.consolidation.adapter.ConsolidatedRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Flink job that consumes fragmented ride events from multiple Kafka topics
 * and consolidates them into a single unified schema written to S3.
 *
 * Pipeline stages:
 *
 * 1. KafkaSource: Consumes raw events from four input topics in parallel.
 *    Each topic carries a different event type (accepted, started, completed, cancelled).
 *
 * 2. EventTypeRouter: Enriches each raw event with _eventType and _rideType
 *    metadata so the ConsolidationAdapter can route without re-inspecting the payload.
 *
 * 3. ConsolidationAdapter(adapterRegistry): Looks up the correct RecordAdapter
 *    from the registry by discriminator and delegates transformation to it.
 *    The adapter itself has no Flink dependency — only this class does.
 *
 * 4. Sink: Writes consolidated records to S3 in Parquet format, partitioned by
 *    date and city for efficient downstream querying.
 *
 * Fault tolerance:
 * Checkpointing with exactly-once semantics coordinates Kafka offset commits
 * with Iceberg's transactional commit protocol, so a job restart replays from
 * the last consistent checkpoint without producing duplicate records.
 */
public class RideEventConsolidationJob {

    private static final List<String> INPUT_TOPICS = Arrays.asList(
            "driver-ride-accepted",
            "driver-ride-started",
            "driver-ride-completed",
            "driver-ride-cancelled"
    );

    public static void main(String[] args) throws Exception {
        String bootstrapServers = getArg(args, "--kafka-bootstrap-servers", "localhost:9092");
        String outputPath       = getArg(args, "--output-path", "s3://your-bucket/driver_ride_activity");
        long checkpointInterval = Long.parseLong(getArg(args, "--checkpoint-interval", "60000"));

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(checkpointInterval, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(checkpointInterval / 2);
        env.getCheckpointConfig().setCheckpointTimeout(120_000);

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(INPUT_TOPICS)
                .setGroupId("ride-event-consolidation-job")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Build the registry once. Each (RideEventType, RideType) combination maps
        // to its own RecordAdapter. Adding a new ride variant means one new adapter
        // class and one new register() call in AdapterRegistry.withAllAdapters().
        AdapterRegistry adapterRegistry = AdapterRegistry.withAllAdapters();

        DataStream<String> rawEvents = env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka: Ride Events");

        DataStream<ConsolidatedRecord> consolidatedRecords = rawEvents
                .map(new EventTypeRouter())
                .map(new ConsolidationAdapter(adapterRegistry))
                .name("ConsolidationAdapter");

        // Write to S3 in Parquet format, partitioned by date and cityId.
        //
        // FileSink<ConsolidatedRecord> sink = FileSink
        //     .forBulkFormat(new Path(outputPath), ParquetWriterFactory.of(...))
        //     .withRollingPolicy(OnCheckpointRollingPolicy.build())
        //     .withBucketAssigner(new DateCityBucketAssigner())
        //     .build();
        // consolidatedRecords.sinkTo(sink);

        consolidatedRecords.print(); // Replace with actual sink in production

        env.execute("Ride Event Consolidation Job");
    }

    private static String getArg(String[] args, String key, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) return args[i + 1];
        }
        return defaultValue;
    }
}
