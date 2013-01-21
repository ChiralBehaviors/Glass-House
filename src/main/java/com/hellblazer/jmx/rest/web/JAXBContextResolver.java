package com.hellblazer.jmx.rest.web;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

	private JAXBContext context;
	private Class<?>[] types = { MBeanAttributeValueJaxBeans.class };

	public JAXBContextResolver() throws Exception {

		// JSONConfiguration config =
		// JSONConfiguration.mapped().arrays("Attribute").build();
		// context = new JSONJAXBContext(config, types);
	}

	public JAXBContext getContext(Class<?> objectType) {
		for (Class<?> type : types) {
			if (type == objectType) {
				return context;
			}
		}
		return null;
	}
}
