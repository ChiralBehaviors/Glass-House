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

package com.hellblazer.glassHouserest.service.impl;

import static com.hellblazer.jmx.cascading.proxy.ProxyCascadingAgent.getTargetName;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.hellblazer.glassHouse.rest.service.impl.JmxServiceImpl;
import com.hellblazer.jmx.cascading.CascadingService;

public class JmxServiceFunctionalTest {
    private static final Logger log = LoggerFactory
	    .getLogger(JmxServiceFunctionalTest.class);

    public static final String ATTRIBUTE_1 = "Attribute1";
    public static final String ATTRIBUTE_2 = "Attribute2";
    public static final String OPERATION_1 = "Operation1";
    public static final String OPERATION_2 = "Operation1";
    public static final String TEST_1_BEAN = "mydomain:type=Test1";
    public static final String TEST_2_BEAN = "mydomain:type=Test2";

    private static int allocatePort() {
	InetSocketAddress address = new InetSocketAddress("127.0.0.1", 0);
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
	    MBeanServer mbs) throws IOException {
	System.setProperty("java.rmi.server.randomIDs", "true");

	Map<String, Object> env = new HashMap<String, Object>();
	JMXServiceURL url = new JMXServiceURL("rmi", jmxEndpoint.getHostName(),
		jmxEndpoint.getPort());
	JMXConnectorServer connectorServer = JMXConnectorServerFactory
		.newJMXConnectorServer(url, env, mbs);
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    return null;
	}
	return connectorServer;
    }

    private JmxService jmxService;
    private CascadingService cascadingService;
    private MBeanServer mBeanServer;
    private static MBeanServer mbs;
    private static JMXConnectorServer server;
    private UriInfo uriInfo;
    private UriBuilder uriBuilder;

    private static String node1;

    @BeforeClass
    public static void initialize() throws IOException, InterruptedException,
	    InstanceAlreadyExistsException, MBeanRegistrationException,
	    NotCompliantMBeanException, MalformedObjectNameException,
	    NullPointerException {

	mbs = MBeanServerFactory.newMBeanServer();

	int port1 = allocatePort();
	InetSocketAddress jmxEndpoint = new InetSocketAddress("127.0.0.1",
		port1);
	server = contruct(jmxEndpoint, mbs);
	server.start();

	Thread.sleep(2000);

	assertTrue(server.isActive());

	node1 = String.format("%s|%s", "127.0.0.1", port1);

	log.trace(String.format("Node 1 label: %s", node1));
	Test1 test1 = new Test1();
	test1.setAttribute1(1);
	test1.setAttribute2(2);

	Test2 test2 = new Test2();
	test2.setAttribute1(1);
	test2.setAttribute2(2);

	mbs.registerMBean(test1, ObjectName.getInstance(TEST_1_BEAN));
	mbs.registerMBean(test2, ObjectName.getInstance(TEST_2_BEAN));
    }

    @Before
    public void setUp() throws Exception {
	mBeanServer = MBeanServerFactory.newMBeanServer();
	cascadingService = new CascadingService(mBeanServer);
	cascadingService.mount(server.getAddress().toString(), "*:*", node1);

	jmxService = new JmxServiceImpl(mBeanServer);

	uriInfo = mock(UriInfo.class);
	uriBuilder = mock(UriBuilder.class);

	when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
	when(uriBuilder.path(any(String.class))).thenReturn(uriBuilder);
	when(uriBuilder.build()).thenReturn(new URI("http://testuri/test"));
    }

    @Test
    public void testGetAllAttributeValues() throws Exception {
	MBeanAttributeValueJaxBeans attributes = jmxService
		.getAllAttributeValues(getTargetName(node1, TEST_1_BEAN)
			.getCanonicalName());

	assertEquals("expected 2 values", 2,
		attributes.mBeanAttributeValueJaxBeans.size());
    }

    @Test
    public void testGetAttributesMetaData() throws Exception {
	MBeanAttributeJaxBeans mBeanAttributesInfoJaxBean = jmxService
		.getAttributesMetaData(uriInfo,
			getTargetName(node1, TEST_1_BEAN).getCanonicalName());

	assertEquals(2,
		mBeanAttributesInfoJaxBean.mBeanAttributeJaxBeans.size());
    }

    @Test
    public void testGetAttributeValues() throws Exception {
	MBeanAttributeValueJaxBean mBeanAttributeValueJaxBean = jmxService
		.getAttributeValue(getTargetName(node1, TEST_1_BEAN)
			.getCanonicalName(), ATTRIBUTE_1);
	assertNotNull(mBeanAttributeValueJaxBean);
	assertEquals(Integer.toString(1), mBeanAttributeValueJaxBean.value);
    }

    @Test
    public void testGetObjectNames() throws Exception {
	MBeanShortJaxBeans mBeanShortJaxBeans = jmxService
		.getMBeanShortJaxBeans(uriInfo);

	assertEquals("Expected to get two common mBeanShortJaxBeans", 2,
		mBeanShortJaxBeans.mbeans.size());
    }

    @Test
    public void testInvokeOperation() throws Exception {
	OperationReturnValueJaxBean value = jmxService.invokeOperation(
		getTargetName(node1, TEST_1_BEAN).getCanonicalName(),
		"operation1");
	assertEquals(Integer.toString(1), value.returnValue);
    }

    @Test
    public void testInvokeOperationWithParameters() throws Exception {
	String value = "Foo Me, Baby";

	String[] params = new String[] { value };

	String[] signature = new String[] { String.class.getCanonicalName() };

	OperationReturnValueJaxBean bean = jmxService.invokeOperation(
		getTargetName(node1, TEST_2_BEAN).getCanonicalName(),
		"operationFoo", params, signature);
	assertEquals(value, bean.returnValue);
    }
}
