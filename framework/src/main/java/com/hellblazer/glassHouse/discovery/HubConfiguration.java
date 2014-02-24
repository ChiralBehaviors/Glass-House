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
package com.hellblazer.glassHouse.discovery;

import static com.hellblazer.slp.ServiceScope.SERVICE_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellblazer.glassHouse.rest.service.impl.AggregateServiceImpl;
import com.hellblazer.gossip.configuration.GossipConfiguration;
import com.hellblazer.gossip.configuration.GossipModule;
import com.hellblazer.jmx.cascading.CascadingService;
import com.hellblazer.nexus.GossipScope;

/**
 * A configuration POJO using the Nexus gossip discovery service.
 * 
 * @author hhildebrand
 * 
 */
public class HubConfiguration {
    public static HubConfiguration fromYaml(InputStream yaml)
                                                             throws JsonParseException,
                                                             JsonMappingException,
                                                             IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new GossipModule());
        return mapper.readValue(yaml, HubConfiguration.class);
    }

    /**
     * The Gossip configuration
     */
    public GossipConfiguration gossip   = new GossipConfiguration();

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
    public Map<String, String> services = Collections.emptyMap();

    /**
     * A Map object that will be passed to the
     * {@link JMXConnectorFactory#connect(JMXServiceURL,Map)} method, in order
     * to connect to the source <tt>MBeanServer</tt>.
     */
    public Map<String, ?>      sourceMap;

    /**
     * Construct a hub from the receiver configuration, using the supplied
     * MBeanServer
     * 
     * @param mbs
     * @return the configured instance of the Hub
     * @throws Exception
     */
    public Hub construct(MBeanServer mbs) throws Exception {
        GossipScope scope = new GossipScope(gossip.construct());
        scope.start();
        CascadingService cascadingService = new CascadingService(mbs);
        mbs.registerMBean(cascadingService,
                          new ObjectName(cascadingServiceName));
        AggregateServiceImpl aggregateService = new AggregateServiceImpl(mbs);
        Hub hub = new Hub(cascadingService, sourceMap, scope, aggregateService);
        for (Map.Entry<String, String> entry : services.entrySet()) {
            hub.listenFor(String.format("(%s=%s)", SERVICE_TYPE, entry.getKey()),
                          entry.getValue());
        }
        return hub;
    }
}
