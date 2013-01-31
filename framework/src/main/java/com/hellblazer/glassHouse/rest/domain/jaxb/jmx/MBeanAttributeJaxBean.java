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
/** 
 * Portions copyright (C) 2013 Hal Hildebrand, All Rights Reserved
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

package com.hellblazer.glassHouse.rest.domain.jaxb.jmx;

import java.net.URI;

import javax.management.MBeanAttributeInfo;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement
public class MBeanAttributeJaxBean implements Comparable<MBeanAttributeJaxBean> {
    @XmlElement(name = "Name")
    public String  name;
    public String  description;
    @XmlElement(name = "type")
    public String  type;
    public boolean isReadable;
    public boolean isWritable;
    public URI     uri;

    public MBeanAttributeJaxBean() {
    }

    public MBeanAttributeJaxBean(UriInfo uriInfo,
                                 MBeanAttributeInfo mBeanAttributeInfo) {
        name = mBeanAttributeInfo.getName();
        description = mBeanAttributeInfo.getDescription();
        type = mBeanAttributeInfo.getType();
        isReadable = mBeanAttributeInfo.isReadable();
        isWritable = mBeanAttributeInfo.isWritable();
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uri = uriBuilder.path("attributes/" + mBeanAttributeInfo.getName()).build();
    }

    public int compareTo(MBeanAttributeJaxBean o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MBeanAttributeJaxBean other = (MBeanAttributeJaxBean) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (isReadable != other.isReadable) {
            return false;
        }
        if (isWritable != other.isWritable) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + (description == null ? 0 : description.hashCode());
        result = prime * result + (isReadable ? 1231 : 1237);
        result = prime * result + (isWritable ? 1231 : 1237);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (uri == null ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanAttributeJaxBean [name=");
        builder.append(name);
        builder.append(", description=");
        builder.append(description);
        builder.append(", type=");
        builder.append(type);
        builder.append(", isReadable=");
        builder.append(isReadable);
        builder.append(", isWritable=");
        builder.append(isWritable);
        builder.append(", uri=");
        builder.append(uri);
        builder.append("]");
        return builder.toString();
    }
}
