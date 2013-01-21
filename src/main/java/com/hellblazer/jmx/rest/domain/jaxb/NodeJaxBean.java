
package com.hellblazer.jmx.rest.domain.jaxb;
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



import java.util.Map;

import javax.management.remote.JMXServiceURL;
import javax.xml.bind.annotation.XmlRootElement;

import com.hellblazer.jmx.rest.util.JMXServiceURLUtils;

/* ------------------------------------------------------------ */
/**
 */
@XmlRootElement(name = "Node")
public class NodeJaxBean implements Comparable<NodeJaxBean> {
	public static class NodeJaxBeanBuilder {
		private String name;
		private String jettyVersion;
		private JMXServiceURL jmxServiceURL;

		// optional
		private int threadCount = 0;
		private int peakThreadCount = 0;
		private Long heapUsed = 0L;
		private Long init = 0L;
		private Long committed = 0L;
		private Long max = 0L;

		public NodeJaxBeanBuilder(String name, String jettyVersion,
				JMXServiceURL jmxServiceURL) {
			this.name = name;
			this.jettyVersion = jettyVersion;
			this.jmxServiceURL = jmxServiceURL;
		}

		public NodeJaxBean build() {
			return new NodeJaxBean(name, jettyVersion, threadCount,
					peakThreadCount, heapUsed, init, committed, max,
					jmxServiceURL);
		}

		public NodeJaxBeanBuilder memory(Map<String, Long> memory) {
			heapUsed = memory.get("used");
			init = memory.get("init");
			committed = memory.get("committed");
			max = memory.get("max");
			return this;
		}

		public NodeJaxBeanBuilder peakThreadCount(int peakThreadCount) {
			this.peakThreadCount = peakThreadCount;
			return this;
		}

		public NodeJaxBeanBuilder threadCount(int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

	}

	public String name;
	public String jettyVersion;
	public int threadCount;
	public int peakThreadCount;
	public Long heapUsed;
	public Long heapInit;
	public Long heapCommitted;
	public Long heapMax;

	public String jmxServiceURL;

	public NodeJaxBean() {
	}

	private NodeJaxBean(String name, String jettyVersion, int threadCount,
			int peakThreadCount, Long heapUsed, Long init, Long committed,
			Long max, JMXServiceURL jmxServiceURL) {
		this.name = name;
		this.threadCount = threadCount;
		this.peakThreadCount = peakThreadCount;
		this.jettyVersion = jettyVersion;
		heapInit = init;
		this.heapUsed = heapUsed;
		heapCommitted = committed;
		heapMax = max;
		this.jmxServiceURL = jmxServiceURL.toString();
	}

	public int compareTo(NodeJaxBean o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NodeJaxBean other = (NodeJaxBean) obj;
		if (heapCommitted == null) {
			if (other.heapCommitted != null) {
				return false;
			}
		} else if (!heapCommitted.equals(other.heapCommitted)) {
			return false;
		}
		if (heapUsed == null) {
			if (other.heapUsed != null) {
				return false;
			}
		} else if (!heapUsed.equals(other.heapUsed)) {
			return false;
		}
		if (heapInit == null) {
			if (other.heapInit != null) {
				return false;
			}
		} else if (!heapInit.equals(other.heapInit)) {
			return false;
		}
		if (jettyVersion == null) {
			if (other.jettyVersion != null) {
				return false;
			}
		} else if (!jettyVersion.equals(other.jettyVersion)) {
			return false;
		}
		if (jmxServiceURL == null) {
			if (other.jmxServiceURL != null) {
				return false;
			}
		} else if (!jmxServiceURL.equals(other.jmxServiceURL)) {
			return false;
		}
		if (heapMax == null) {
			if (other.heapMax != null) {
				return false;
			}
		} else if (!heapMax.equals(other.heapMax)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (peakThreadCount != other.peakThreadCount) {
			return false;
		}
		if (threadCount != other.threadCount) {
			return false;
		}
		return true;
	}

	public Long getCommitted() {
		return heapCommitted;
	}

	public Long getHeapUsed() {
		return heapUsed;
	}

	public Long getInit() {
		return heapInit;
	}

	public String getJettyVersion() {
		return jettyVersion;
	}

	public JMXServiceURL getJmxServiceURL() {
		return JMXServiceURLUtils.getJMXServiceURL(jmxServiceURL);
	}

	public Long getMax() {
		return heapMax;
	}

	public String getName() {
		return name;
	}

	public int getPeakThreadCount() {
		return peakThreadCount;
	}

	public int getThreadCount() {
		return threadCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (heapCommitted == null ? 0 : heapCommitted.hashCode());
		result = prime * result + (heapUsed == null ? 0 : heapUsed.hashCode());
		result = prime * result + (heapInit == null ? 0 : heapInit.hashCode());
		result = prime * result
				+ (jettyVersion == null ? 0 : jettyVersion.hashCode());
		result = prime * result
				+ (jmxServiceURL == null ? 0 : jmxServiceURL.hashCode());
		result = prime * result + (heapMax == null ? 0 : heapMax.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + peakThreadCount;
		result = prime * result + threadCount;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeJaxBean [name=");
		builder.append(name);
		builder.append(", jettyVersion=");
		builder.append(jettyVersion);
		builder.append(", threadCount=");
		builder.append(threadCount);
		builder.append(", peakThreadCount=");
		builder.append(peakThreadCount);
		builder.append(", heapUsed=");
		builder.append(heapUsed);
		builder.append(", init=");
		builder.append(heapInit);
		builder.append(", committed=");
		builder.append(heapCommitted);
		builder.append(", max=");
		builder.append(heapMax);
		builder.append(", jmxServiceURL=");
		builder.append(jmxServiceURL);
		builder.append("]");
		return builder.toString();
	}
}
