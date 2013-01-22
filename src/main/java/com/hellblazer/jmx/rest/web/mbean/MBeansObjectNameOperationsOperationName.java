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

package com.hellblazer.jmx.rest.web.mbean;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBeans;
import com.hellblazer.jmx.rest.web.BaseAggregateWebController;
 
@Path("/mbeans/{objectName}/operations/{operationName}")
public class MBeansObjectNameOperationsOperationName extends
        BaseAggregateWebController {
    private static Logger log = LoggerFactory.getLogger(MBeansObjectNameAttributes.class);

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OperationReturnValueJaxBeans invokeOperation(@PathParam("objectName") String objectName,
                                                        @PathParam("operationName") String operationName,
                                                        @QueryParam("nodes") String nodes) {
        log.info("invokeOperationWithParameters: " + operationName);
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        return aggregateService.invokeOperation(jmxNodes, objectName,
                                                operationName);
    }

    @GET
    @Path("/{params}/{signature}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OperationReturnValueJaxBeans invokeOperationWithParameters(@PathParam("objectName") String objectName,
                                                                      @PathParam("operationName") String operationName,
                                                                      @PathParam("params") String params,
                                                                      @PathParam("signature") String signature,
                                                                      @QueryParam("nodes") String nodes) {
        log.info("invokeOperationWithParameters: " + operationName);
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        String[] paramArray = params.split(",");
        String[] signatureArray = signature.split(",");
        return aggregateService.invokeOperation(jmxNodes, objectName,
                                                operationName, paramArray,
                                                signatureArray);
    }

}
