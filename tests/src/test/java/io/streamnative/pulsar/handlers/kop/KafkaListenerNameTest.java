/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamnative.pulsar.handlers.kop;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.pulsar.broker.ServiceConfigurationUtils;
import org.testng.annotations.Test;

/**
 * Test for kafkaListenerName config.
 */
@Slf4j
public class KafkaListenerNameTest extends KopProtocolHandlerTestBase {

    @Override
    protected void setup() throws Exception {
        // Set up in the test method
    }

    @Override
    protected void cleanup() throws Exception {
        // Clean up in the test method
    }

    @Test(timeOut = 30000)
    public void testListenerName() throws Exception {
        super.resetConfig();
        conf.setAdvertisedAddress(null);
        final String localAddress = ServiceConfigurationUtils.getDefaultOrConfiguredAddress(null);
        conf.setInternalListenerName("pulsar");
        final String advertisedListeners =
                "pulsar:pulsar://" + localAddress + ":" + brokerPort
                        + ",kafka:pulsar://" + "localhost:" + kafkaBrokerPort;
        conf.setAdvertisedListeners(advertisedListeners);
        conf.setKafkaListenerName("kafka");
        log.info("Set advertisedListeners to {}", advertisedListeners);
        super.internalSetup();

        final KafkaProducer<String, String> producer = new KafkaProducer<>(newKafkaProducerProperties());
        producer.send(new ProducerRecord<>("my-topic", "my-message"), (metadata, exception) -> {
            if (exception == null) {
                log.info("Send to {}", metadata);
            } else {
                log.error("Send failed: {}", exception.getMessage());
            }
        });
        producer.close();

        super.internalCleanup();
    }
}
