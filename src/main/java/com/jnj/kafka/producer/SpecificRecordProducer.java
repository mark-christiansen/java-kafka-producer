package com.jnj.kafka.producer;

import com.jnj.model.avro.Person;
import com.jnj.model.avro.Status;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class SpecificRecordProducer {

    private static final Logger log = LoggerFactory.getLogger(SpecificRecordProducer.class);
    private static final int BATCH_SIZE = 1000;

    private final KafkaProducer<Long, Person> producer;
    private final String topicName;

    public SpecificRecordProducer(KafkaProducer<Long, Person> producer, String topicName) {
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
                List<Person> records = convert(dataGenerator.generate(schema, (int) batchSize));
                for (Person record : records) {
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

    private List<Person> convert(List<Map<String, Object>> values) {

        List<Person> records = new ArrayList<>();
        for (Map<String, Object> value : values) {

            GenericData.EnumSymbol statusEnum = (GenericData.EnumSymbol) value.get("status");
            Status status = statusEnum != null ? Status.valueOf(statusEnum.toString()) : null;

            Person person = Person.newBuilder()
                    .setId((Long) value.get("id"))
                    .setFirstName((String) value.get("firstName"))
                    .setLastName((String) value.get("lastName"))
                    .setStatus(status)
                    .setAge((Integer) value.get("age"))
                    .setBirthDate((LocalDate) value.get("birthDate"))
                    .setMoney((BigDecimal) value.get("money"))
                    .setCreated((Instant) value.get("created"))
                    .setUpdated((Instant) value.get("updated"))
                    //.setSsn((String) value.get("ssn"))
                    .build();
            records.add(person);
        }
        return records;
    }
}
