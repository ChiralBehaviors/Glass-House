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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedAttribute;
import net.gescobar.jmx.annotation.ManagedOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.glassHouse.rest.service.impl.AggregateServiceImpl;
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
	private final ObjectName sourcePattern;

	private Listener(ObjectName sourcePattern) {
	    this.sourcePattern = sourcePattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hellblazer.slp.ServiceListener#serviceChanged(com.hellblazer.
	 * slp.ServiceEvent)
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
	    switch (event.getType()) {
	    case REGISTERED: {
		registered(event.getReference(), sourcePattern);
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

    public static final String CONFIG_YML = "config.yml";
    public static final String HOST = "host";
    public static final String TYPE = "type";
    public static final String TYPE_NAME = MBeanServerConnection.class
	    .getSimpleName();
    public static final String URL = "service.url";

    private static Logger log = LoggerFactory.getLogger(Hub.class);

    private final CascadingServiceMBean cascadingService;
    private final Map<String, String> nodeNames = new HashMap<>();
    private final Map<String, Listener> outstandingQueries = new HashMap<String, Hub.Listener>();
    private final Map<ServiceReference, String> registrations = new ConcurrentHashMap<ServiceReference, String>();
    private final ServiceScope scope;
    private final Map<String, ?> sourceMap;
    private final AggregateServiceImpl aggregateService;

    public Hub(CascadingServiceMBean cascadingService,
	    Map<String, ?> sourceMap, ServiceScope scope,
	    AggregateServiceImpl aggregateService)
	    throws MalformedObjectNameException {
	this.cascadingService = cascadingService;
	this.sourceMap = sourceMap;
	this.scope = scope;
	this.aggregateService = aggregateService;
    }

    @ManagedAttribute(description = "The list of discovered nodes")
    public String[] getNodes() {
	Collection<String> values = nodeNames.values();
	return values.toArray(new String[values.size()]);
    }

    @ManagedOperation(description = "Listen for JMX service URLs that match the service query", impact = Impact.ACTION)
    public void listenFor(String query, String filter)
	    throws InvalidSyntaxException, MalformedObjectNameException,
	    NullPointerException {
	ObjectName sourcePattern = ObjectName.getInstance(filter);
	log.info(String
		.format("Listening for %s with filter %s", query, filter));
	Listener listener = new Listener(sourcePattern);
	outstandingQueries.put(query, listener);
	scope.addServiceListener(listener, query);
    }

    /**
     * A convienence method so one doesn't have to construct a formal service
     * query filter expression.
     * 
     * @param serviceName
     *            - the service name
     * @param filter
     *            - the filter used to select the mbeans on this service
     * @throws InvalidSyntaxException
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     */
    @ManagedOperation(description = "Listen for JMX service URLs that match the abstract service name", impact = Impact.ACTION)
    public void listenForService(String serviceName, String filter)
	    throws InvalidSyntaxException, MalformedObjectNameException,
	    NullPointerException {
	listenFor(String.format("(%s=%s)", SERVICE_TYPE, serviceName), filter);
    }

    @ManagedOperation(description = "Remove the registered query", impact = Impact.ACTION)
    public void removeQuery(String query) throws InvalidSyntaxException {
	Listener listener = outstandingQueries.remove(query);
	if (listener == null) {
	    log.info(String.format("No listener registered for query '%s'",
		    query));
	    return;
	}
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
	String jmxUrl = "jmx"
		.equals(url.getServiceType().getAbstractTypeName()) ? String
		.format("%s://%s:%s%s", url.getServiceType().toString(),
			url.getHost(), url.getPort(), url.getUrlPath())
		: String.format("service:%s://%s:%s%s", url.getServiceType()
			.getConcreteTypeName().toString(), url.getHost(),
			url.getPort(), url.getUrlPath());
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
     * @param sourcePattern
     * @throws IOException
     */
    protected void registered(ServiceReference reference,
	    ObjectName sourcePattern) {
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
	    String nodeName = String
		    .format("%s|%s|%s", url.getServiceType().toString()
			    .replace(':', '|'), url.getHost(), url.getPort());
	    String mountPoint = cascadingService.mount(jmxServiceURL,
		    sourceMap, sourcePattern, nodeName);
	    registrations.put(reference, mountPoint);
	    nodeNames.put(mountPoint, nodeName);
	    aggregateService.addNode(nodeName);
	} catch (InstanceAlreadyExistsException | IOException e) {
	    log.info(
		    String.format("Error registering MBeans for: %s:%s",
			    url.getHost(), url.getPort()), e);
	}
    }

    /**
     * @param reference
     * @throws IOException
     */
    protected void unregistered(ServiceReference reference) {
	String mountPoint = registrations.remove(reference);
	ServiceURL url = reference.getUrl();
	if (mountPoint != null) {
	    String nodeName = nodeNames.remove(mountPoint);
	    try {
		log.info(String.format("Unregistering MBeans for: %s:%s",
			url.getHost(), url.getPort()));
		cascadingService.unmount(mountPoint);
		aggregateService.removeNode(nodeName);
	    } catch (IOException e) {
		log.warn(String.format(
			"Unable to unmount %s, mount point id %s", reference,
			mountPoint));
	    }
	} else {
	    log.warn(String.format("No cascading registration for %s",
		    reference));
	}
    }
}
