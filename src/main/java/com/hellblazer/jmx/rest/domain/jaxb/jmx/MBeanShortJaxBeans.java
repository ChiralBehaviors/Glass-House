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

package com.hellblazer.jmx.rest.domain.jaxb.jmx;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement(name = "MBeans")
public class MBeanShortJaxBeans {
    @XmlElement(name = "MBean")
    public Set<MBeanShortJaxBean> mbeans = new HashSet<MBeanShortJaxBean>();

    public MBeanShortJaxBeans() {
    }

    public MBeanShortJaxBeans(Set<MBeanShortJaxBean> mBeans) {
        mbeans = mBeans;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeansJaxBean [objectName=");
        builder.append(mbeans);
        builder.append("]");
        return builder.toString();
    }

}
