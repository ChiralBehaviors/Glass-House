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

package com.hellblazer.glassHouse.rest.mbean.singular;

import static com.hellblazer.glassHouse.AuthenticatedUser.AUDIT_LOG;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.glassHouse.AuthenticatedUser;
import com.hellblazer.glassHouse.rest.domain.jaxb.ErrorJaxBean;
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.yammer.dropwizard.auth.Auth;

@Path("/mbean/{objectName}/operations/{operationName}")
public class MBeanObjectNameOperationsOperationName {
    public MBeanObjectNameOperationsOperationName(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    private static Logger    log = LoggerFactory.getLogger(MBeanObjectNameOperationsOperationName.class);

    private final JmxService jmxService;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response invokeOperation(@PathParam("objectName") String objectName,
                                    @PathParam("operationName") String operationName,
                                    @Auth AuthenticatedUser user) {
        AUDIT_LOG.info(String.format("User [%s] invoking [%s] operation on [%s]",
                                     user.getName(), operationName, objectName));
        log.info("invokeOperationWithParameters: " + operationName);

        try {
            return Response.ok(jmxService.invokeOperation(objectName,
                                                          operationName)).build();
        } catch (MalformedObjectNameException | NullPointerException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorJaxBean(
                                                                               "Invalid Object name",
                                                                               objectName)).build();
        } catch (InstanceNotFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(new ErrorJaxBean(
                                                                             "Unkown Object",
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
                                                  @Auth AuthenticatedUser user) {
        AUDIT_LOG.info(String.format("User [%s] invoking [%s(%s)] operation on [%s]",
                                     user.getName(), operationName, signature,
                                     objectName));
        log.info("invokeOperationWithParameters: " + operationName);

        String[] paramArray = params.split(",");
        String[] signatureArray = signature.split(",");
        try {
            return Response.ok(jmxService.invokeOperation(objectName,
                                                          operationName,
                                                          paramArray,
                                                          signatureArray)).build();
        } catch (MalformedObjectNameException | NullPointerException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ErrorJaxBean(
                                                                               "Invalid Object name",
                                                                               objectName)).build();
        } catch (InstanceNotFoundException e) {
            return Response.status(Status.NOT_FOUND).entity(new ErrorJaxBean(
                                                                             "Unkown Object",
                                                                             objectName)).build();
        }
    }

}
