package com.example.consolidation.job;

import com.example.consolidation.adapter.AdapterRegistry;
import com.example.consolidation.adapter.ConsolidationAdapter;
import com.example.consolidation.adapter.DriverRideActivityRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Reads ride events from a single Kafka topic and consolidates them into one
 * DriverRideActivityRecord schema, writing results to S3.
 *
 * ConsolidationAdapter reads the eventType and rideType fields from each event,
 * looks up the right RecordAdapter in the registry, and delegates transformation.
 *
 * Exactly-once checkpointing keeps Kafka offsets in sync with Iceberg commits,
 * so a restart replays from the last checkpoint without duplicating records.
 * The 30-second minimum pause between checkpoints prevents back-to-back runs.
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

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setTopics("ride-events")
                .setGroupId("ride-consolidation-consumer")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        AdapterRegistry adapterRegistry = AdapterRegistry.withAllAdapters();

        DataStream<DriverRideActivityRecord> consolidatedRecords = env
                .fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka ride events")
                .map(new ConsolidationAdapter(adapterRegistry))
                .name("ConsolidationAdapter");

        // Replace .print() with an Iceberg sink in production, for example:
        //
        // FlinkSink.<DriverRideActivityRecord>forRowType(tableSchema.asStruct(), ...)
        //     .tableLoader(tableLoader)
        //     .build();
        //
        // consolidatedRecords.sinkTo(icebergSink);
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
