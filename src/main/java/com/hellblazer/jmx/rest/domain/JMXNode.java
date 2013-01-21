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

package com.hellblazer.jmx.rest.domain;

import javax.management.remote.JMXServiceURL;

/* ------------------------------------------------------------ */
/**
 */
public class JMXNode {
    private String        _nodeName;
    private JMXServiceURL _jmxServiceURL;

    public JMXNode(String nodeName, JMXServiceURL jmxServiceURL) {
        _nodeName = nodeName;
        _jmxServiceURL = jmxServiceURL;
    }

    public JMXServiceURL getJmxServiceURL() {
        return _jmxServiceURL;
    }

    public String getNodeName() {
        return _nodeName;
    }

}
