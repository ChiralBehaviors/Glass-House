package com.hellblazer.glassHouse.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hellblazer.nexus.GossipScope;
import com.hellblazer.slp.ServiceScope;

public class JmxProcess {

    public static JmxConfiguration configFrom(String config)
	    throws JsonParseException, JsonMappingException, IOException {

	File configFile = new File(config);
	if (configFile.exists()) {
	    return YamlHelper.fromYaml(configFile);
	}
	InputStream is = JmxConfiguration.class.getResourceAsStream(config);
	if (is == null) {
	    System.err.println(String.format(
		    "Configuration resource %s does not exist", config));
	}
	return YamlHelper.fromYaml(is);
    }

    private final JmxDiscovery jmxDiscovery;
    private final ServiceScope scope;

    public static void main(String[] argv) throws JsonParseException,
	    JsonMappingException, IOException, InterruptedException {
	if (argv.length != 1) {
	    System.err.println("Usage JmxProcess <config file>");
	    System.exit(1);
	}

	new JmxProcess(configFrom(argv[0]));
    }

    public JmxProcess(JmxConfiguration configuration) throws IOException,
	    InterruptedException {
	scope = new GossipScope(configuration.gossip.construct());
	this.jmxDiscovery = new JmxDiscovery(configuration, scope);
	scope.start();
	jmxDiscovery.start();
	while (true) {
	    Thread.sleep(1000);
	}
    }

    public JmxProcess(String config) throws JsonParseException,
	    JsonMappingException, IOException, InterruptedException {
	this(configFrom(config));
    }
}
