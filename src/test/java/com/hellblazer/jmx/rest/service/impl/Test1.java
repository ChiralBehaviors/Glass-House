/** 
 * (C) Copyright 2012 Hal Hildebrand, All Rights Reserved
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

package com.hellblazer.jmx.rest.service.impl;

/**
 * @author hhildebrand
 * 
 */
public class Test1 implements Test1MBean {

    private int attribute1 = -1;
    private int attribute2 = -1;

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#getAttribute1()
     */
    @Override
    public int getAttribute1() {
        return attribute1;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#setAttribute1(int)
     */
    @Override
    public void setAttribute1(int value) {
        attribute1 = value;

    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#getAttribute2()
     */
    @Override
    public int getAttribute2() {
        return attribute2;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#setAttribute2(int)
     */
    @Override
    public void setAttribute2(int value) {
        attribute2 = value;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#operation1()
     */
    @Override
    public String operation1() {
        return Integer.toString(attribute1);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.jmx.rest.service.impl.Test1MBean#operation2()
     */
    @Override
    public String operation2() {
        return Integer.toString(attribute2);
    }
}
