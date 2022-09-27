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
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.scram.ScramLoginModule;
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
        Properties properties;
        String kafkaProperties = config.getKafkaProperties();
        if (kafkaProperties != null) {
            properties = SystemConfig.readProperties(kafkaProperties);
        } else {
            properties = new Properties();
        }

        StringSerializer serializer = new StringSerializer();
        Properties producerProperties = forProducer(config);
        producerProperties.putAll(properties);

        producer = new AsyncProducer<>(producerProperties, serializer, serializer);

        int numOfConsumers = config.getKafkaNumOfConsumers();
        StringDeserializer deserializer = new StringDeserializer();
        Properties consumerProperties = forConsumer(config);
        consumerProperties.putAll(properties);

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

    public Properties forConsumer(SystemConfig config) {

        // Each consumer has its own UNIQUE group, so they all consume all messages.
        // If possible, use repeatable (but UNIQUE) string, e.g. for a restarted node.
        String groupId = UUID.randomUUID().toString(); // we use UUID for now, should be OK

        Properties consumerProps = baseProperties(config);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return consumerProps;
    }

    public Properties forProducer(SystemConfig config) {
        Properties producerProps = baseProperties(config);

        producerProps.put(CommonClientConfigs.RETRIES_CONFIG, config.getKafkaNumOfRetries());
        producerProps.put(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG, config.getKafkaRetryBackoffMillis());

        return producerProps;
    }

    private Properties baseProperties(SystemConfig config) {

        Properties baseProperties = new Properties();
        String bootstrapServers = Objects.requireNonNull(config.getKafkaBootstrapServers());

        baseProperties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        baseProperties.putAll(configureSecurity(config));

        return baseProperties;
    }

    private Properties configureSecurity(SystemConfig config) {
        Properties securityProperties = new Properties();

        if (config.getKafkaSecurityProtocol() == null) {
            return securityProperties;
        }

        if (config.getKafkaSecurityProtocol() != null) {
            securityProperties.put(
                    CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                    SecurityProtocol.forName(config.getKafkaSecurityProtocol()));
        }

        if (isSasl(SecurityProtocol.forName(config.getKafkaSecurityProtocol()))) {
            if (config.getKafkaSecurityUser() != null && config.getKafkaSecurityPassword() != null) {

                securityProperties.put(SaslConfigs.SASL_MECHANISM, config.getKafkaSecuritySaslMechanism());
                securityProperties.put(
                        SaslConfigs.SASL_JAAS_CONFIG,
                        String.format(
                                "%s required username='%s' password='%s';",
                                getLoginModule(config.getKafkaSecuritySaslMechanism()).getName(),
                                config.getKafkaSecurityUser(),
                                config.getKafkaSecurityPassword()));

            } else if (config.getKafkaSecuritySaslJaasConf() != null) {

                securityProperties.put(SaslConfigs.SASL_MECHANISM, config.getKafkaSecuritySaslMechanism());
                securityProperties.put(SaslConfigs.SASL_JAAS_CONFIG, config.getKafkaSecuritySaslJaasConf());
            }
        }

        return securityProperties;
    }

    private boolean isSasl(SecurityProtocol securityProtocol) {
        return securityProtocol == SecurityProtocol.SASL_PLAINTEXT || securityProtocol == SecurityProtocol.SASL_SSL;
    }

    private Class getLoginModule(String saslMechanism) {
        switch (saslMechanism.toUpperCase()) {
            case "PLAIN":
                return PlainLoginModule.class;
            case "SCRAM-SHA-256":
            case "SCRAM-SHA-512":
                return ScramLoginModule.class;
            default:
                throw new IllegalArgumentException("Unsupported SASL mechanism " + saslMechanism);
        }
    }
}
