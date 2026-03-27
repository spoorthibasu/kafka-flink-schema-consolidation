package com.example.consolidation.job;

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
 * Key pipeline stages:
 *
 * 1. KafkaSource: Consumes raw events from multiple input topics in parallel.
 *    Each topic carries a different event type (accepted, started, completed, cancelled).
 *
 * 2. EventTypeRouter: Enriches each raw event with metadata about its type and
 *    ride variant so the ConsolidationAdapter knows how to process it.
 *
 * 3. ConsolidationAdapter: Maps each raw event to a DriverRideActivityRecord,
 *    setting discriminator fields and populating the appropriate attribute block.
 *
 * 4. Sink: Writes consolidated records to S3 in Parquet format, partitioned by
 *    date and city for efficient downstream querying.
 *
 * Fault tolerance:
 * Checkpointing is enabled with exactly-once semantics. The checkpoint interval
 * is configurable via the --checkpoint-interval argument (default: 60 seconds).
 * Flink state is stored in the configured state backend and checkpoints are
 * written to S3.
 *
 * Parallelism:
 * The job respects Flink's parallelism settings. Each Kafka partition is processed
 * by one parallel task instance. Set parallelism to match your Kafka partition count
 * for maximum throughput.
 */
public class RideEventConsolidationJob {

    private static final List<String> INPUT_TOPICS = Arrays.asList(
            "driver-ride-accepted",
            "driver-ride-started",
            "driver-ride-completed",
            "driver-ride-cancelled"
    );

    public static void main(String[] args) throws Exception {
        // Parse job arguments
        String bootstrapServers = getArg(args, "--kafka-bootstrap-servers", "localhost:9092");
        String outputPath = getArg(args, "--output-path", "s3://your-bucket/driver_ride_activity");
        long checkpointInterval = Long.parseLong(getArg(args, "--checkpoint-interval", "60000"));

        // Set up the Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Enable checkpointing for fault tolerance
        // Exactly-once semantics ensure no events are lost or duplicated on failure
        env.enableCheckpointing(checkpointInterval, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(checkpointInterval / 2);
        env.getCheckpointConfig().setCheckpointTimeout(120_000);

        // Build the Kafka source
        // All input topics are consumed in a single source for unified processing
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics(INPUT_TOPICS)
                .setGroupId("ride-event-consolidation-job")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // Consume raw events from Kafka
        DataStream<String> rawEvents = env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka: Ride Events");

        // Route and enrich events with type metadata, then consolidate
        // In a production system, you would deserialize JSON/Avro here before routing
        DataStream<ConsolidatedRecord> consolidatedRecords = rawEvents
                .map(new EventTypeRouter())     // Enrich with _eventType and _rideType metadata
                .map(new ConsolidationAdapter()) // Map to DriverRideActivityRecord
                .name("ConsolidationAdapter");

        // Write to S3 in Parquet format
        // Partitioned by ingestion date and cityId for efficient downstream queries:
        // s3://your-bucket/driver_ride_activity/date=2024-01-15/city=NYC/part-0.parquet
        //
        // Note: In production, use Flink's FileSink with ParquetWriterFactory.
        // The sink configuration is omitted here for clarity but the pattern is:
        //
        // FileSink<ConsolidatedRecord> sink = FileSink
        //     .forBulkFormat(new Path(outputPath), ParquetWriterFactory.of(...))
        //     .withRollingPolicy(OnCheckpointRollingPolicy.build())
        //     .withBucketAssigner(new DateCityBucketAssigner())
        //     .build();
        //
        // consolidatedRecords.sinkTo(sink);

        consolidatedRecords.print(); // Replace with actual sink in production

        env.execute("Ride Event Consolidation Job");
    }

    private static String getArg(String[] args, String key, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }
}
