/** 
 * (C) Copyright 2012 Hal Hildebrand, All Rights Reserved
 * 
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

package com.hellblazer.jmx.rest.service.impl;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hellblazer.jmx.cascading.CascadingService;
import com.hellblazer.jmx.rest.AbstractMockitoTest;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.service.AggregateService;

public class AggregateServiceFunctionalTest extends AbstractMockitoTest {

    public static final String ATTRIBUTE_1 = "Attribute1";
    public static final String ATTRIBUTE_2 = "Attribute2";
    public static final String OPERATION_1 = "Operation1";
    public static final String OPERATION_2 = "Operation1";
    public static final String TEST_1_BEAN = "mydomain:type=Test1";
    public static final String TEST_2_BEAN = "mydomain:type=Test2";

    private static int allocatePort() {
        InetSocketAddress address = new InetSocketAddress("localhost", 0);
        ServerSocket socket = null;
        try {
            socket = new ServerSocket();
            socket.bind(address);
            return socket.getLocalPort();
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;
    }

    private static JMXConnectorServer contruct(InetSocketAddress jmxEndpoint,
                                               MBeanServer mbs)
                                                               throws IOException {
        System.setProperty("java.rmi.server.randomIDs", "true");

        Map<String, Object> env = new HashMap<String, Object>();
        JMXServiceURL url = new JMXServiceURL("rmi", jmxEndpoint.getHostName(),
                                              jmxEndpoint.getPort());
        return JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
    }

    private AggregateService   aggregateService;
    private CascadingService   cascadingService;
    private Set<String>        jmxNodes;
    private MBeanServer        mBeanServer;
    private MBeanServer        mbs1;
    private MBeanServer        mbs2;
    private MBeanServer        mbs3;
    private String             node1;
    private String             node2;
    private String             node3;
    private JMXConnectorServer server1;
    private JMXConnectorServer server2;
    private JMXConnectorServer server3;

    @Before
    public void setUp() throws Exception {
        mBeanServer = MBeanServerFactory.newMBeanServer();

        mbs1 = MBeanServerFactory.newMBeanServer();
        mbs2 = MBeanServerFactory.newMBeanServer();
        mbs3 = MBeanServerFactory.newMBeanServer();

        cascadingService = new CascadingService(mBeanServer);
        int port1 = allocatePort();
        InetSocketAddress jmxEndpoint = new InetSocketAddress("localhost",
                                                              port1);
        server1 = contruct(jmxEndpoint, mbs1);
        server1.start();

        int port2 = allocatePort();
        jmxEndpoint = new InetSocketAddress("localhost", port2);
        server2 = contruct(jmxEndpoint, mbs2);
        server2.start();

        int port3 = allocatePort();
        jmxEndpoint = new InetSocketAddress("localhost", port3);
        server3 = contruct(jmxEndpoint, mbs3);
        server3.start();

        node1 = String.format("%s|%s", "localhost", port1);
        node2 = String.format("%s|%s", "localhost", port2);
        node3 = String.format("%s|%s", "localhost", port3);

        jmxNodes = new HashSet<String>();
        jmxNodes.add(node1);
        jmxNodes.add(node2);
        jmxNodes.add(node3);

        cascadingService.mount(server1.getAddress().toString(), "*:*", node1);
        cascadingService.mount(server1.getAddress().toString(), "*:*", node2);
        cascadingService.mount(server1.getAddress().toString(), "*:*", node3);

        aggregateService = new AggregateServiceImpl(mBeanServer);
    }

    @After
    public void tearDown() throws Exception {
        server1.stop();
        server2.stop();
        server3.stop();
    }

    @Test
    public void testGetAllAttributeValues() throws Exception {
        Test1 test1 = new Test1();
        test1.setAttribute1(1);
        test1.setAttribute2(2);

        Test2 test2 = new Test2();
        test2.setAttribute1(1);
        test2.setAttribute2(2);

        mbs1.registerMBean(test1, ObjectName.getInstance(TEST_1_BEAN));
        mbs1.registerMBean(test2, ObjectName.getInstance(TEST_2_BEAN));

        mbs2.registerMBean(test1, ObjectName.getInstance(TEST_1_BEAN));
        mbs2.registerMBean(test2, ObjectName.getInstance(TEST_2_BEAN));

        mbs3.registerMBean(test1, ObjectName.getInstance(TEST_1_BEAN));
        mbs3.registerMBean(test2, ObjectName.getInstance(TEST_2_BEAN));

        Thread.sleep(1000);

        MBeanAttributeValueJaxBeans attributes = aggregateService.getAllAttributeValues(jmxNodes,
                                                                                        TEST_1_BEAN);

        assertEquals("expected 2 values for each node", 6,
                     attributes.mBeanAttributeValueJaxBeans.size());
    }
}
