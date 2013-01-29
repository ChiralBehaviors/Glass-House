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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OperationReturnValueJaxBean implements
        Comparable<OperationReturnValueJaxBean> {

    @XmlElement(name = "NodeName")
    public String nodeName;
    public String returnValue;
    public String exception;

    public OperationReturnValueJaxBean() {
    }

    public OperationReturnValueJaxBean(String nodeName, Object object,
                                       String exception) {
        this.nodeName = nodeName;
        if (exception != null) {
            returnValue = "failure!";
        } else {
            returnValue = object == null ? "success" : object.toString();
        }
        this.exception = exception;
    }

    public int compareTo(OperationReturnValueJaxBean o) {
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
        OperationReturnValueJaxBean other = (OperationReturnValueJaxBean) obj;
        if (nodeName == null) {
            if (other.nodeName != null) {
                return false;
            }
        } else if (!nodeName.equals(other.nodeName)) {
            return false;
        }
        if (returnValue == null) {
            if (other.returnValue != null) {
                return false;
            }
        } else if (!returnValue.equals(other.returnValue)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (nodeName == null ? 0 : nodeName.hashCode());
        result = prime * result
                 + (returnValue == null ? 0 : returnValue.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OperationReturnValueJaxBean [nodeName=");
        builder.append(nodeName);
        builder.append(", returnValue=");
        builder.append(returnValue);
        builder.append("]");
        return builder.toString();
    }
}
