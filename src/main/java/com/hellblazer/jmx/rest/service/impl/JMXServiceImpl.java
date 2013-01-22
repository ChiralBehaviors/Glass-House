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

import java.util.HashSet;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.cascading.CascadingAgent;
import com.hellblazer.jmx.rest.service.JMXService;

public class JMXServiceImpl implements JMXService {
    private static final Logger log                                       = LoggerFactory.getLogger(JMXServiceImpl.class);

    public static final String  MEMORY_MXBEAN                             = "java.lang:type=Memory";
    public static final String  THREADING_MXBEAN                          = "java.lang:type=Threading";
    public static final String  LOGGING_MBEAN                             = "java.util.logging:type=logging";
    public static final String  JETTY_SERVER_MBEAN                        = "org.eclipse.jetty.server:type=server,id=0";
    public static final String  JETTY_SERVER_MBEANID1                     = "org.eclipse.jetty.server:type=server,id=1";
    public static final String  MEMORY_MXBEAN_HEAP                        = "HeapMemoryUsage";
    public static final String  MEMORY_MXBEAN_NONHEAP                     = "NonHeapMemoryUsage";
    public static final String  MEMORY_MXBEAN_OBJECT_PENDING_FINALIZATION = "ObjectPendingFinalizationCount";
    public static final String  MEMORY_MXBEAN_VERBOSE                     = "Verbose";

    private static JMXService   jmxService;

    public static JMXService getInstance() {
        if (jmxService == null) {
            jmxService = new JMXServiceImpl();
        }
        return jmxService;
    }

    private final MBeanServer mbeanServer;

    private JMXServiceImpl() {
        mbeanServer = MBeanServerFactory.createMBeanServer();
    }

    public Object getAttribute(String jmxNode, String objectName,
                               String attribute)
                                                throws InstanceNotFoundException,
                                                AttributeNotFoundException,
                                                MalformedObjectNameException,
                                                MBeanException,
                                                ReflectionException {
        return mbeanServer.getAttribute(inject(jmxNode, objectName), attribute);

    }

    public MBeanAttributeInfo[] getAttributes(String jmxNode, String objectName)
                                                                                throws InstanceNotFoundException,
                                                                                IntrospectionException,
                                                                                MalformedObjectNameException,
                                                                                ReflectionException {
        return mbeanServer.getMBeanInfo(inject(jmxNode, objectName)).getAttributes();
    }

    public Set<ObjectName> getObjectNames(String jmxNode)
                                                         throws MalformedObjectNameException,
                                                         NullPointerException {
        ObjectName targetQuery = ObjectName.getInstance(String.format("*:%s=%s",
                                                                      CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                                                      jmxNode));
        return mbeanServer.queryNames(targetQuery, null);
    }

    public Set<String> getObjectNamesByPrefix(String jmxNode, String prefix)
                                                                            throws MalformedObjectNameException,
                                                                            NullPointerException {
        Set<ObjectName> objectNames = getObjectNames(jmxNode);
        Set<String> filteredObjectNames = new HashSet<String>();
        for (ObjectName objectName : objectNames) {
            String canonicalName = objectName.toString();
            if (canonicalName.startsWith(prefix)) {
                filteredObjectNames.add(objectName.toString());
            }
        }
        return filteredObjectNames;
    }

    public MBeanOperationInfo[] getOperations(String jmxNode, String objectName)
                                                                                throws InstanceNotFoundException,
                                                                                IntrospectionException,
                                                                                MalformedObjectNameException,
                                                                                ReflectionException {
        return mbeanServer.getMBeanInfo(inject(jmxNode, objectName)).getOperations();
    }

    public Object invoke(String jmxNode, String objectName,
                         String operationName, Object[] params,
                         String[] signature)
                                            throws MalformedObjectNameException,
                                            InstanceNotFoundException,
                                            ReflectionException, MBeanException {
        ObjectName targetName = inject(jmxNode, objectName);
        log.debug(String.format("invoke: target objectName: %s, operationName: %s",
                                targetName, operationName));
        return mbeanServer.invoke(targetName, operationName, params, signature);

    }

    public ObjectName inject(String jmxNode, String objectName)
                                                               throws MalformedObjectNameException {
        ObjectName original = ObjectName.getInstance(objectName);
        String target = String.format("%s:%s=%s,%s",
                                      original.getDomain(),
                                      CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                      jmxNode,
                                      original.getKeyPropertyListString());
        ;
        return ObjectName.getInstance(target);
    }
}
