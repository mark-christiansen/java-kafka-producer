package com.jnj.kafka.producer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    @Bean
    @ConfigurationProperties(prefix = "producer")
    public Properties producerProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "admin")
    public Properties adminProperties() {
        return new Properties();
    }

    @Bean
    public KafkaProducer kafkaProducer() {
        return new KafkaProducer(producerProperties());
    }

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(adminProperties());
    }
}
