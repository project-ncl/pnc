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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class SystemConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "system-config";

    private final static Logger log = LoggerFactory.getLogger(SystemConfig.class);

    private final String authenticationProviderId;

    /**
     * number of threads that are taking a build task to be build and starting the building process (their job finishes
     * at starting bpm build, then they go back to grab the next build task)
     */
    private int coordinatorThreadPoolSize;

    private String brewTagPattern;

    /**
     * maximum number of build tasks processed at a time (build tasks that are in progress, regardless of whether they
     * are starting bpm process, being build by executor, etc)
     */
    private int coordinatorMaxConcurrentBuilds;

    private KeycloakClientConfig keycloakServiceAccountConfig;

    private long serviceTokenRefreshIfExpiresInSeconds;

    /**
     * Temporary builds life span set as number of days. After the expiration the temporary builds gets deleted.
     * Defaults to 14 days.
     */
    private int temporaryBuildsLifeSpan;

    private String messageSenderId;

    private int messagingInternalQueueSize;

    /**
     * Kafka or Infinispan. Null or any other results in local "distribution"
     */
    private String distributedEventType;

    private String kafkaBootstrapServers; // list of Kafka bootstrap servers; ; required if distributedEventType is
                                          // "kafka"
    private String kafkaTopic; // the Kafka topic used to distribute events in JSON form; required
    private int kafkaNumOfConsumers; // number of Kafka consumers consuming 'kafkaTopic', default is 1
    private int kafkaNumOfRetries; // number of retries the client will attempt to resend requests, default is 0
    private int kafkaRetryBackoffMillis; // amount of time to wait before attempting to retry a failed request, default
                                         // is 0
    private String kafkaAcks; // The number of acknowledgments the producer requires the leader to have received before
                              // considering a request complete; one of "all", "-1", "0", "1"
    private String kafkaSecurityProtocol; // org.apache.kafka.common.security.auth.SecurityProtocol; one of PLAINTEXT |
                                          // SASL_PLAINTEXT | SASL_SSL | SSL; optional
    private String kafkaSecuritySaslMechanism; // SASL mechanism configuration; optional
    private String kafkaSecuritySaslJaasConf; // JAAS login context parameters for SASL connections; either
                                              // kafkaSecuritySaslJaasConf or (kafkaSecurityUser and
                                              // kafkaSecurityPassword) are required if kafkaSecuritySaslMechanism is
                                              // specified
    private String kafkaSecurityUser; // either kafkaSecuritySaslJaasConf or (kafkaSecurityUser and
                                      // kafkaSecurityPassword) are required if kafkaSecuritySaslMechanism is specified
    private String kafkaSecurityPassword; // either kafkaSecuritySaslJaasConf or (kafkaSecurityUser and
                                          // kafkaSecurityPassword) are required if kafkaSecuritySaslMechanism is
                                          // specified
    private String kafkaProperties; // path to additional Kafka properties; e.g. security, ack, etc; optional
    private String infinispanClusterName; // Infinispan cluster name; required if distributedEventType is "infinispan"
    private String infinispanTransportProperties; // path to Infinispan transport properties; optional

    private final boolean legacyBuildCoordinator;

    public SystemConfig(
            @JsonProperty("authenticationProviderId") String authenticationProviderId,
            @JsonProperty("coordinatorThreadPoolSize") String coordinatorThreadPoolSize,
            @JsonProperty("brewTagPattern") String brewTagPattern,
            @JsonProperty("coordinatorMaxConcurrentBuilds") String coordinatorMaxConcurrentBuilds,
            @JsonProperty("keycloakServiceAccountConfig") KeycloakClientConfig keycloakServiceAccountConfig,
            @JsonProperty("serviceTokenRefreshIfExpiresInSeconds") String serviceTokenRefreshIfExpiresInSeconds,
            @JsonProperty("temporaryBuildsLifeSpan") String temporaryBuildsLifeSpan,
            @JsonProperty("messageSenderId") String messageSenderId,
            @JsonProperty("messagingInternalQueueSize") String messagingInternalQueueSize,
            @JsonProperty("distributedEventType") String distributedEventType,
            @JsonProperty("kafkaBootstrapServers") String kafkaBootstrapServers,
            @JsonProperty("kafkaTopic") String kafkaTopic,
            @JsonProperty("kafkaNumOfConsumers") String kafkaNumOfConsumers,
            @JsonProperty("kafkaNumOfRetries") String kafkaNumOfRetries,
            @JsonProperty("kafkaRetryBackoffMillis") String kafkaRetryBackoffMillis,
            @JsonProperty("kafkaAcks") String kafkaAcks,
            @JsonProperty("kafkaSecurityProtocol") String kafkaSecurityProtocol,
            @JsonProperty("kafkaSecuritySaslMechanism") String kafkaSecuritySaslMechanism,
            @JsonProperty("kafkaSecuritySaslJaasConf") String kafkaSecuritySaslJaasConf,
            @JsonProperty("kafkaSecurityUser") String kafkaSecurityUser,
            @JsonProperty("kafkaSecurityPassword") String kafkaSecurityPassword,
            @JsonProperty("kafkaProperties") String kafkaProperties,
            @JsonProperty("infinispanClusterName") String infinispanClusterName,
            @JsonProperty("infinispanTransportProperties") String infinispanTransportProperties,
            @JsonProperty("legacyBuildCoordinator") String legacyBuildCoordinator) {
        this.authenticationProviderId = authenticationProviderId;
        this.coordinatorThreadPoolSize = toIntWithDefault("coordinatorThreadPoolSize", coordinatorThreadPoolSize, 1);
        this.coordinatorMaxConcurrentBuilds = toIntWithDefault(
                "coordinatorMaxConcurrentBuilds",
                coordinatorMaxConcurrentBuilds,
                10);
        this.brewTagPattern = brewTagPattern;
        this.keycloakServiceAccountConfig = keycloakServiceAccountConfig;
        // 24 hours
        this.serviceTokenRefreshIfExpiresInSeconds = toIntWithDefault(
                "serviceTokenRefreshIfExpiresInSeconds",
                serviceTokenRefreshIfExpiresInSeconds,
                86400);
        this.temporaryBuildsLifeSpan = toIntWithDefault("temporaryBuildsLifeSpan", temporaryBuildsLifeSpan, 14);
        this.messageSenderId = messageSenderId;
        this.messagingInternalQueueSize = toIntWithDefault(
                "messagingInternalQueueSize",
                messagingInternalQueueSize,
                1000);
        this.distributedEventType = distributedEventType;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.kafkaTopic = kafkaTopic;
        this.kafkaNumOfConsumers = toIntWithDefault("kafkaNumOfConsumers", kafkaNumOfConsumers, 1);
        this.kafkaNumOfRetries = toIntWithDefault("kafkaNumOfRetries", kafkaNumOfRetries, 0);
        this.kafkaRetryBackoffMillis = toIntWithDefault("kafkaRetryBackoffMillis", kafkaRetryBackoffMillis, 0);
        this.kafkaAcks = kafkaAcks;
        this.kafkaSecurityProtocol = kafkaSecurityProtocol;
        this.kafkaSecuritySaslMechanism = kafkaSecuritySaslMechanism;
        this.kafkaSecuritySaslJaasConf = kafkaSecuritySaslJaasConf;
        this.kafkaSecurityUser = kafkaSecurityUser;
        this.kafkaSecurityPassword = kafkaSecurityPassword;
        this.kafkaProperties = kafkaProperties;
        this.infinispanClusterName = infinispanClusterName;
        this.infinispanTransportProperties = infinispanTransportProperties;
        this.legacyBuildCoordinator = Boolean.valueOf(legacyBuildCoordinator);
    }

    public static Properties readProperties(String file) {
        try {
            Properties properties = new Properties();
            try (InputStream stream = new FileInputStream(file)) {
                properties.load(stream);
            }
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int getCoordinatorThreadPoolSize() {
        return coordinatorThreadPoolSize;
    }

    public int getCoordinatorMaxConcurrentBuilds() {
        return coordinatorMaxConcurrentBuilds;
    }

    public String getBrewTagPattern() {
        return brewTagPattern;
    }

    public int getTemporaryBuildsLifeSpan() {
        return temporaryBuildsLifeSpan;
    }

    public KeycloakClientConfig getKeycloakServiceAccountConfig() {
        return keycloakServiceAccountConfig;
    }

    public long getServiceTokenRefreshIfExpiresInSeconds() {
        return serviceTokenRefreshIfExpiresInSeconds;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public String getAuthenticationProviderId() {
        return authenticationProviderId;
    }

    public int getMessagingInternalQueueSize() {
        return messagingInternalQueueSize;
    }

    public String getDistributedEventType() {
        return distributedEventType;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public int getKafkaNumOfConsumers() {
        return kafkaNumOfConsumers;
    }

    public int getKafkaNumOfRetries() {
        return kafkaNumOfRetries;
    }

    public int getKafkaRetryBackoffMillis() {
        return kafkaRetryBackoffMillis;
    }

    public String getKafkaAcks() {
        return kafkaAcks;
    }

    public String getKafkaSecurityProtocol() {
        return kafkaSecurityProtocol;
    }

    public String getKafkaSecuritySaslMechanism() {
        return kafkaSecuritySaslMechanism;
    }

    public String getKafkaSecuritySaslJaasConf() {
        return kafkaSecuritySaslJaasConf;
    }

    public String getKafkaSecurityUser() {
        return kafkaSecurityUser;
    }

    public String getKafkaSecurityPassword() {
        return kafkaSecurityPassword;
    }

    public String getKafkaProperties() {
        return kafkaProperties;
    }

    public String getInfinispanClusterName() {
        return infinispanClusterName;
    }

    public String getInfinispanTransportProperties() {
        return infinispanTransportProperties;
    }

    public boolean isLegacyBuildCoordinator() {
        return legacyBuildCoordinator;
    }

    private int toIntWithDefault(String fieldName, String numberAsString, int defaultValue) {
        int result = defaultValue;
        if (numberAsString == null) {
            log.warn("Value in field: " + fieldName + " not set. Will use default value: {}", defaultValue);
        } else {
            try {
                result = Integer.parseInt(numberAsString);
            } catch (NumberFormatException nfe) {
                log.warn(
                        "Invalid value in field: " + fieldName
                                + ". Expected an integer, got: {}. Will use default value: {}",
                        numberAsString,
                        defaultValue,
                        nfe);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SystemConfig{" + "authenticationProviderId='" + authenticationProviderId + '\''
                + ", coordinatorThreadPoolSize=" + coordinatorThreadPoolSize + ", brewTagPattern='" + brewTagPattern
                + '\'' + ", coordinatorMaxConcurrentBuilds=" + coordinatorMaxConcurrentBuilds
                + ", keycloakServiceAccountConfig=" + keycloakServiceAccountConfig
                + ", serviceTokenRefreshIfExpiresInSeconds=" + serviceTokenRefreshIfExpiresInSeconds
                + ", temporaryBuildsLifeSpan=" + temporaryBuildsLifeSpan + ", messageSenderId='" + messageSenderId
                + '\'' + ", messagingInternalQueueSize=" + messagingInternalQueueSize + ", distributedEventType='"
                + distributedEventType + '\'' + ", kafkaBootstrapServers='" + kafkaBootstrapServers + '\''
                + ", kafkaTopic='" + kafkaTopic + '\'' + ", kafkaNumOfConsumers=" + kafkaNumOfConsumers
                + ", kafkaNumOfRetries=" + kafkaNumOfRetries + ", kafkaRetryBackoffMillis=" + kafkaRetryBackoffMillis
                + ", kafkaSecurityProtocol='" + kafkaSecurityProtocol + '\'' + ", kafkaSecuritySaslMechanism='"
                + kafkaSecuritySaslMechanism + '\'' + ", kafkaSecuritySaslJaasConf='" + kafkaSecuritySaslJaasConf + '\''
                + ", kafkaSecurityUser='" + kafkaSecurityUser + '\'' + ", kafkaSecurityPassword='"
                + kafkaSecurityPassword + '\'' + ", kafkaProperties='" + kafkaProperties + '\''
                + ", infinispanClusterName='" + infinispanClusterName + '\'' + ", infinispanTransportProperties='"
                + infinispanTransportProperties + '\'' + '}';
    }

}
