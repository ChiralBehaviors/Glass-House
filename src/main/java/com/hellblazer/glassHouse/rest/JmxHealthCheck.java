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

package com.hellblazer.glassHouse.rest;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.yammer.metrics.core.HealthCheck;

/**
 * @author hhildebrand
 * 
 */
public class JmxHealthCheck extends HealthCheck {
    private final MBeanServer mbs;

    /**
     * @param mbs
     */
    public JmxHealthCheck(MBeanServer mbs) {
        super("JMX Health Check");
        this.mbs = mbs;
    }

    /* (non-Javadoc)
     * @see com.yammer.metrics.core.HealthCheck#check()
     */
    @Override
    protected Result check() throws Exception {
        if (mbs.queryMBeans(ObjectName.getInstance(String.format("%s:*",
                                                                 "JMImplementation")),
                            null).size() > 0) {
            return Result.healthy();
        }
        return Result.unhealthy("MBeanServer does not appear to be working");
    }

}
