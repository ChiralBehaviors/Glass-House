// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package com.hellblazer.jmx.rest.service.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hellblazer.jmx.cascading.CascadingService;
import com.hellblazer.jmx.rest.service.JMXService;

public class JMXServiceTest {

    private static final String       NODE_NAME = "foo|bar";

    private static MBeanServer        mBeanServer;
    private static JMXService         service;
    private static CascadingService   cascadingService;
    private static JMXConnectorServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        mBeanServer = MBeanServerFactory.newMBeanServer();
        service = new JMXServiceImpl(mBeanServer);
        cascadingService = new CascadingService(mBeanServer);
        InetSocketAddress jmxEndpoint = new InetSocketAddress("localhost",
                                                              allocatePort());
        server = contruct(jmxEndpoint,
                          ManagementFactory.getPlatformMBeanServer());
        server.start();
        cascadingService.mount(server.getAddress().toString(), "*:*", NODE_NAME);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.stop();
    }

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

    @Test
    public void testGetMBeanAttributeInfo() throws Exception {
        MBeanAttributeInfo[] attributes = service.getAttributes(NODE_NAME,
                                                                "foo");
        assertTrue(attributes.length > 1);
    }

    @Test
    public void testGetMBeanOperationInfo() throws Exception {
        MBeanOperationInfo[] mBeanOperationInfos = service.getOperations(NODE_NAME,
                                                                         AggregateServiceTest.JETTY_SERVER_MBEAN);
        assertTrue(mBeanOperationInfos.length > 1);
    }

    @Test
    public void testGetObjectNames() throws Exception {
        Set<ObjectName> objectNames = service.getObjectNames(NODE_NAME);
        assertTrue("At least one ObjectName should be returned",
                   objectNames.size() > 0);
    }

    @Test
    public void testGetObjectNamesByPrefix() throws Exception {
        String prefix = "org.eclipse.jetty.servlet:type=servletmapping";
        Set<String> objectNames = service.getObjectNamesByPrefix(NODE_NAME,
                                                                 prefix);
        assertEquals("Two servlet objectNames starting with \"" + prefix
                     + "\" expected.", 2, objectNames.size());
    }

    @Test
    public void testInvokeOperationWithMultipleParameter() throws Exception {
        String[] params = new String[] { "javax.management.remote.rmi",
                "FINEST" };

        String[] signature = new String[] { String.class.getName(),
                String.class.getName() };

        service.invoke(NODE_NAME, "java.util.logging:type=Logging",
                       "setLoggerLevel", params, signature);
    }
}
