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

import com.hellblazer.jmx.rest.domain.JMXNode;
import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;
import com.hellblazer.jmx.rest.util.FilterNodesUtils;
import com.hellblazer.jmx.rest.web.BaseAggregateWebController;

/* ------------------------------------------------------------ */
/**
 */
@Path("/mbeans/{objectName}/attributes")
public class MBeansObjectNameAttributes extends BaseAggregateWebController {
	private static Logger log = LoggerFactory.getLogger(MBeansObjectNameAttributes.class);
    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MBeanAttributeValueJaxBeans getAttribute(@PathParam("objectName") String objectName,
                                                    @QueryParam("nodes") String nodes) {
        Collection<JMXNode> jmxNodes = FilterNodesUtils.getNodesToAggregate(nodes);
        try {
            return aggregateService.getAllAttributeValues(jmxNodes, objectName);
        } catch (InstanceNotFoundException e) {
            log.info("getAttribute: ", e);
            return null;
        }
    }
}
