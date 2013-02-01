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

package com.hellblazer.glassHouse.rest.mbean.singular;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.yammer.dropwizard.auth.Auth;

@Path("jmx/mbean/{objectName}")
public class MBeanObjectName {

    private final JmxService jmxService;

    public MBeanObjectName(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getOperations(@PathParam("objectName") String objectName,
                                  @Auth AuthenticatedUser user) {
        MBeanAttributeJaxBeans mBeanAttributesJaxBean;
        MBeanOperationJaxBeans mBeanOperationsJaxBean;
        try {
            mBeanAttributesJaxBean = jmxService.getAttributesMetaData(uriInfo,
                                                                      objectName);
            if (mBeanAttributesJaxBean.mBeanAttributeJaxBeans.size() == 0) {
                throw new InstanceNotFoundException(objectName);
            }
            mBeanOperationsJaxBean = jmxService.getOperationsMetaData(uriInfo,
                                                                      objectName);
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
                                                          objectName), e);
        }
        return Response.ok(new MBeanJaxBean(objectName, mBeanOperationsJaxBean,
                                            mBeanAttributesJaxBean)).build();
    }

}
