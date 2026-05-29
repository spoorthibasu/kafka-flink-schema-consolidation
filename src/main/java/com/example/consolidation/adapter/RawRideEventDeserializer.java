package com.example.consolidation.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.util.Map;

/**
 * Turns raw Kafka bytes into a RawRideEvent.
 *
 * Kept separate from ConsolidationAdapter so the adapter only deals with routing,
 * not JSON parsing. A real pipeline would swap this for an Avro or Schema Registry
 * deserializer without touching the adapter.
 */
public class RawRideEventDeserializer implements DeserializationSchema<RawRideEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public RawRideEvent deserialize(byte[] message) throws IOException {
        return new RawRideEvent(MAPPER.readValue(message, new TypeReference<Map<String, Object>>() {}));
    }

    @Override
    public boolean isEndOfStream(RawRideEvent nextElement) {
        return false;
    }

    @Override
    public TypeInformation<RawRideEvent> getProducedType() {
        return TypeInformation.of(RawRideEvent.class);
    }
}
