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

package com.hellblazer.glassHouse.rest.mbean.singular;

import static com.hellblazer.glassHouse.AuthenticatedUser.AUDIT_LOG;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.hellblazer.glassHouse.AuthenticatedUser;
import com.hellblazer.glassHouse.rest.domain.jaxb.jmx.MBeanShortJaxBeans;
import com.hellblazer.glassHouse.rest.service.JmxService;
import com.yammer.dropwizard.auth.Auth;

/**
 * @author hhildebrand
 * 
 */
@Path("/mbean")
public class MBean {

    /**
     * @param jmxService
     */
    public MBean(JmxService jmxService) {
        super();
        this.jmxService = jmxService;
    }

    private final JmxService jmxService;

    @Context
    UriInfo                  uriInfo;

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MBeanShortJaxBeans getMBeans(@Auth AuthenticatedUser user) {
        AUDIT_LOG.info(String.format("User [%s] listing MBeans", user.getName()));
        return jmxService.getMBeanShortJaxBeans(uriInfo);
    }

}
