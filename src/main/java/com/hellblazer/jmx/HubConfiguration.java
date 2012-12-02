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

package com.hellblazer.jmx;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.hellblazer.gossip.configuration.GossipConfiguration;
import com.yammer.dropwizard.config.Configuration;

/**
 * @author hhildebrand
 * 
 */
public class HubConfiguration extends Configuration {

    /**
     * The domain name for the JMX MBeanServer
     */
    public String              domainName;

    /**
     * The Gossip configuration
     */
    public GossipConfiguration gossip       = new GossipConfiguration();

    /**
     * The JMX object name to register the cascading service
     */
    public String              name;

    /**
     * The list of abstract service names corresponding to desired JMX adapter
     * services
     */
    public List<String>        serviceNames = Collections.emptyList();

    /**
     * A Map object that will be passed to the
     * {@link JMXConnectorFactory#connect(JMXServiceURL,Map)} method, in order
     * to connect to the source <tt>MBeanServer</tt>.
     */
    public Map<String, ?>      sourceMap;

    /**
     * An <tt>ObjectName</tt> pattern that must be satisfied by the
     * <tt>ObjectName</tt>s of the source MBeans. A null sourcePattern is
     * equivalent to *:*
     */
    public String              sourcePattern;

    /**
     * The <i>domain path</i> under which the source MBeans will be mounted in
     * the target <tt>MBeanServer</tt>. This string may contain up to 2 %s
     * patterns to accomidate the host and port of the remote MBeanServer.
     */
    public String              targetPath   = "/[%s/%s]";
}
