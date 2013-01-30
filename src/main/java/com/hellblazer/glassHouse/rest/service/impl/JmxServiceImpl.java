/** 
 * (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
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

import java.util.Set;
import java.util.TreeSet;

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

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.hellblazer.jmx.cascading.CascadingAgent;

/**
 * @author hhildebrand
 * 
 */
public class JmxServiceImpl implements JmxService {

    private final MBeanServer mbs;

    /**
     * @param mbs
     */
    public JmxServiceImpl(MBeanServer mbs) {
        super();
        this.mbs = mbs;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#getAllAttributeValues(java.lang.String)
     */
    @Override
    public MBeanAttributeValueJaxBeans getAllAttributeValues(String objectName)
                                                                               throws InstanceNotFoundException,
                                                                               MalformedObjectNameException,
                                                                               NullPointerException,
                                                                               IntrospectionException,
                                                                               ReflectionException {
        Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();

        ObjectName n = ObjectName.getInstance(objectName);
        for (MBeanAttributeInfo info : mbs.getMBeanInfo(n).getAttributes()) {
            mBeanAttributeValueJaxBeans.add(getAttributeValue(n, info.getName()));
        }
        return new MBeanAttributeValueJaxBeans(mBeanAttributeValueJaxBeans);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#getAttributesMetaData(javax.ws.rs.core.UriInfo, java.lang.String)
     */
    @Override
    public MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                        String objectName)
                                                                          throws InstanceNotFoundException,
                                                                          IntrospectionException,
                                                                          MalformedObjectNameException,
                                                                          ReflectionException,
                                                                          NullPointerException {

        Set<MBeanAttributeJaxBean> mBeanAttributeJaxBeans = new TreeSet<MBeanAttributeJaxBean>();
        for (MBeanAttributeInfo info : mbs.getMBeanInfo(ObjectName.getInstance(objectName)).getAttributes()) {
            mBeanAttributeJaxBeans.add(new MBeanAttributeJaxBean(uriInfo, info));
        }

        return new MBeanAttributeJaxBeans(mBeanAttributeJaxBeans);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#getAttributeValue(java.lang.String, java.lang.String)
     */
    @Override
    public MBeanAttributeValueJaxBean getAttributeValue(String objectName,
                                                        String attributeName)
                                                                             throws MalformedObjectNameException,
                                                                             NullPointerException {
        return getAttributeValue(ObjectName.getInstance(objectName),
                                 attributeName);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#getMBeanShortJaxBeans(javax.ws.rs.core.UriInfo)
     */
    @Override
    public MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo) {
        Set<MBeanShortJaxBean> mBeanShortJaxBeans = new TreeSet<MBeanShortJaxBean>();
        QueryExp query;
        try {
            query = Query.not(ObjectName.getInstance(String.format("%s:*",
                                                                   "JMImplementation")));
        } catch (MalformedObjectNameException | NullPointerException e) {
            throw new IllegalStateException(
                                            "Cannot create query to exclude JMImplementation");
        }
        for (ObjectName name : mbs.queryNames(null, query)) {
            mBeanShortJaxBeans.add(new MBeanShortJaxBean(
                                                         uriInfo,
                                                         name.getCanonicalName()));
        }
        return new MBeanShortJaxBeans(mBeanShortJaxBeans);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#getOperationsMetaData(javax.ws.rs.core.UriInfo, java.lang.String)
     */
    @Override
    public MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                        String objectName)
                                                                          throws InstanceNotFoundException,
                                                                          IntrospectionException,
                                                                          MalformedObjectNameException,
                                                                          ReflectionException,
                                                                          NullPointerException {
        Set<MBeanOperationJaxBean> mBeanOperationJaxBeans = new TreeSet<MBeanOperationJaxBean>();
        for (MBeanOperationInfo info : mbs.getMBeanInfo(ObjectName.getInstance(objectName)).getOperations()) {
            mBeanOperationJaxBeans.add(new MBeanOperationJaxBean(uriInfo, info));
        }

        return new MBeanOperationJaxBeans(objectName, mBeanOperationJaxBeans);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#invokeOperation(java.lang.String, java.lang.String)
     */
    @Override
    public OperationReturnValueJaxBean invokeOperation(String objectName,
                                                       String operationName)
                                                                            throws MalformedObjectNameException,
                                                                            NullPointerException {
        return invokeOperation(objectName, operationName, new Object[] {},
                               new String[] {});
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.service.JmxService#invokeOperation(java.lang.String, java.lang.String, java.lang.String[], java.lang.String[])
     */
    @Override
    public OperationReturnValueJaxBean invokeOperation(String objectName,
                                                       String operationName,
                                                       Object[] params,
                                                       String[] signature)
                                                                          throws MalformedObjectNameException,
                                                                          NullPointerException {
        ObjectName n = ObjectName.getInstance(objectName);
        Object returnValue = null;
        String exception = null;
        try {
            returnValue = mbs.invoke(n, operationName, params, signature);
        } catch (ReflectionException | MBeanException
                | InstanceNotFoundException e) {
            exception = e.toString();
        }
        return new OperationReturnValueJaxBean(
                                               n.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME),
                                               returnValue, exception);
    }

    /**
     * @param n
     * @param attributeName
     * @return
     */
    private MBeanAttributeValueJaxBean getAttributeValue(ObjectName n,
                                                         String attributeName) {
        Object value = null;
        String exception = null;
        try {
            value = mbs.getAttribute(n, attributeName);
        } catch (AttributeNotFoundException | MBeanException
                | ReflectionException | InstanceNotFoundException e) {
            exception = e.toString();
        }
        String nodeName = n.getKeyProperty(CascadingAgent.CASCADED_NODE_PROPERTY_NAME);
        if (nodeName == null) {
            nodeName = "";
        }
        return new MBeanAttributeValueJaxBean(attributeName, nodeName,
                                              n.getCanonicalName(), value,
                                              exception);
    }

}
