/** 
 * Portions copyright (C) 2013 Hal Hildebrand, All Rights Reserved
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
package com.hellblazer.glassHouse.rest.service.impl;

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

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;
import com.hellblazer.glassHouse.rest.service.AggregateService;
import com.hellblazer.glassHouse.rest.util.ValueFactory;
import com.hellblazer.jmx.cascading.CascadingAgent;

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
        QueryExp nodeQuery = Query.not(ObjectName.getInstance(String.format("%s:*",
                                                                            "JMImplementation")));
        // Build up the query for all indicated nodes
        for (String jmxNode : jmxNodes) {
            QueryExp query = getNodeWildcardName(jmxNode);
            nodeQuery = Query.or(nodeQuery, query);
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

    /**
     * @param nodeX
     * @param sourceName
     * @return
     */
    public static ObjectName stripNodeName(ObjectName sourceName) {
        if (sourceName.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME) == null) {
            return sourceName;
        }
        try {
            final String domain = sourceName.getDomain();
            final String list = sourceName.getKeyPropertyListString();
            int index = list.indexOf(CascadingAgent.CASCADED_NODE_PROPERTY_NAME);
            if (index == -1) {
                throw new IllegalStateException(
                                                String.format("Did not find the %s property in string scan of %s",
                                                              CascadingAgent.CASCADED_NODE_PROPERTY_NAME,
                                                              list));
            }
            String prefix = list.substring(0, index);
            int suffixIndex = list.indexOf(',', index);
            String suffix = "";
            if (suffixIndex > 0) {
                suffix = list.substring(suffixIndex + 1);
            }

            return ObjectName.getInstance(String.format("%s:%s%s", domain,
                                                        prefix, suffix));
        } catch (MalformedObjectNameException x) {
            log.error(String.format("Cannot create wild card nod form of source name %s",
                                    sourceName), x);
            throw new IllegalStateException(
                                            String.format("Cannot create wild card nod form of source name %s",
                                                          sourceName), x);
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
            log.error(String.format("Cannot create wild card nod form of source name %s",
                                    sourceName), x);
            throw new IllegalStateException(
                                            String.format("Cannot create wild card nod form of source name %s",
                                                          sourceName), x);
        }
    }

    private final MBeanServer  mbeanServer;
    private final ValueFactory valueFactory;
    private final List<String> mBeanServerNodes = new CopyOnWriteArrayList<String>();

    public AggregateServiceImpl(MBeanServer mBeanServer,
                                ValueFactory valueFactory) {
        this.mbeanServer = mBeanServer;
        this.valueFactory = valueFactory;
    }

    public AggregateServiceImpl(MBeanServer mBeanServer) {
        this(mBeanServer, ValueFactory.getDefault());
    }

    /**
     * @param nodeName
     */
    public void addNode(String nodeName) {
        mBeanServerNodes.add(nodeName);
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

        Set<ObjectName> names = queryObjectNames(jmxNodes, objectName);
        if (names.size() == 0) {
            throw new InstanceNotFoundException(objectName);
        }
        for (ObjectName n : names) {
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
     * @see com.hellblazer.glassHouse.rest.service.AggregateService#getMbeanServerNodes()
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
            try {
                Collection<ObjectName> originalNames = mbeanServer.queryNames(getNodeWildcardName(jmxNode),
                                                                              null);
                Set<ObjectName> nodeObjectNames = new HashSet<ObjectName>();
                for (ObjectName stripped : originalNames) {
                    nodeObjectNames.add(stripNodeName(stripped));
                }
                if (commonObjectNames.isEmpty()) {
                    commonObjectNames.addAll(nodeObjectNames);
                } else {
                    removeObjectNamesWhichDoNotExistOnCurrentNode(commonObjectNames,
                                                                  nodeObjectNames);
                }
            } catch (MalformedObjectNameException | NullPointerException e) {
                log.warn(String.format("Exception getting object names for node %s",
                                       jmxNode), e);
            }

        }
        for (ObjectName n : commonObjectNames) {
            mBeanShortJaxBeans.add(new MBeanShortJaxBean(
                                                         uriInfo,
                                                         stripNodeName(n).getCanonicalName()));
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

        Set<ObjectName> names = queryObjectNames(jmxNodes, objectName);
        if (names.size() == 0) {
            throw new InstanceNotFoundException(objectName);
        }
        for (ObjectName n : names) {
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
                                                                             NullPointerException,
                                                                             InstanceNotFoundException {
        return invokeOperation(jmxNodes, objectName, operationName,
                               new String[] {}, new String[] {});
    }

    @Override
    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName,
                                                        String[] paramStrings,
                                                        String[] signature)
                                                                           throws MalformedObjectNameException,
                                                                           NullPointerException,
                                                                           InstanceNotFoundException {
        assert paramStrings != null;
        assert signature != null;
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();

        Set<ObjectName> names = queryObjectNames(jmxNodes, objectName);
        if (names.size() == 0) {
            throw new InstanceNotFoundException(objectName);
        }
        Object[] parameters = new Object[paramStrings.length];
        int i = 0;
        for (String param : paramStrings) {
            try {
                parameters[i] = valueFactory.valueOf(param, signature[i]);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                                                   String.format("Invalid argument type [%s]",
                                                                 param), e);
            }
            i++;
        }
        for (ObjectName n : names) {
            Object returnValue = null;
            String exception = null;
            try {
                returnValue = mbeanServer.invoke(n, operationName, parameters,
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

    /**
     * @param nodeName
     */
    public void removeNode(String nodeName) {
        mBeanServerNodes.remove(nodeName);
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
                                              stripNodeName(objectName).getCanonicalName(),
                                              value, exception);
    }

    private Set<ObjectName> queryObjectNames(Collection<String> jmxNodes,
                                             String name)
                                                         throws MalformedObjectNameException,
                                                         NullPointerException {
        QueryExp attributeQuery = constructNodeQuery(jmxNodes);
        ObjectName objectName = ObjectName.getInstance(name);
        if (objectName.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME) == null) {
            objectName = wildcardNodeForm(objectName);
        }
        return mbeanServer.queryNames(objectName, attributeQuery);
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
}
