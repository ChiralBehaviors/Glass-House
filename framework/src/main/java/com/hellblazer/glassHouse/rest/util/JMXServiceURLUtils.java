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

package com.hellblazer.glassHouse.rest.util;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

public class JMXServiceURLUtils {

	private static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private static ObjectName name;
	private static JMXServiceURL rmiServiceURL;

	public static JMXServiceURL getJMXServiceURL(Address address) {
		return getJMXServiceURL("service:jmx:rmi:///jndi/rmi://"
				+ address.getHost() + ":" + address.getPort() + "/jettyjmx");
	}

	public static JMXServiceURL getJMXServiceURL(String jmxServiceURLString) {
		try {
			return new JMXServiceURL(jmxServiceURLString);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static JMXServiceURL getLocalJMXServiceURL() {
		if (rmiServiceURL == null) {
			try {
				name = new ObjectName(
						"org.eclipse.jetty:name=rmiconnectorserver");
				rmiServiceURL = (JMXServiceURL) mbs.getAttribute(name,
						"Address");
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

		}
		return rmiServiceURL;
	}

}
