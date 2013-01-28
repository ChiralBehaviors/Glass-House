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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;

import com.hellblazer.jmx.cascading.CascadingService;
import com.hellblazer.jmx.rest.AbstractMockitoTest;

public class AggregateServiceFunctionalTest extends AbstractMockitoTest {

    public static final String MEMORY_MXBEAN                             = "java.lang:type=Memory";
    public static final String THREADING_MXBEAN                          = "java.lang:type=Threading";
    public static final String MEMORY_MXBEAN_HEAP                        = "HeapMemoryUsage";
    public static final String MEMORY_MXBEAN_NONHEAP                     = "NonHeapMemoryUsage";
    public static final String MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION = "ObjectPendingFinalizationCount";
    public static final String MEMORY_MXBEAN_VERBOSE                     = "Verbose";

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

    private MBeanServer        mBeanServer;
    private MBeanServer        mbs1;
    private MBeanServer        mbs2;
    private MBeanServer        mbs3;
    private CascadingService   cascadingService;
    private JMXConnectorServer server1;
    private JMXConnectorServer server2;
    private JMXConnectorServer server3;

    @Before
    public void setUp() throws Exception {
        mBeanServer = MBeanServerFactory.newMBeanServer();

        mbs1 = MBeanServerFactory.newMBeanServer();
        cascadingService = new CascadingService(mBeanServer);
        int node1 = allocatePort();
        InetSocketAddress jmxEndpoint = new InetSocketAddress("localhost",
                                                              node1);
        server1 = contruct(jmxEndpoint,
                           ManagementFactory.getPlatformMBeanServer());
        server1.start();

        int node2 = allocatePort();
        jmxEndpoint = new InetSocketAddress("localhost", node2);
        server2 = contruct(jmxEndpoint,
                           ManagementFactory.getPlatformMBeanServer());
        server2.start();

        int node3 = allocatePort();
        jmxEndpoint = new InetSocketAddress("localhost", node3);
        server3 = contruct(jmxEndpoint,
                           ManagementFactory.getPlatformMBeanServer());
        server3.start();

        cascadingService.mount(server1.getAddress().toString(), "*:*",
                               String.format("%s|%s", "localhost", node1));
        cascadingService.mount(server1.getAddress().toString(), "*:*",
                               String.format("%s|%s", "localhost", node2));
        cascadingService.mount(server1.getAddress().toString(), "*:*",
                               String.format("%s|%s", "localhost", node3));
    }

    @After
    public void tearDown() throws Exception {
        server1.stop();
        server2.stop();
        server3.stop();
    }
}
