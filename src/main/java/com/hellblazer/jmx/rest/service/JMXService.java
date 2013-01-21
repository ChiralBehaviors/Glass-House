package com.hellblazer.jmx.rest.service;

import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

public interface JMXService {

    public abstract Object getAttribute(JMXServiceURL jmxServiceURL,
                                        String objectName, String attributeName)
                                                                                throws InstanceNotFoundException;

    public abstract MBeanAttributeInfo[] getAttributes(JMXServiceURL jmxServiceURL,
                                                       String objectName)
                                                                         throws InstanceNotFoundException;

    public abstract Set<ObjectName> getObjectNames(JMXServiceURL jmxServiceURL);

    public abstract Set<String> getObjectNamesByPrefix(JMXServiceURL jmxServiceURL,
                                                       String prefix);

    public abstract MBeanOperationInfo[] getOperations(JMXServiceURL jmxServiceURL,
                                                       String objectName)
                                                                         throws InstanceNotFoundException;

    public abstract Object invoke(JMXServiceURL jmxServiceURL,
                                  String objectName, String operationName,
                                  Object[] params, String[] signature);

}