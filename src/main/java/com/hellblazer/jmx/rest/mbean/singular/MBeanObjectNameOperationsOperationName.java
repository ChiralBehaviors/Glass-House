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

package com.hellblazer.jmx.rest.mbean.singular;

import javax.management.MalformedObjectNameException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.OperationReturnValueJaxBean;
import com.hellblazer.jmx.rest.service.JmxService;

@Path("jmx/mbean/{objectName}/operations/{operationName}")
public class MBeanObjectNameOperationsOperationName {
    public MBeanObjectNameOperationsOperationName(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    private static Logger    log = LoggerFactory.getLogger(MBeanObjectNameOperationsOperationName.class);

    private final JmxService jmxService;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OperationReturnValueJaxBean invokeOperation(@PathParam("objectName") String objectName,
                                                       @PathParam("operationName") String operationName)
                                                                                                        throws MalformedObjectNameException,
                                                                                                        NullPointerException {
        log.info("invokeOperationWithParameters: " + operationName);

        return jmxService.invokeOperation(objectName, operationName);
    }

    @GET
    @Path("/{params}/{signature}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OperationReturnValueJaxBean invokeOperationWithParameters(@PathParam("objectName") String objectName,
                                                                     @PathParam("operationName") String operationName,
                                                                     @PathParam("params") String params,
                                                                     @PathParam("signature") String signature)
                                                                                                              throws MalformedObjectNameException,
                                                                                                              NullPointerException {
        log.info("invokeOperationWithParameters: " + operationName);

        String[] paramArray = params.split(",");
        String[] signatureArray = signature.split(",");
        return jmxService.invokeOperation(objectName, operationName,
                                          paramArray, signatureArray);
    }

}
