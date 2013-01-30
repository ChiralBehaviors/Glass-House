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

package com.hellblazer.glassHouse.rest.service;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;

/**
 * @author hhildebrand
 * 
 */
public interface JmxService {

    /**
     * Answer the list of descriptions of mbeans on this server
     * 
     * @param uriInfo
     * @return
     */
    MBeanShortJaxBeans getMBeanShortJaxBeans(UriInfo uriInfo);

    /**
     * @param uriInfo
     * @param objectName
     * @return
     * @throws NullPointerException
     * @throws ReflectionException
     * @throws MalformedObjectNameException
     * @throws IntrospectionException
     */
    MBeanAttributeJaxBeans getAttributesMetaData(UriInfo uriInfo,
                                                 String objectName)
                                                                   throws InstanceNotFoundException,
                                                                   IntrospectionException,
                                                                   MalformedObjectNameException,
                                                                   ReflectionException,
                                                                   NullPointerException;

    /**
     * @param uriInfo
     * @param objectName
     * @return
     * @throws NullPointerException
     * @throws ReflectionException
     * @throws MalformedObjectNameException
     * @throws IntrospectionException
     */
    MBeanOperationJaxBeans getOperationsMetaData(UriInfo uriInfo,
                                                 String objectName)
                                                                   throws InstanceNotFoundException,
                                                                   IntrospectionException,
                                                                   MalformedObjectNameException,
                                                                   ReflectionException,
                                                                   NullPointerException;

    /**
     * @param objectName
     * @return
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws IntrospectionException
     */
    MBeanAttributeValueJaxBeans getAllAttributeValues(String objectName)
                                                                        throws InstanceNotFoundException,
                                                                        MalformedObjectNameException,
                                                                        NullPointerException,
                                                                        IntrospectionException,
                                                                        ReflectionException;

    /**
     * @param objectName
     * @param operationName
     * @return
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     */
    OperationReturnValueJaxBean invokeOperation(String objectName,
                                                String operationName)
                                                                     throws MalformedObjectNameException,
                                                                     NullPointerException,
                                                                     InstanceNotFoundException;

    /**
     * @param objectName
     * @param operationName
     * @param paramArray
     * @param signatureArray
     * @return
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws InstanceNotFoundException
     */
    OperationReturnValueJaxBean invokeOperation(String objectName,
                                                String operationName,
                                                Object[] paramArray,
                                                String[] signatureArray)
                                                                        throws MalformedObjectNameException,
                                                                        NullPointerException,
                                                                        InstanceNotFoundException;

    /**
     * @param objectName
     * @param attributeName
     * @return
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     */
    MBeanAttributeValueJaxBean getAttributeValue(String objectName,
                                                 String attributeName)
                                                                      throws MalformedObjectNameException,
                                                                      NullPointerException;

}
