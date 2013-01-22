package com.hellblazer.jmx.rest.service;

import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public interface JMXService {

    public abstract Object getAttribute(String jmxNode, String objectName,
                                        String attributeName)
                                                             throws InstanceNotFoundException,
                                                             AttributeNotFoundException,
                                                             MalformedObjectNameException,
                                                             MBeanException,
                                                             ReflectionException;

    public abstract MBeanAttributeInfo[] getAttributes(String jmxNode,
                                                       String objectName)
                                                                         throws InstanceNotFoundException,
                                                                         IntrospectionException,
                                                                         MalformedObjectNameException,
                                                                         ReflectionException;

    public abstract Set<ObjectName> getObjectNames(String jmxNode)
                                                                  throws MalformedObjectNameException,
                                                                  NullPointerException;

    public abstract Set<String> getObjectNamesByPrefix(String jmxNode,
                                                       String prefix)
                                                                     throws MalformedObjectNameException,
                                                                     NullPointerException;

    public abstract MBeanOperationInfo[] getOperations(String jmxNode,
                                                       String objectName)
                                                                         throws InstanceNotFoundException,
                                                                         IntrospectionException,
                                                                         MalformedObjectNameException,
                                                                         ReflectionException;

    public abstract Object invoke(String jmxNode, String objectName,
                                  String operationName, Object[] params,
                                  String[] signature)
                                                     throws MalformedObjectNameException,
                                                     InstanceNotFoundException,
                                                     ReflectionException,
                                                     MBeanException;

}