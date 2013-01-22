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

package com.hellblazer.jmx.rest.service.impl;

import static junit.framework.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.hellblazer.jmx.rest.service.JMXNodeService;

public class JMXNodeServiceTest {
    JMXNodeService jmxNodeService = new JMXNodeServiceImpl();

    /* ------------------------------------------------------------ */
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for
     * {@link org.mortbay.jetty.jmx.ws.service.impl.JMXNodeServiceImpl#getNodes()}
     * .
     */
    @Test
    public void testGetNodes() {
        Collection<String> jmxNodes = jmxNodeService.getNodes();
        assertEquals("Two nodes expected", 2, jmxNodes.size());
    }

}
