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

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXServiceURL;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hellblazer.jmx.rest.service.JMXService;
import com.hellblazer.jmx.rest.util.Address;
import com.hellblazer.jmx.rest.util.JMXServiceURLUtils;
import com.hellblazer.jmx.rest.util.RandomIntRangeGenerator;

public class JMXServiceTest {

    private MBeanServer   mBeanServer;
    private Server        server;
    private JMXService    service       = JMXServiceImpl.getInstance();
    private static int    port          = RandomIntRangeGenerator.getRandomInt(1024,
                                                                               65535);
    private Address       address       = new Address("127.0.0.1", port);

    private JMXServiceURL jmxServiceURL = JMXServiceURLUtils.getJMXServiceURL(address);

    @Before
    public void setUp() throws Exception {
        server = new Server();
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        server.addConnector(new SocketConnector());
        ServletContextHandler context = new ServletContextHandler(server, "/");

        ServletHandler servletHandler = context.getServletHandler();
        createServlets(servletHandler);

        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        server.join();
    }

    // @Test
    public void testGetMBeanAttributeInfo() throws InstanceNotFoundException {
        MBeanAttributeInfo[] attributes = service.getAttributes(jmxServiceURL,
                                                                "foo");
        assertTrue(attributes.length > 1);
    }

    // @Test
    public void testGetMBeanOperationInfo()
                                           throws MalformedObjectNameException,
                                           NullPointerException,
                                           InstanceNotFoundException {
        MBeanOperationInfo[] mBeanOperationInfos = service.getOperations(jmxServiceURL,
                                                                         JETTY_SERVER_MBEAN_NAME);
        assertTrue(mBeanOperationInfos.length > 1);
    }

    // @Test
    public void testGetObjectNames() {
        Set<ObjectName> objectNames = service.getObjectNames(jmxServiceURL);
        assertTrue("At least one ObjectName should be returned",
                   objectNames.size() > 0);
    }

    // @Test
    public void testGetObjectNamesByPrefix() {
        String prefix = "org.eclipse.jetty.servlet:type=servletmapping";
        Set<String> objectNames = service.getObjectNamesByPrefix(jmxServiceURL,
                                                                 prefix);
        assertEquals("Two servlet objectNames starting with \"" + prefix
                     + "\" expected.", 2, objectNames.size());
    }

    @Test
    public void testInvokeOperationWithMultipleParameter() {
        String[] params = new String[] { "javax.management.remote.rmi",
                "FINEST" };

        String[] signature = new String[] { String.class.getName(),
                String.class.getName() };

        service.invoke(jmxServiceURL, "java.util.logging:type=Logging",
                       "setLoggerLevel", params, signature);
    }

    private ServletHandler createServlets(ServletHandler servletHandler) {
        Servlet servlet = new DefaultServlet();
        ServletHolder servletHolder = new ServletHolder(servlet);
        servletHandler.addServletWithMapping(servletHolder, "/");
        servletHandler.addServletWithMapping(servletHolder, "/anotherPath/");
        return servletHandler;
    }
}
