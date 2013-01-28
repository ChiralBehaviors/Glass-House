package com.hellblazer.jmx.rest.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.cascading.CascadingAgent;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;
import com.hellblazer.jmx.rest.service.AggregateService;

public class AggregateServiceImpl implements AggregateService {
    private static Logger      log              = LoggerFactory.getLogger(AggregateServiceImpl.class);
    public static final String ID_REPLACE_REGEX = ",id=\\d+";

    /**
     * Construct the query which selects names with any of the supplied
     * collection of jmx nodes
     * 
     * @param jmxNodes
     * @return
     * @throws MalformedObjectNameException
     */
    public static QueryExp constructNodeQuery(Collection<String> jmxNodes)
                                                                          throws MalformedObjectNameException {
        QueryExp nodeQuery = null;
        // Build up the query for all indicated nodes
        for (String jmxNode : jmxNodes) {
            QueryExp query = getNodeWildcardName(jmxNode);
            if (nodeQuery == null) {
                nodeQuery = query;
            } else {
                nodeQuery = Query.or(nodeQuery, query);
            }
        }
        return nodeQuery;
    }

    /**
     * @param jmxNode
     * @return
     * @throws MalformedObjectNameException
     */
    public static ObjectName getNodeWildcardName(String jmxNode)
                                                                throws MalformedObjectNameException {
        return ObjectName.getInstance(String.format("*:%s=%s,*",
                                                    CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                                    jmxNode));
    }

    private final MBeanServer  mbeanServer;

    private final List<String> mBeanServerNodes = new CopyOnWriteArrayList<String>();

    public AggregateServiceImpl(MBeanServer mBeanServer) {
        mbeanServer = mBeanServer;
    }

    @Override
    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<String> jmxNodes,
                                                             String objectName)
                                                                               throws MalformedObjectNameException,
                                                                               NullPointerException,
                                                                               IntrospectionException,
                                                                               InstanceNotFoundException,
                                                                               ReflectionException {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();

        for (ObjectName n : queryObjectNames(jmxNodes, objectName)) {
            for (MBeanAttributeInfo info : mbeanServer.getMBeanInfo(n).getAttributes()) {
                mBeanAttributeValueJaxBeans.add(getAttributeValueFor(info.getName(),
                                                                     n));
            }
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    @Override
    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName)
                                                                          throws InstanceNotFoundException,
                                                                          MalformedObjectNameException,
                                                                          IntrospectionException,
                                                                          NullPointerException,
                                                                          ReflectionException {
        Set<MBeanAttributeJaxBean> mBeanAttributeJaxBeans = new TreeSet<MBeanAttributeJaxBean>();

        for (ObjectName n : queryObjectNames(jmxNodes, objectName)) {
            for (MBeanAttributeInfo info : mbeanServer.getMBeanInfo(n).getAttributes()) {
                mBeanAttributeJaxBeans.add(new MBeanAttributeJaxBean(uriInfo,
                                                                     info));
            }
        }

        return new MBeanAttributeJaxBeans(mBeanAttributeJaxBeans);
    }

    @Override
    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<String> jmxNodes,
                                                          String objectName,
                                                          String attributeName)
                                                                               throws MalformedObjectNameException {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();

        for (ObjectName n : queryObjectNames(jmxNodes, objectName)) {
            mBeanAttributeValueJaxBeans.add(getAttributeValueFor(attributeName,
                                                                 n));
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.AggregateService#getMbeanServerNodes()
     */
    @Override
    public List<String> getMbeanServerNodes() {
        return mBeanServerNodes;
    }

    @Override
    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo,
                                                    Collection<String> jmxNodes) {
        Set<ObjectName> commonObjectNames = new HashSet<ObjectName>();

        Set<MBeanShortJaxBean> mBeanShortJaxBeans = new TreeSet<MBeanShortJaxBean>();
        for (String jmxNode : jmxNodes) {
            Set<ObjectName> nodeObjectNames;
            try {
                nodeObjectNames = mbeanServer.queryNames(getNodeWildcardName(jmxNode),
                                                         null);
                if (commonObjectNames.isEmpty()) {
                    commonObjectNames.addAll(nodeObjectNames);
                } else {
                    removeObjectNamesWhichDoNotExistOnCurrentNode(commonObjectNames,
                                                                  nodeObjectNames);
                }
            } catch (MalformedObjectNameException | NullPointerException e) {
                log.warn(String.format("Exception getting object names for node %s",
                                       jmxNode));
            }

        }
        for (String objectNameString : getObjectNamesWithoutId(commonObjectNames)) {
            mBeanShortJaxBeans.add(new MBeanShortJaxBean(uriInfo,
                                                         objectNameString));
        }
        return new MBeanShortJaxBeans(mBeanShortJaxBeans);
    }

    @Override
    public Collection<String> getNodes() {
        return mBeanServerNodes;
    }

    @Override
    public Collection<String> getNodesToAggregate(String nodes) {
        if (nodes != null) {
            List<String> nodeList = Arrays.asList(nodes.split(","));
            Collection<String> nodesToCollect = new HashSet<String>();
            for (String jmxNode : mBeanServerNodes) {
                if (nodeList.contains(jmxNode)) {
                    nodesToCollect.add(jmxNode);
                }
            }
            return nodesToCollect;
        }
        return mBeanServerNodes;
    }

    @Override
    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName)
                                                                          throws MalformedObjectNameException,
                                                                          NullPointerException,
                                                                          IntrospectionException,
                                                                          InstanceNotFoundException,
                                                                          ReflectionException {
        Set<MBeanOperationJaxBean> mBeanOperationJaxBeans = new TreeSet<MBeanOperationJaxBean>();

        for (ObjectName n : queryObjectNames(jmxNodes, objectName)) {
            for (MBeanOperationInfo info : mbeanServer.getMBeanInfo(n).getOperations()) {
                mBeanOperationJaxBeans.add(new MBeanOperationJaxBean(uriInfo,
                                                                     info));
            }
        }

        return new MBeanOperationJaxBeans(objectName, mBeanOperationJaxBeans);
    }

    @Override
    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName)
                                                                             throws MalformedObjectNameException,
                                                                             NullPointerException {
        return invokeOperation(jmxNodes, objectName, operationName,
                               new Object[] {}, new String[] {});
    }

    @Override
    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName,
                                                        Object[] params,
                                                        String[] signature)
                                                                           throws MalformedObjectNameException,
                                                                           NullPointerException {
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();

        for (ObjectName n : queryObjectNames(jmxNodes, objectName)) {
            Object returnValue = null;
            String exception = null;
            try {
                returnValue = mbeanServer.invoke(n, operationName, params,
                                                 signature);
            } catch (ReflectionException | MBeanException
                    | InstanceNotFoundException e) {
                exception = e.toString();
            }
            operationReturnValueJaxBeans.add(new OperationReturnValueJaxBean(
                                                                             n.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME),
                                                                             returnValue,
                                                                             exception));
        }

        return new OperationReturnValueJaxBeans(operationReturnValueJaxBeans);
    }

    private MBeanAttributeValueJaxBean getAttributeValueFor(String attributeName,
                                                            ObjectName objectName) {

        Object value = null;
        String exception = null;
        try {
            value = mbeanServer.getAttribute(objectName, attributeName);
        } catch (AttributeNotFoundException | MBeanException
                | ReflectionException | InstanceNotFoundException e) {
            exception = e.toString();
        }
        return new MBeanAttributeValueJaxBean(
                                              attributeName,
                                              objectName.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME),
                                              objectName.getCanonicalName(),
                                              value, exception);
    }

    private Set<String> getObjectNamesWithoutId(Set<ObjectName> objectNames) {
        Set<String> objectNameStrings = new TreeSet<String>();
        for (ObjectName objectName : objectNames) {
            objectNameStrings.add(objectName.toString().replaceFirst(ID_REPLACE_REGEX,
                                                                     ""));
        }
        return objectNameStrings;
    }

    private String parseObjectNameToAggregateMBeansWithMultipleIDs(String objectName) {
        Set<String> objectNames = new TreeSet<String>();
        objectNames.add(objectName);

        String idMatchRegex = ".*?" + ID_REPLACE_REGEX;

        if (objectName.matches(idMatchRegex)) {
            return objectName.replaceFirst(ID_REPLACE_REGEX, "");
        }
        return objectName;
    }

    private Set<ObjectName> queryObjectNames(Collection<String> jmxNodes,
                                             String objectName)
                                                               throws MalformedObjectNameException,
                                                               NullPointerException {
        QueryExp attributeQuery = constructNodeQuery(jmxNodes);

        ObjectName idParsed = ObjectName.getInstance(parseObjectNameToAggregateMBeansWithMultipleIDs(objectName));
        if (idParsed.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME) != null) {
            idParsed = wildcardNodeForm(idParsed);
        }
        return mbeanServer.queryNames(idParsed, attributeQuery);
    }

    private void removeObjectNamesWhichDoNotExistOnCurrentNode(Set<ObjectName> commonObjectNames,
                                                               Set<ObjectName> nodeObjectNames) {
        for (Iterator<ObjectName> it = commonObjectNames.iterator(); it.hasNext();) {
            ObjectName objectName = it.next();
            if (!nodeObjectNames.contains(objectName)) {
                it.remove();
            }
        }
    }

    /**
     * @param nodeX
     * @param sourceName
     * @return
     */
    public static ObjectName wildcardNodeForm(ObjectName sourceName) {
        try {
            final String domain = sourceName.getDomain();
            final String list = sourceName.getKeyPropertyListString();
            final String targetName = String.format("%s:%s=*,%s",
                                                    domain,
                                                    CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                                    list);
            return ObjectName.getInstance(targetName);
        } catch (MalformedObjectNameException x) {
            log.error(String.format("Cannot crreate wild card nod form of source name %s",
                                    sourceName), x);
            throw new IllegalStateException(
                                            String.format("Cannot crreate wild card nod form of source name %s",
                                                          sourceName), x);
        }
    }
}
