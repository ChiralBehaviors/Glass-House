/** 
 * (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
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

package com.hellblazer.jmx.rest.mbean.singular;

import javax.management.MalformedObjectNameException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.jmx.rest.domain.jaxb.jmx.MBeanAttributeValueJaxBean;
import com.hellblazer.jmx.rest.service.JmxService;

@Path("jmx/mbean/{objectName}/attributes/{attributeName}")
public class MBeanObjectNameAttributesAttributeName {

    public MBeanObjectNameAttributesAttributeName(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    private final JmxService jmxService;

    @Context
    UriInfo                  uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MBeanAttributeValueJaxBean getAttribute(@PathParam("objectName") String objectName,
                                                   @PathParam("attributeName") String attributeName)
                                                                                                    throws MalformedObjectNameException {
        return jmxService.getAttributeValue(objectName, attributeName);
    }

}
