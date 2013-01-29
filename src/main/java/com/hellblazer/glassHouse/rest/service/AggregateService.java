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
package com.hellblazer.glassHouse.rest.service;

import java.util.Collection;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;

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
