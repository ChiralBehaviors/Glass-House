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
package com.hellblazer.glassHouse.rest.web;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBeans;

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
