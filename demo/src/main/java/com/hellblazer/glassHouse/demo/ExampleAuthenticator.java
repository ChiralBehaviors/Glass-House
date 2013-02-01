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

package com.hellblazer.glassHouse.demo;

import com.google.common.base.Optional;
import com.hellblazer.glassHouse.AuthenticatedUser;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;

/**
 * @author hhildebrand
 * 
 */
public class ExampleAuthenticator implements
        Authenticator<BasicCredentials, AuthenticatedUser> {
    @Override
    public Optional<AuthenticatedUser> authenticate(final BasicCredentials credentials)
                                                                                       throws AuthenticationException {
        if ("secret".equals(credentials.getPassword())) {
            AuthenticatedUser user = new AuthenticatedUser() {
                private final String name = credentials.getUsername();

                @Override
                public String getName() {
                    return name;
                }
            };
            return Optional.of(user);
        }
        return Optional.absent();
    }
}