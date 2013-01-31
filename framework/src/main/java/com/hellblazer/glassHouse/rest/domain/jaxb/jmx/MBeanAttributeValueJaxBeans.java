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

import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MBeanAttributeValueJaxBeans {
    @XmlElement(name = "Attribute")
    public Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans = new TreeSet<MBeanAttributeValueJaxBean>();

    public MBeanAttributeValueJaxBeans() {
    }

    public MBeanAttributeValueJaxBeans(Set<MBeanAttributeValueJaxBean> mBeanAttributeValueJaxBeans) {
        this.mBeanAttributeValueJaxBeans = mBeanAttributeValueJaxBeans;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MBeanAttributeValuesJaxBean [mBeanAttributeValueJaxBeans=");
        builder.append(mBeanAttributeValueJaxBeans);
        builder.append("]");
        return builder.toString();
    }
}
