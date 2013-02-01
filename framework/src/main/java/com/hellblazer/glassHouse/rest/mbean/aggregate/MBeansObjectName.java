// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================
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

package com.hellblazer.glassHouse.rest.mbean.aggregate;

import java.util.Collection;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.glassHouse.AuthenticatedUser;
import com.hellblazer.glassHouse.rest.domain.jaxb.ErrorJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.service.AggregateService;
import com.yammer.dropwizard.auth.Auth;
import static com.hellblazer.glassHouse.AuthenticatedUser.AUDIT_LOG;

@Path("jmx/aggregate/{objectName}")
public class MBeansObjectName {

    private final AggregateService aggregateService;

    public MBeansObjectName(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getOperations(@PathParam("objectName") String objectName,
                                  @QueryParam("nodes") String nodes,
                                  @Auth AuthenticatedUser user) {
        AUDIT_LOG.info(String.format("User [%s] retrieving [%s] operation metadata for nodes [%s]",
                                     user.getName(), objectName, nodes));
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        MBeanAttributeJaxBeans mBeanAttributesJaxBean;
        MBeanOperationJaxBeans mBeanOperationsJaxBean;
        try {
            mBeanAttributesJaxBean = aggregateService.getAttributesMetaData(uriInfo,
                                                                            jmxNodes,
                                                                            objectName);
            if (mBeanAttributesJaxBean.mBeanAttributeJaxBeans.size() == 0) {
                throw new InstanceNotFoundException(objectName);
            }
            mBeanOperationsJaxBean = aggregateService.getOperationsMetaData(uriInfo,
                                                                            jmxNodes,
                                                                            objectName);
            return Response.ok(new MBeanJaxBean(objectName,
                                                mBeanOperationsJaxBean,
                                                mBeanAttributesJaxBean)).build();
        } catch (InstanceNotFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(new ErrorJaxBean(
                                                                             "Object not found",
                                                                             objectName)).build();
        } catch (MalformedObjectNameException | NullPointerException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorJaxBean(
                                                                               "Invalid Object name",
                                                                               objectName)).build();
        } catch (IntrospectionException | ReflectionException e) {
            throw new IllegalStateException(
                                            String.format("Unexpected exception retrieving operations for %s",
                                                          objectName, nodes), e);
        }
    }
}
