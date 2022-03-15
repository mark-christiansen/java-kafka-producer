package com.jnj.kafka.producer;

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final String OPTION_TOPIC = "topic";
    private static final String OPTION_MESSAGES = "messages";
    private static final String OPTION_SCHEMA = "schema";
    private static final String DEFAULT_SCHEMA = "person";
    private static final String OPTION_PARTITIONS = "partitions";
    private static final int DEFAULT_PARTITIONS = 1;

    @Autowired
    private KafkaProducer<Long, GenericRecord> kafkaProducer;
    @Autowired
    private AdminClient adminClient;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args).close();
    }

    @Override
    public void run(String... args) throws Exception {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);
        String topicName = cmd.getOptionValue(OPTION_TOPIC);
        long messages = Long.parseLong(cmd.getOptionValue(OPTION_MESSAGES));
        String schemaName = cmd.hasOption(OPTION_SCHEMA) ? cmd.getOptionValue(OPTION_SCHEMA) : DEFAULT_SCHEMA;
        int partitions = cmd.hasOption(OPTION_PARTITIONS) ? Integer.parseInt(cmd.getOptionValue(OPTION_PARTITIONS)) : DEFAULT_PARTITIONS;
        createTopic(topicName, partitions);
        Producer producer = new Producer(kafkaProducer, topicName);
        producer.start(messages, schemaName);
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder("t").longOpt(OPTION_TOPIC).optionalArg(false).hasArg(true).desc("Topic to produce to").type(String.class).build());
        options.addOption(Option.builder("m").longOpt(OPTION_MESSAGES).optionalArg(false).hasArg(true).desc("Messages to produce").type(Long.class).build());
        options.addOption(Option.builder("s").longOpt(OPTION_SCHEMA).optionalArg(true).hasArg(true).desc("Schema name to use").type(Long.class).build());
        options.addOption(Option.builder("p").longOpt(OPTION_PARTITIONS).optionalArg(true).hasArg(true).desc("Number of partitions to give topic when creating").type(Long.class).build());
        return options;
    }

    private void createTopic(String topicName, int partitions) {

        final NewTopic newTopic = new NewTopic(topicName, Optional.of(partitions), Optional.empty());
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }
}
