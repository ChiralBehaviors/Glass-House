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

import java.util.Collections;
import java.util.Map;

import javax.management.ObjectName;
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
     * The default domain name for the JMX MBeanServer managed by the hub
     */
    public String              domainName;

    /**
     * The Gossip configuration
     */
    public GossipConfiguration gossip          = new GossipConfiguration();

    /**
     * The JMX object name to register the hub service
     */
    public String              hubName;

    /**
     * The JMX object name to register the cascading service
     */
    public String              cascadingServiceName;

    /**
     * The map of abstract service names to jmx filter patterns. The keys
     * represent the abstract services that the hub will listen for. When an
     * instance of the service is discovered, the MBeans on the corresponding
     * MBean {@link ObjectName} pattern will be proxied into the hub.
     * <p>
     * for example:
     * 
     * <pre>
     *     daemon.jmx.* -> "*:*"
     *        when a "daemon.jmx.rmi://foo.com:5676 service is discovered,
     *        all the MBeans will be proxied
     *     app1.jmx.rmi -> "appServer:*"
     *        only the MBeans in the domain "appServer" will be proxied
     * </pre>
     */
    public Map<String, String> services        = Collections.emptyMap();

    /**
     * A Map object that will be passed to the
     * {@link JMXConnectorFactory#connect(JMXServiceURL,Map)} method, in order
     * to connect to the source <tt>MBeanServer</tt>.
     */
    public Map<String, ?>      sourceMap;
}
