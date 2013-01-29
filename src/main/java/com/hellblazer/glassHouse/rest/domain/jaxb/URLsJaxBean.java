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

package com.hellblazer.glassHouse.rest.domain.jaxb;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Node")
public class URLsJaxBean implements Comparable<URLsJaxBean> {

    @XmlElement(name = "nodeName")
    public String nodeName;
    @XmlElement(name = "URL")
    public URI    uri;

    public URLsJaxBean() {
    }

    public URLsJaxBean(UriInfo uriInfo, String nodeName, String path) {
        this.nodeName = nodeName;
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uri = uriBuilder.build();
    }

    public int compareTo(URLsJaxBean o) {
        return nodeName.compareTo(o.nodeName);
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
        URLsJaxBean other = (URLsJaxBean) obj;
        if (nodeName == null) {
            if (other.nodeName != null) {
                return false;
            }
        } else if (!nodeName.equals(other.nodeName)) {
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
        result = prime * result + (nodeName == null ? 0 : nodeName.hashCode());
        result = prime * result + (uri == null ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("URLsJaxBean [nodeName=");
        builder.append(nodeName);
        builder.append(", uri=");
        builder.append(uri);
        builder.append("]");
        return builder.toString();
    }

}