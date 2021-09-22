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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.InetAddress;
import java.util.Map;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.testng.annotations.Test;

/**
 * Tests for EndPoint.
 */
public class EndPointTest {

    @Test
    public void testValidListener() throws Exception {

        String kafkaProtocolMap = "PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL";
        Map<String, SecurityProtocol> protocolMap = EndPoint.parseProtocolMap(kafkaProtocolMap);

        EndPoint endPoint = new EndPoint("PLAINTEXT://192.168.0.1:9092", protocolMap);
        assertEquals(endPoint.getSecurityProtocol(), SecurityProtocol.PLAINTEXT);
        assertEquals(endPoint.getHostname(), "192.168.0.1");
        assertEquals(endPoint.getPort(), 9092);

        final String localhost = InetAddress.getLocalHost().getCanonicalHostName();
        endPoint = new EndPoint("SSL://:9094",protocolMap);
        assertEquals(endPoint.getSecurityProtocol(), SecurityProtocol.SSL);
        assertEquals(endPoint.getHostname(), localhost);
        assertEquals(endPoint.getPort(), 9094);
    }

    @Test
    public void testValidListeners() throws Exception {
        String kafkaProtocolMap = "PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL";
        final  Map<String, EndPoint> endPointMap =
                EndPoint.parseListeners("PLAINTEXT://localhost:9092,SSL://:9093", kafkaProtocolMap);
        assertEquals(endPointMap.size(), 2);

        final EndPoint plainEndPoint = endPointMap.get("PLAINTEXT");
        assertNotNull(plainEndPoint);
        assertEquals(plainEndPoint.getSecurityProtocol(), SecurityProtocol.PLAINTEXT);
        assertEquals(plainEndPoint.getHostname(), "localhost");
        assertEquals(plainEndPoint.getPort(), 9092);

        final EndPoint sslEndPoint = endPointMap.get("SSL");
        final String localhost = InetAddress.getLocalHost().getCanonicalHostName();
        assertNotNull(sslEndPoint);
        assertEquals(sslEndPoint.getSecurityProtocol(), SecurityProtocol.SSL);
        assertEquals(sslEndPoint.getHostname(), localhost);
        assertEquals(sslEndPoint.getPort(), 9093);
    }

    @Test
    public void testMultiListeners() throws Exception {
        String kafkaProtocolMap = "EXTERNAL:PLAINTEXT,INTERNAL:PLAINTEXT";

        Map<String, EndPoint>  endPointMap =  EndPoint.parseListeners("EXTERNAL://localhost:9092,INTERNAL://localhost:9094", kafkaProtocolMap);
        final EndPoint externalEndPoint = endPointMap.get("EXTERNAL");
        assertNotNull(externalEndPoint);
        assertEquals(externalEndPoint.getSecurityProtocol(), SecurityProtocol.PLAINTEXT);
        assertEquals(externalEndPoint.getHostname(), "localhost");
        assertEquals(externalEndPoint.getPort(), 9092);

        final EndPoint internalEndPoint = endPointMap.get("INTERNAL");
        final String localhost = InetAddress.getLocalHost().getCanonicalHostName();
        assertNotNull(internalEndPoint);
        assertEquals(internalEndPoint.getSecurityProtocol(), SecurityProtocol.PLAINTEXT);
        assertEquals(internalEndPoint.getHostname(), "localhost");
        assertEquals(internalEndPoint.getPort(), 9094);
    }

    @Test
    public void testGetEndPoint() {
        String kafkaProtocolMap = "PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL";
        Map<String, SecurityProtocol> protocolMap = EndPoint.parseProtocolMap(kafkaProtocolMap);

        String listeners = "PLAINTEXT://:9092,SSL://:9093,SASL_SSL://:9094,SASL_PLAINTEXT:9095";
        assertEquals(EndPoint.getPlainTextEndPoint(listeners, protocolMap).getPort(), 9092);
        assertEquals(EndPoint.getSslEndPoint(listeners, protocolMap).getPort(), 9093);
        listeners = "SASL_PLAINTEXT://:9095,SASL_SSL://:9094,SSL://:9093,PLAINTEXT:9092";
        assertEquals(EndPoint.getPlainTextEndPoint(listeners, protocolMap).getPort(), 9095);
        assertEquals(EndPoint.getSslEndPoint(listeners, protocolMap).getPort(), 9094);

        try {
            EndPoint.getPlainTextEndPoint("SSL://:9093", protocolMap);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has no plain text endpoint"));
        }

        try {
            EndPoint.getSslEndPoint("PLAINTEXT://:9092", protocolMap);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has no ssl endpoint"));
        }
    }

    @Test
    public void testOriginalUrl() throws Exception {
        String kafkaProtocolMap = "PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL";
        Map<String, SecurityProtocol> protocolMap = EndPoint.parseProtocolMap(kafkaProtocolMap);

        final EndPoint endPoint = new EndPoint("PLAINTEXT://:9092", protocolMap);
        assertEquals(endPoint.getHostname(), InetAddress.getLocalHost().getCanonicalHostName());
        assertEquals(endPoint.getOriginalListener(), "PLAINTEXT://:9092");
    }
}
