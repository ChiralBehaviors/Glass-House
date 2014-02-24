package com.hellblazer.glassHouse.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellblazer.gossip.configuration.GossipModule;

public class YamlHelper {

    public static JmxConfiguration fromYaml(File yaml)
	    throws JsonParseException, JsonMappingException, IOException {
	return fromYaml(new FileInputStream(yaml));
    }

    public static JmxConfiguration fromYaml(InputStream yaml)
	    throws JsonParseException, JsonMappingException, IOException {
	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	mapper.registerModule(getModule());
	return mapper.readValue(yaml, JmxConfiguration.class);
    }

    public static Module getModule() {
	return new GossipModule();
    }
}