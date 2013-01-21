package com.hellblazer.jmx.rest.service;

import java.util.Collection;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.jmx.rest.domain.JMXNode;
import com.hellblazer.jmx.rest.domain.jaxb.NodeJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;

public interface AggregateService {
    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<JMXNode> jmxNodes,
                                                             String objectName)
                                                                               throws InstanceNotFoundException;

    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                        Collection<JMXNode> jmxNodes,
                                                        String objectName)
                                                                          throws InstanceNotFoundException;

    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<JMXNode> jmxNodes,
                                                          String objectName,
                                                          String attributeName)
                                                                               throws InstanceNotFoundException;

    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo,
                                                    Collection<JMXNode> jmxNodes);

    public Set<NodeJaxBean> getNodes();

    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                        Collection<JMXNode> jmxNodes,
                                                        String objectName)
                                                                          throws InstanceNotFoundException;

    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes,
                                                        String objectName,
                                                        String operationName);

    public OperationReturnValueJaxBeans invokeOperation(Collection<JMXNode> jmxNodes,
                                                        String objectName,
                                                        String operationName,
                                                        Object[] params,
                                                        String[] signature);
}
