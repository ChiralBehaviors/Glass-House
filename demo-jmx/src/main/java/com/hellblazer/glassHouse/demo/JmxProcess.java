package com.hellblazer.glassHouse.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class JmxProcess {

    public static void main(String[] argv) {
	if (argv.length != 1) {
	    System.err.println("Usage JmxProcess <config file>");
	    System.exit(1);
	}
    }

    public JmxProcess(JmxConfiguration config) {

    }

    public JmxProcess(String config) throws JsonParseException,
	    JsonMappingException, IOException {
	this(configFrom(config));
    }

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
}
