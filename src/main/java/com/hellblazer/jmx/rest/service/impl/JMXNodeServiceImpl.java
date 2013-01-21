// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package com.hellblazer.jmx.rest.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.jmx.rest.domain.JMXNode;
import com.hellblazer.jmx.rest.service.JMXNodeService;

/* ------------------------------------------------------------ */
/**
 */
public class JMXNodeServiceImpl implements JMXNodeService {
	private static Logger log = LoggerFactory
			.getLogger(JMXNodeServiceImpl.class);

	private Properties _properties = new Properties();

	public Set<JMXNode> getNodes() { 
		InputStream propertyInputStream = this.getClass().getClassLoader()
				.getResourceAsStream("jmxNodes.properties");
		if (propertyInputStream == null) {
			throw new IllegalStateException(
					"Couldn't read jmxNodes.properties file!");
		}
		Set<JMXNode> jmxNodes = new HashSet<JMXNode>();
		try {
			_properties.load(propertyInputStream);
			String nodeString = (String) _properties.get("nodes");
			String[] nodes = nodeString.split(",");
			for (String string : nodes) {
				String jmxServiceURL = "service:jmx:rmi:///jndi/rmi://"
						+ string + "/jettyjmx";
				jmxNodes.add(new JMXNode(string, new JMXServiceURL(
						jmxServiceURL)));
			}
		} catch (IOException e) {
			log.warn("Exception loading jmxNode properties", e);
		} finally {
			try {
				propertyInputStream.close();
			} catch (IOException e) {
				log.warn(
						"getNodes: Couldn't close InputStream. This might lead to a file descriptor leak: ",
						e);
			}
		}
		return jmxNodes;
	}

}
