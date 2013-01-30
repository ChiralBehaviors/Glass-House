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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanJaxBean;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeansObjectNameAttributes;
import com.hellblazer.glassHouse.rest.service.JmxService;

@Path("jmx/mbean/{objectName}")
public class MBeanObjectName {
    private static Logger    log = LoggerFactory.getLogger(MBeansObjectNameAttributes.class);

    private final JmxService jmxService;

    public MBeanObjectName(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getOperations(@PathParam("objectName") String objectName)
                                                                             throws MalformedObjectNameException,
                                                                             IntrospectionException,
                                                                             NullPointerException,
                                                                             ReflectionException {
        MBeanAttributeJaxBeans mBeanAttributesJaxBean;
        MBeanOperationJaxBeans mBeanOperationsJaxBean;
        try {
            mBeanAttributesJaxBean = jmxService.getAttributesMetaData(uriInfo,
                                                                      objectName);
            mBeanOperationsJaxBean = jmxService.getOperationsMetaData(uriInfo,
                                                                      objectName);
        } catch (InstanceNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("getOperations: ", e);
            }
            return Response.status(Status.NOT_FOUND).entity(MBeanJaxBean.EMPTY_MBEAN_JAX_BEAN).build();
        }
        return Response.ok(new MBeanJaxBean(objectName, mBeanOperationsJaxBean,
                                            mBeanAttributesJaxBean)).build();
    }

}
