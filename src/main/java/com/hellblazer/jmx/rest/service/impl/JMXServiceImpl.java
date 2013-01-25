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
    private static final Logger log = LoggerFactory.getLogger(JMXServiceImpl.class);

    private final MBeanServer   mbeanServer;

    public JMXServiceImpl() {
        this(MBeanServerFactory.createMBeanServer());
    }

    public JMXServiceImpl(MBeanServer mBeanServer) {
        mbeanServer = mBeanServer;
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
        ObjectName targetQuery = ObjectName.getInstance(String.format("*:%s=%s,*",
                                                                      CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                                                      jmxNode));
        return mbeanServer.queryNames(targetQuery, null);
    }

    public Set<String> getObjectNamesByPrefix(String jmxNode, String prefix)
                                                                            throws MalformedObjectNameException,
                                                                            NullPointerException {
        prefix = inject(jmxNode, prefix).getCanonicalName();
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
        if (log.isDebugEnabled()) {
            log.debug(String.format("invoke: target objectName: %s, operationName: %s",
                                    targetName, operationName));
        }
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
