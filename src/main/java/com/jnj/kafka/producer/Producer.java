package com.jnj.kafka.producer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class Producer {

    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private static final int BATCH_SIZE = 1000;

    private final KafkaProducer<Long, GenericRecord> producer;
    private final String topicName;

    public Producer(KafkaProducer<Long, GenericRecord> producer, String topicName) {
        this.producer = producer;
        this.topicName = topicName;
    }

    public void start(long messages, String schemaName) throws IOException {

        SchemaLoader schemaLoader = new SchemaLoader();
        Schema schema = schemaLoader.getSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException(format("Schema \"%s.avsc\" was not found in the classpath", schemaName));
        }
        DataGenerator dataGenerator = new DataGenerator(new HashMap<>());

        log.info("Producer started");
        long count = 0;
        try {
            while (count < messages) {
                long batchSize = count + BATCH_SIZE < messages ? BATCH_SIZE : messages - count;
                List<GenericRecord> records = convert(schema, dataGenerator.generate(schema, (int) batchSize));
                for (GenericRecord record : records) {
                    producer.send(new ProducerRecord<>(topicName, (Long) record.get("id"), record));
                }
                producer.flush();
                log.info("Produced {} messages", batchSize);
                count += batchSize;
            }
        } catch (Exception e) {
            log.error("Error producing messages", e);
            throw e;
        } finally {
            producer.close();
        }
        log.info("Produced total of {} messages", count);
        log.info("Producer finished");
    }

    private List<GenericRecord> convert(Schema schema, List<Map<String, Object>> values) {

        List<GenericRecord> records = new ArrayList<>();
        for (Map<String, Object> value : values) {
            GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
            for (Schema.Field field : schema.getFields()) {
                recordBuilder.set(field, value.get(field.name()));
            }
            records.add(recordBuilder.build());
        }
        return records;
    }
}
