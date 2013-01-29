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
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.cascading.CascadingServiceMBean;
import com.hellblazer.slp.InvalidSyntaxException;
import com.hellblazer.slp.ServiceEvent;
import com.hellblazer.slp.ServiceListener;
import com.hellblazer.slp.ServiceReference;
import com.hellblazer.slp.ServiceScope;
import com.hellblazer.slp.ServiceURL;

/**
 * @author hhildebrand
 * 
 */
public class Hub {
    private class Listener implements ServiceListener {

        /* (non-Javadoc)
         * @see com.hellblazer.slp.ServiceListener#serviceChanged(com.hellblazer.slp.ServiceEvent)
         */
        @Override
        public void serviceChanged(ServiceEvent event) {
            switch (event.getType()) {
                case REGISTERED: {
                    registered(event.getReference());
                    break;
                }
                case MODIFIED: {
                    modified(event.getReference());
                    break;
                }
                case UNREGISTERED: {
                    unregistered(event.getReference());
                    break;
                }
            }
        }

    }

    public static final String                  CONFIG_YML    = "config.yml";
    public static final String                  HOST          = "host";
    public static final String                  TYPE          = "type";
    public static final String                  TYPE_NAME     = MBeanServerConnection.class.getSimpleName();
    public static final String                  URL           = "service.url";

    private static Logger                       log           = LoggerFactory.getLogger(Hub.class);

    private final Map<ServiceReference, String> registrations = new ConcurrentHashMap<ServiceReference, String>();
    private final Listener                      listener      = new Listener();
    private final ServiceScope                  scope;
    private final CascadingServiceMBean         cascadingService;
    private final String                        nameNodePattern;
    private final ObjectName                    sourcePattern;
    private final Map<String, ?>                sourceMap;

    public Hub(CascadingServiceMBean cascadingService, String sourcePattern,
               Map<String, ?> sourceMap, ServiceScope scope,
               String nodeNamePattern) throws MalformedObjectNameException {
        this.cascadingService = cascadingService;
        this.sourcePattern = sourcePattern == null ? null
                                                  : new ObjectName(
                                                                   sourcePattern);
        this.sourceMap = sourceMap;
        this.scope = scope;
        this.nameNodePattern = nodeNamePattern == null ? "%s:%s"
                                                      : nodeNamePattern;
    }

    @ManagedOperation(description = "Listen for JMX service URLs that match the service query", impact = Impact.ACTION)
    public void listenFor(String query) throws InvalidSyntaxException {
        log.info(String.format("Listening for %s", query));
        scope.addServiceListener(listener, query);
    }

    /**
     * A convienence method so one doesn't have to construct a formal service
     * query filter expression.
     * 
     * @param serviceName
     * @throws InvalidSyntaxException
     */
    @ManagedOperation(description = "Listen for JMX service URLs that match the abstract service name", impact = Impact.ACTION)
    public void listenForService(String serviceName)
                                                    throws InvalidSyntaxException {
        listenFor(String.format("(%s=%s)", SERVICE_TYPE, serviceName));
    }

    @ManagedOperation(description = "Remove the registered query for JMX service URLs", impact = Impact.ACTION)
    public void removeQuery(String query) throws InvalidSyntaxException {
        scope.removeServiceListener(listener, query);
    }

    @ManagedOperation(description = "Remove the registered query for the abstract serviceName", impact = Impact.ACTION)
    public void removeServiceNameQuery(String serviceName)
                                                          throws InvalidSyntaxException {
        removeQuery(String.format("(%s=%s)", SERVICE_TYPE, serviceName));
    }

    /**
     * @param reference
     * @return
     * @throws MalformedURLException
     */
    private JMXServiceURL toServiceURL(ServiceReference reference)
                                                                  throws MalformedURLException {
        ServiceURL url = reference.getUrl();
        String jmxUrl = "jmx".equals(url.getServiceType().getAbstractTypeName()) ? String.format("%s://%s:%s%s",
                                                                                                 url.getServiceType().toString(),
                                                                                                 url.getHost(),
                                                                                                 url.getPort(),
                                                                                                 url.getUrlPath())
                                                                                : String.format("service:%s://%s:%s%s",
                                                                                                url.getServiceType().getConcreteTypeName().toString(),
                                                                                                url.getHost(),
                                                                                                url.getPort(),
                                                                                                url.getUrlPath());
        return new JMXServiceURL(jmxUrl);
    }

    /**
     * @param reference
     */
    protected void modified(ServiceReference reference) {
        // Does nothing at the moment
    }

    /**
     * @param reference
     * @throws IOException
     */
    protected void registered(ServiceReference reference) {
        assert reference != null;
        JMXServiceURL jmxServiceURL;
        try {
            jmxServiceURL = toServiceURL(reference);
        } catch (MalformedURLException e) {
            log.error(String.format("Invalid jmx url for %s", reference), e);
            return;
        }
        ServiceURL url = reference.getUrl();
        try {
            log.info(String.format("Registering MBeans for: %s:%s",
                                   url.getHost(), url.getPort()));
            registrations.put(reference,
                              cascadingService.mount(jmxServiceURL,
                                                     sourceMap,
                                                     sourcePattern,
                                                     nameNodePattern == null ? null
                                                                            : String.format(nameNodePattern,
                                                                                            url.getHost(),
                                                                                            url.getPort())));
        } catch (InstanceAlreadyExistsException | IOException e) {
            log.info(String.format("Error registering MBeans for: %s:%s",
                                   url.getHost(), url.getPort()), e);
        }
    }

    /**
     * @param reference
     * @throws IOException
     */
    protected void unregistered(ServiceReference reference) {
        String registration = registrations.remove(reference);
        ServiceURL url = reference.getUrl();
        if (registration != null) {
            try {
                log.info(String.format("Unregistering MBeans for: %s:%s",
                                       url.getHost(), url.getPort()));
                cascadingService.unmount(registration);
            } catch (IOException e) {
                log.warn(String.format("Unable to unmount %s, mount point id %s",
                                       reference, registration));
            }
        } else {
            log.warn(String.format("No cascading registration for %s",
                                   reference));
        }
    }
}
