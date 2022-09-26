/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.notification.dist;

import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import io.apicurio.registry.utils.IoUtil;
import io.apicurio.registry.utils.kafka.AsyncProducer;
import io.apicurio.registry.utils.kafka.ConsumerContainer;
import io.apicurio.registry.utils.kafka.ConsumerSkipRecordsSerializationExceptionHandler;
import io.apicurio.registry.utils.kafka.Oneof2;
import io.apicurio.registry.utils.kafka.ProducerActions;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

public class KafkaDistributedEventHandler extends AbstractDistributedEventHandler {
    private ProducerActions<String, String> producer;
    private ConsumerContainer.DynamicPool<String, String> consumer;

    private final SystemConfig config;
    private final String topic;

    public KafkaDistributedEventHandler(SystemConfig config) {
        this.config = Objects.requireNonNull(config);
        this.topic = Objects.requireNonNull(config.getKafkaTopic());
    }

    @Override
    public void sendEvent(Object event) {
        producer.apply(new ProducerRecord<>(topic, toMessage(event)));
    }

    @Override
    public void start() {
        String bootstrapServers = Objects.requireNonNull(config.getKafkaBootstrapServers());
        int numOfConsumers = config.getKafkaNumOfConsumers();
        // Each consumer has its own UNIQUE group, so they all consume all messages.
        // If possible, use repeatable (but UNIQUE) string, e.g. for a restarted node.
        String groupId = UUID.randomUUID().toString(); // we use UUID for now, should be OK

        Properties properties;
        String kafkaProperties = config.getKafkaProperties();
        if (kafkaProperties != null) {
            properties = SystemConfig.readProperties(kafkaProperties);
        } else {
            properties = new Properties();
        }

        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        StringSerializer serializer = new StringSerializer();
        Properties producerProperties = new Properties();
        producerProperties.putAll(properties);
        producer = new AsyncProducer<>(producerProperties, serializer, serializer);

        Properties consumerProperties = new Properties();
        consumerProperties.putAll(properties);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        StringDeserializer deserializer = new StringDeserializer();
        consumer = new ConsumerContainer.DynamicPool<>(
                consumerProperties,
                deserializer,
                deserializer,
                topic,
                numOfConsumers,
                Oneof2.first(this::consume),
                new ConsumerSkipRecordsSerializationExceptionHandler());
    }

    private void consume(ConsumerRecord<String, String> cr) {
        sendMessage(cr.value());
    }

    @Override
    public void close() {
        IoUtil.closeIgnore(producer);
        IoUtil.closeIgnore(consumer);
    }
}
