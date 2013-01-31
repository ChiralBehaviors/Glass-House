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

package com.hellblazer.glassHouse;

import static com.hellblazer.slp.ServiceScope.SERVICE_TYPE;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.hellblazer.glassHouse.discovery.Hub;
import com.hellblazer.glassHouse.rest.JmxHealthCheck;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeans;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeansObjectName;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeansObjectNameAttributes;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeansObjectNameAttributesAttributeName;
import com.hellblazer.glassHouse.rest.mbean.aggregate.MBeansObjectNameOperationsOperationName;
import com.hellblazer.glassHouse.rest.mbean.singular.MBean;
import com.hellblazer.glassHouse.rest.mbean.singular.MBeanObjectName;
import com.hellblazer.glassHouse.rest.mbean.singular.MBeanObjectNameAttributes;
import com.hellblazer.glassHouse.rest.mbean.singular.MBeanObjectNameAttributesAttributeName;
import com.hellblazer.glassHouse.rest.mbean.singular.MBeanObjectNameOperationsOperationName;
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.hellblazer.glassHouse.rest.service.impl.AggregateServiceImpl;
import com.hellblazer.glassHouse.rest.service.impl.JmxServiceImpl;
import com.hellblazer.glassHouse.rest.web.Index;
import com.hellblazer.glassHouse.rest.web.Nodes;
import com.hellblazer.gossip.configuration.YamlHelper;
import com.hellblazer.jmx.cascading.CascadingService;
import com.hellblazer.nexus.GossipScope;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * @author hhildebrand
 * 
 */
public class HubService extends Service<HubConfiguration> {

    public static void main(String[] argv) throws Exception {
        new HubService().run(argv);
    }

    private Hub hub;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yammer.dropwizard.Service#initialize(com.yammer.dropwizard.config
     * .Bootstrap)
     */
    @Override
    public void initialize(Bootstrap<HubConfiguration> bootstrap) {
        bootstrap.getObjectMapperFactory().registerModule(YamlHelper.getModule());
        bootstrap.setName("Glass House");
        bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yammer.dropwizard.Service#run(com.yammer.dropwizard.config.Configuration
     * , com.yammer.dropwizard.config.Environment)
     */
    @Override
    public void run(HubConfiguration configuration, Environment environment)
                                                                            throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer(configuration.domainName);
        GossipScope scope = new GossipScope(configuration.gossip.construct());
        scope.start();
        CascadingService cascadingService = new CascadingService();
        ManagementFactory.getPlatformMBeanServer().registerMBean(cascadingService,
                                                                 new ObjectName(
                                                                                configuration.cascadingServiceName));
        AggregateServiceImpl aggregateService = new AggregateServiceImpl(mbs);
        hub = new Hub(cascadingService, configuration.sourceMap, scope,
                      aggregateService);
        for (Map.Entry<String, String> entry : configuration.services.entrySet()) {
            hub.listenFor(String.format("(%s=%s)", SERVICE_TYPE, entry.getKey()),
                          entry.getValue());
        }
        environment.addHealthCheck(new JmxHealthCheck(mbs));
        JmxService jmxService = new JmxServiceImpl(mbs);

        environment.addResource(Index.class);
        environment.addResource(Nodes.class);

        environment.addResource(new MBean(jmxService));
        environment.addResource(new MBeanObjectName(jmxService));
        environment.addResource(new MBeanObjectNameAttributes(jmxService));
        environment.addResource(new MBeanObjectNameAttributesAttributeName(
                                                                           jmxService));
        environment.addResource(new MBeanObjectNameOperationsOperationName(
                                                                           jmxService));

        environment.addResource(new MBeans(aggregateService));
        environment.addResource(new MBeansObjectName(aggregateService));
        environment.addResource(new MBeansObjectNameAttributes(aggregateService));
        environment.addResource(new MBeansObjectNameAttributesAttributeName(
                                                                            aggregateService));
        environment.addResource(new MBeansObjectNameOperationsOperationName(
                                                                            aggregateService));
        environment.addResource(new Index());
        environment.addResource(new Nodes(aggregateService));
    }

}
