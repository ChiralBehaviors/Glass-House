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

package com.hellblazer.jmx.rest.domain.jaxb;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Index")
public class IndexJaxBean {
    public URI nodes;
    public URI aggregate;
    public URI mBean;

    public IndexJaxBean() {
    }

    public IndexJaxBean(UriInfo uriInfo) {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        aggregate = uriBuilder.path("aggregate").build();
        uriBuilder = uriInfo.getAbsolutePathBuilder();
        mBean = uriBuilder.path("mbean").build();
        uriBuilder = uriInfo.getAbsolutePathBuilder();
        nodes = uriBuilder.path("nodes").build();
    }

}
