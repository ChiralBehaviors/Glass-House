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

package com.hellblazer.jmx.rest.web;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.jmx.rest.domain.jaxb.NodeJaxBean;
import com.hellblazer.jmx.rest.domain.jaxb.NodesJaxBean;

/* ------------------------------------------------------------ */
/**
 */
@Path("jmx/nodes")
public class Nodes extends BaseAggregateWebController {
    @Context
    UriInfo uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public NodesJaxBean getObjectNames() {
        Set<NodeJaxBean> nodes = aggregateService.getNodes();
        return new NodesJaxBean(nodes);
    }

}
