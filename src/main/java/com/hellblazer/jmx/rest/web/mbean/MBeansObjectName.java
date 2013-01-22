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

import javax.management.InstanceNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeJaxBeans;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanOperationJaxBeans;
import com.hellblazer.jmx.rest.service.AggregateService;

@Path("/mbeans/{objectName}")
public class MBeansObjectName {
    private static Logger          log = LoggerFactory.getLogger(MBeansObjectNameAttributes.class);
    
    private final AggregateService aggregateService;

    public MBeansObjectName(AggregateService aggregateService) {
        this.aggregateService = aggregateService;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MBeanJaxBean getOperations(@PathParam("objectName") String objectName,
                                      @QueryParam("nodes") String nodes) {
        Collection<String> jmxNodes = aggregateService.getNodesToAggregate(nodes);

        MBeanAttributeJaxBeans mBeanAttributesJaxBean;
        MBeanOperationJaxBeans mBeanOperationsJaxBean;
        try {
            mBeanAttributesJaxBean = aggregateService.getAttributesMetaData(uriInfo,
                                                                            jmxNodes,
                                                                            objectName);
            mBeanOperationsJaxBean = aggregateService.getOperationsMetaData(uriInfo,
                                                                            jmxNodes,
                                                                            objectName);
        } catch (InstanceNotFoundException e) {
            log.info("getOperations: ", e);
            return MBeanJaxBean.EMPTY_MBEAN_JAX_BEAN;
        }
        return new MBeanJaxBean(objectName, mBeanOperationsJaxBean,
                                mBeanAttributesJaxBean);
    }

}
