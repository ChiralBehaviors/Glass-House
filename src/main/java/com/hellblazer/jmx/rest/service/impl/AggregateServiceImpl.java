package com.hellblazer.jmx.rest.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.hellblazer.jmx.rest.service.JMXService;

public class AggregateServiceImpl implements AggregateService {
    private static Logger      log              = LoggerFactory.getLogger(AggregateServiceImpl.class);
    public static final String ID_REPLACE_REGEX = ",id=\\d+";

    private JMXService         _jmxService;
    private final List<String> mBeanServerNodes = new CopyOnWriteArrayList<String>();

    public AggregateServiceImpl(JMXService jmxService) {
        _jmxService = jmxService;
    }

    @Override
    public MBeanAttributeValueJaxBeans getAllAttributeValues(Collection<String> jmxNodes,
                                                             String objectName) {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = aggregateMBeanAttributeInfos(jmxNodes,
                                                                                           objectName);
        for (String jmxNode : jmxNodes) {
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,
                                                                                      jmxNode);
            try {
                mBeanAttributeValueJaxBeans.addAll(getAttributeValues(objectNames,
                                                                      mBeanAttributeInfos,
                                                                      jmxNode));
            } catch (InstanceNotFoundException e) {
                try {
                    objectNames = _jmxService.getObjectNamesByPrefix(jmxNode,
                                                                     objectName);
                    mBeanAttributeValueJaxBeans.addAll(getAttributeValues(objectNames,
                                                                          mBeanAttributeInfos,
                                                                          jmxNode));
                } catch (MalformedObjectNameException | NullPointerException e1) {
                    log.warn(String.format("Invalid name %s", objectName));
                } catch (InstanceNotFoundException e1) {
                    log.warn(String.format("Instance %s not found on %s",
                                           objectName, jmxNode));
                }
            }
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    @Override
    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName)
                                                                          throws InstanceNotFoundException {
        Set<MBeanAttributeJaxBean> mBeanAttributeJaxBeans = new TreeSet<MBeanAttributeJaxBean>();
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = aggregateMBeanAttributeInfos(jmxNodes,
                                                                                           objectName);

        for (String attributeName : mBeanAttributeInfos.keySet()) {
            mBeanAttributeJaxBeans.add(new MBeanAttributeJaxBean(
                                                                 uriInfo,
                                                                 mBeanAttributeInfos.get(attributeName)));
        }

        return new MBeanAttributeJaxBeans(mBeanAttributeJaxBeans);
    }

    @Override
    public MBeanAttributeValueJaxBeans getAttributeValues(Collection<String> jmxNodes,
                                                          String objectName,
                                                          String attributeName) {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        for (String jmxNode : jmxNodes) {
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,
                                                                                      jmxNode);
            try {
                getAttributeValuesForNode(attributeName,
                                          mBeanAttributeValueJaxBeans, jmxNode,
                                          objectNames);
            } catch (InstanceNotFoundException e) {
                try {
                    objectNames = _jmxService.getObjectNamesByPrefix(jmxNode,
                                                                     objectName);
                    getAttributeValuesForNode(attributeName,
                                              mBeanAttributeValueJaxBeans,
                                              jmxNode, objectNames);
                } catch (MalformedObjectNameException | NullPointerException e1) {
                    log.warn(String.format("Invalid name %s", objectName));
                } catch (InstanceNotFoundException e1) {
                    log.debug(String.format("Instance %s not found on %s",
                                            objectName, jmxNode));
                }
            }
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
                nodeObjectNames = _jmxService.getObjectNames(jmxNode);
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
    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                        Collection<String> jmxNodes,
                                                        String objectName) {
        Set<MBeanOperationJaxBean> mBeanOperationJaxBeans = new TreeSet<MBeanOperationJaxBean>();
        Map<String, MBeanOperationInfo> mBeanOperations = aggregateOperations(jmxNodes,
                                                                              objectName);

        for (MBeanOperationInfo mBeanOperationInfo : mBeanOperations.values()) {
            mBeanOperationJaxBeans.add(new MBeanOperationJaxBean(uriInfo,
                                                                 mBeanOperationInfo));
        }

        return new MBeanOperationJaxBeans(objectName, mBeanOperationJaxBeans);
    }

    @Override
    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName) {
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();
        for (String jmxNode : jmxNodes) {
            String exception = null;
            Object returnValue = null;
            try {
                returnValue = _jmxService.invoke(jmxNode, objectName,
                                                 operationName, null, null);
            } catch (MalformedObjectNameException | ReflectionException
                    | MBeanException e) {
                exception = e.toString();
            } catch (InstanceNotFoundException e) {
                log.debug("Instance %s not found on %s", objectName, jmxNode);
                break;
            }
            operationReturnValueJaxBeans.add(new OperationReturnValueJaxBean(
                                                                             jmxNode,
                                                                             returnValue,
                                                                             exception));
        }
        return new OperationReturnValueJaxBeans(operationReturnValueJaxBeans);
    }

    @Override
    public OperationReturnValueJaxBeans invokeOperation(Collection<String> jmxNodes,
                                                        String objectName,
                                                        String operationName,
                                                        Object[] params,
                                                        String[] signature) {
        Set<OperationReturnValueJaxBean> operationReturnValueJaxBeans = new TreeSet<OperationReturnValueJaxBean>();
        for (String jmxNode : jmxNodes) {
            Object returnValue = null;
            String exception = null;
            try {
                returnValue = _jmxService.invoke(jmxNode, objectName,
                                                 operationName, params,
                                                 signature);
            } catch (MalformedObjectNameException | InstanceNotFoundException
                    | ReflectionException | MBeanException e) {
                exception = e.toString();
            }
            operationReturnValueJaxBeans.add(new OperationReturnValueJaxBean(
                                                                             jmxNode,
                                                                             returnValue,
                                                                             exception));
        }
        return new OperationReturnValueJaxBeans(operationReturnValueJaxBeans);
    }

    private void addAttributeInfosToMap(Map<String, MBeanAttributeInfo> mBeanAttributeInfos,
                                        Set<String> duplicatesToRemove,
                                        MBeanAttributeInfo[] mBeanAttributeInfoArray) {
        for (MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfoArray) {
            String attributeName = mBeanAttributeInfo.getName();
            if (mBeanAttributeInfos.containsKey(attributeName)
                && !mBeanAttributeInfo.equals(mBeanAttributeInfos.get(attributeName))) {
                duplicatesToRemove.add(attributeName);
            } else {
                mBeanAttributeInfos.put(attributeName, mBeanAttributeInfo);
            }
        }
    }

    private Map<String, MBeanAttributeInfo> aggregateMBeanAttributeInfos(Collection<String> jmxNodes,
                                                                         String objectName) {
        Map<String, MBeanAttributeInfo> mBeanAttributeInfos = new HashMap<String, MBeanAttributeInfo>();
        Set<String> nonEqualAttributeNamesToRemove = new HashSet<String>();
        for (String jmxNode : jmxNodes) {
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,
                                                                                      jmxNode);

            try {
                if (!aggregateMBeanAttributeInfosForNode(mBeanAttributeInfos,
                                                         nonEqualAttributeNamesToRemove,
                                                         jmxNode, objectNames)) {
                    return Collections.emptyMap();
                }
            } catch (InstanceNotFoundException e) {
                try {
                    objectNames = _jmxService.getObjectNamesByPrefix(jmxNode,
                                                                     objectName);
                } catch (MalformedObjectNameException | NullPointerException e1) {
                    log.debug(String.format("Invalid object name %s",
                                            objectName), e1);
                }
                try {
                    if (!aggregateMBeanAttributeInfosForNode(mBeanAttributeInfos,
                                                             nonEqualAttributeNamesToRemove,
                                                             jmxNode,
                                                             objectNames)) {
                        return Collections.emptyMap();
                    }
                } catch (InstanceNotFoundException e1) {
                    log.debug(String.format("Cannot find instance %s on %s",
                                            objectName, jmxNode), e1);
                }
            }
        }

        removeDuplicates(mBeanAttributeInfos, nonEqualAttributeNamesToRemove);

        return mBeanAttributeInfos;
    }

    private boolean aggregateMBeanAttributeInfosForNode(Map<String, MBeanAttributeInfo> mBeanAttributeInfos,
                                                        Set<String> nonEqualAttributeNamesToRemove,
                                                        String jmxNode,
                                                        Set<String> objectNames)
                                                                                throws InstanceNotFoundException {
        for (String aggregatedObjectName : objectNames) {
            MBeanAttributeInfo[] mBeanAttributeInfoArray;
            try {
                mBeanAttributeInfoArray = _jmxService.getAttributes(jmxNode,
                                                                    aggregatedObjectName);
                if (mBeanAttributeInfoArray != null) {
                    addAttributeInfosToMap(mBeanAttributeInfos,
                                           nonEqualAttributeNamesToRemove,
                                           mBeanAttributeInfoArray);
                } else {
                    return false;
                }
            } catch (IntrospectionException | MalformedObjectNameException
                    | ReflectionException e) {
                log.warn("Unable to get attribute info for %s on %s",
                         aggregatedObjectName, jmxNode);
                return false;
            }
        }
        return true;
    }

    private boolean aggregateMBeanOperationInfosForNode(Set<String> objectNames,
                                                        Map<String, MBeanOperationInfo> mBeanOperations,
                                                        Set<MBeanOperationInfo> nonEqualOperationsToRemove,
                                                        String jmxNode)
                                                                       throws InstanceNotFoundException {
        for (String aggregatedObjectName : objectNames) {
            MBeanOperationInfo[] mBeanOperationInfoArray;
            try {
                mBeanOperationInfoArray = _jmxService.getOperations(jmxNode,
                                                                    aggregatedObjectName);
                if (mBeanOperationInfoArray != null) {
                    for (MBeanOperationInfo mBeanOperationInfo : mBeanOperationInfoArray) {
                        if (mBeanOperations.containsKey(mBeanOperationInfo.getName())
                            && !mBeanOperations.get(mBeanOperationInfo.getName()).equals(mBeanOperationInfo)) {
                            nonEqualOperationsToRemove.add(mBeanOperationInfo);
                        } else {
                            mBeanOperations.put(mBeanOperationInfo.getName(),
                                                mBeanOperationInfo);
                        }
                    }
                } else {
                    return false;
                }
            } catch (IntrospectionException | MalformedObjectNameException
                    | ReflectionException e) {
                log.warn(String.format("Cannot retrieve operation information for node %s",
                                       jmxNode));
            }
        }
        return true;
    }

    private Map<String, MBeanOperationInfo> aggregateOperations(Collection<String> jmxNodes,
                                                                String objectName) {

        Map<String, MBeanOperationInfo> mBeanOperations = new HashMap<String, MBeanOperationInfo>();
        Set<MBeanOperationInfo> nonEqualOperationsToRemove = new HashSet<MBeanOperationInfo>();
        // TODO: three nested for loops and some if clauses...too high cyclomatic complexity
        for (String jmxNode : jmxNodes) {
            Set<String> objectNames = parseObjectNameToAggregateMBeansWithMultipleIDs(objectName,
                                                                                      jmxNode);
            try {
                if (!aggregateMBeanOperationInfosForNode(objectNames,
                                                         mBeanOperations,
                                                         nonEqualOperationsToRemove,
                                                         jmxNode)) {
                    return Collections.emptyMap();
                }
            } catch (InstanceNotFoundException e) {
                try {
                    objectNames = _jmxService.getObjectNamesByPrefix(jmxNode,
                                                                     objectName);
                    try {
                        if (!aggregateMBeanOperationInfosForNode(objectNames,
                                                                 mBeanOperations,
                                                                 nonEqualOperationsToRemove,
                                                                 jmxNode)) {
                            return Collections.emptyMap();
                        }
                    } catch (InstanceNotFoundException e1) {
                        log.debug(String.format("Cannot find instance %s on %s",
                                                objectName, jmxNode), e1);
                    }
                } catch (MalformedObjectNameException | NullPointerException e1) {
                    log.warn(String.format("Malformed object name %s",
                                           objectName), e1);
                }
            }
        }

        removeNonEqualOperations(mBeanOperations, nonEqualOperationsToRemove);

        return mBeanOperations;
    }

    private Set<MBeanAttributeValueJaxBean> getAttributeValues(Set<String> objectNames,
                                                               Map<String, MBeanAttributeInfo> mBeanAttributeInfos,
                                                               String jmxNode)
                                                                              throws InstanceNotFoundException {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();
        for (String attributeName : mBeanAttributeInfos.keySet()) {
            getAttributeValuesForNode(attributeName,
                                      mBeanAttributeValueJaxBeans, jmxNode,
                                      objectNames);
        }
        return mBeanAttributeValueJaxBeans;
    }

    private void getAttributeValuesForNode(String attributeName,
                                           Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans,
                                           String jmxNode,
                                           Set<String> objectNames)
                                                                   throws InstanceNotFoundException {
        for (String aggregatedObjectName : objectNames) {
            Object value = null;
            String exception = null;
            try {
                value = _jmxService.getAttribute(jmxNode, aggregatedObjectName,
                                                 attributeName);
            } catch (AttributeNotFoundException | MalformedObjectNameException
                    | MBeanException | ReflectionException e) {
                exception = e.toString();
            }
            mBeanAttributeValueJaxBeans.add(new MBeanAttributeValueJaxBean(
                                                                           attributeName,
                                                                           jmxNode,
                                                                           aggregatedObjectName,
                                                                           value,
                                                                           exception));
        }
    }

    private Set<String> getObjectNamesWithoutId(Set<ObjectName> objectNames) {
        Set<String> objectNameStrings = new TreeSet<String>();
        for (ObjectName objectName : objectNames) {
            objectNameStrings.add(objectName.toString().replaceFirst(ID_REPLACE_REGEX,
                                                                     ""));
        }
        return objectNameStrings;
    }

    private void removeDuplicates(Map<String, MBeanAttributeInfo> mBeanAttributeInfos,
                                  Set<String> duplicatesToRemove) {
        for (String attributeName : duplicatesToRemove) {
            mBeanAttributeInfos.remove(attributeName);
        }
    }

    private void removeNonEqualOperations(Map<String, MBeanOperationInfo> mBeanOperations,
                                          Set<MBeanOperationInfo> nonEqualOperationsToRemove) {
        for (MBeanOperationInfo mBeanOperationInfo : nonEqualOperationsToRemove) {
            mBeanOperations.remove(mBeanOperationInfo.getName());
        }
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

    Set<String> parseObjectNameToAggregateMBeansWithMultipleIDs(String objectName,
                                                                String jmxNode) {
        Set<String> objectNames = new TreeSet<String>();
        objectNames.add(objectName);

        String idMatchRegex = ".*?" + ID_REPLACE_REGEX;

        if (objectName.matches(idMatchRegex)) {
            String objectNamePrefix = objectName.replaceFirst(ID_REPLACE_REGEX,
                                                              "");
            try {
                objectNames = _jmxService.getObjectNamesByPrefix(jmxNode,
                                                                 objectNamePrefix);
            } catch (MalformedObjectNameException | NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return objectNames;
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
}
