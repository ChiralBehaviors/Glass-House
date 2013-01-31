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

@XmlRootElement(name = "Parameter")
public class MBeanParamterJaxBean implements Comparable<MBeanParamterJaxBean> {
    @XmlElement(name = "ParameterName")
    public String name;
    @XmlElement(name = "Description")
    public String description;
    @XmlElement(name = "Type")
    public String type;

    public MBeanParamterJaxBean() {
    }

    public MBeanParamterJaxBean(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public int compareTo(MBeanParamterJaxBean o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanParamterJaxBean [name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

}