package com.hellblazer.jmx.rest.service;

import java.util.Collection;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;

public interface AggregateService {
    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<String> jmxNodes,
                                                             String objectName)
                                                                               throws MalformedObjectNameException,
                                                                               NullPointerException,
                                                                               IntrospectionException,
                                                                               InstanceNotFoundException,
                                                                               ReflectionException;

    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName)
                                                                          throws InstanceNotFoundException,
                                                                          MalformedObjectNameException, IntrospectionException, NullPointerException, ReflectionException;

    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<String> jmxNodes,
                                                          String objectName,
                                                          String attributeName)
                                                                               throws MalformedObjectNameException;

    public List<String> getMbeanServerNodes();

    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo,
                                                    Collection<String> jmxNodes);

    public Collection<String> getNodes();

    public abstract Collection<String> getNodesToAggregate(String nodes);

    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName) throws MalformedObjectNameException, NullPointerException, IntrospectionException, InstanceNotFoundException, ReflectionException;

    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName)
                                                                             throws MalformedObjectNameException,
                                                                             NullPointerException;

    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName,
                                                        Object[] params,
                                                        String[] signature)
                                                                           throws MalformedObjectNameException,
                                                                           NullPointerException;
}
