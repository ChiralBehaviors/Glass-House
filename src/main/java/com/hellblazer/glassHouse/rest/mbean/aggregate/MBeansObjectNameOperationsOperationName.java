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

import javax.management.MalformedObjectNameException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.glassHouse.rest.domain.jaxb.ErrorJaxBean;
import com.hellblazer.glassHouse.rest.service.AggregateService;

@Path("jmx/aggregate/{objectName}/operations/{operationName}")
public class MBeansObjectNameOperationsOperationName {
    public MBeansObjectNameOperationsOperationName(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    private static Logger          log = LoggerFactory.getLogger(MBeansObjectNameAttributes.class);

    private final AggregateService aggregateService;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response invokeOperation(@PathParam("objectName") String objectName,
                                    @PathParam("operationName") String operationName,
                                    @QueryParam("nodes") String nodes) {
        log.info("invokeOperationWithParameters: " + operationName);
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        try {
            return Response.ok(aggregateService.invokeOperation(jmxNodes,
                                                                objectName,
                                                                operationName)).build();
        } catch (MalformedObjectNameException | NullPointerException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorJaxBean(
                                                                               "Invalid Object name",
                                                                               objectName)).build();
        }
    }

    @GET
    @Path("/{params}/{signature}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response invokeOperationWithParameters(@PathParam("objectName") String objectName,
                                                  @PathParam("operationName") String operationName,
                                                  @PathParam("params") String params,
                                                  @PathParam("signature") String signature,
                                                  @QueryParam("nodes") String nodes) {
        log.info("invokeOperationWithParameters: " + operationName);
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        String[] paramArray = params.split(",");
        String[] signatureArray = signature.split(",");
        try {
            return Response.ok(aggregateService.invokeOperation(jmxNodes,
                                                                objectName,
                                                                operationName,
                                                                paramArray,
                                                                signatureArray)).build();
        } catch (MalformedObjectNameException | NullPointerException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorJaxBean(
                                                                               "Invalid Object name",
                                                                               objectName)).build();
        }
    }

}
