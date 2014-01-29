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

package com.hellblazer.glassHouse.demo;

import com.hellblazer.glassHouse.discovery.HubConfiguration;
import com.yammer.dropwizard.config.Configuration;

/**
 * @author hhildebrand
 * 
 */
public class HubServiceConfiguration extends Configuration {

    public HubConfiguration hub;

    /**
     * The default domain name for the JMX MBeanServer managed by the hub
     */
    public String           domainName;
}
