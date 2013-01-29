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

@XmlRootElement(name = "MBean")
public class MBeanJaxBean implements Comparable<MBeanJaxBean> {
    public static final MBeanJaxBean EMPTY_MBEAN_JAX_BEAN = new MBeanJaxBean(
                                                                             "not found",
                                                                             null,
                                                                             null);

    @XmlElement(name = "ObjectName")
    public String                    objectName;
    @XmlElement(name = "Operations")
    public MBeanOperationJaxBeans    operations;
    @XmlElement(name = "Attributes")
    public MBeanAttributeJaxBeans    attributes;

    public MBeanJaxBean() {
    }

    public MBeanJaxBean(String objectName,
                        MBeanOperationJaxBeans mBeanOperationsJaxBean,
                        MBeanAttributeJaxBeans mBeanAttributesJaxBean) {
        this.objectName = objectName;
        operations = mBeanOperationsJaxBean;
        attributes = mBeanAttributesJaxBean;
    }

    public int compareTo(MBeanJaxBean o) {
        return objectName.compareTo(o.objectName);
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
        MBeanJaxBean other = (MBeanJaxBean) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (objectName == null) {
            if (other.objectName != null) {
                return false;
            }
        } else if (!objectName.equals(other.objectName)) {
            return false;
        }
        if (operations == null) {
            if (other.operations != null) {
                return false;
            }
        } else if (!operations.equals(other.operations)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + (attributes == null ? 0 : attributes.hashCode());
        result = prime * result
                 + (objectName == null ? 0 : objectName.hashCode());
        result = prime * result
                 + (operations == null ? 0 : operations.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanJaxBean [objectName=");
        builder.append(objectName);
        builder.append(", operations=");
        builder.append(operations);
        builder.append(", attributes=");
        builder.append(attributes);
        builder.append("]");
        return builder.toString();
    }

}