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

package com.hellblazer.glassHouse.rest.util;

import java.net.InetSocketAddress;

/**
 * @author hhildebrand
 * 
 */
public class Address {
    public static Address from(String hostAndPort) {
        String host;
        int port;
        int colon = hostAndPort.indexOf(':');
        if (colon >= 0) {
            host = hostAndPort.substring(0, colon);
            port = Integer.parseInt(hostAndPort.substring(colon + 1));
        } else {
            host = hostAndPort;
            port = 0;
        }
        return new Address(host, port);
    }

    private final String host;

    private final int    port;

    public Address(String host, int port) {
        this.host = host.trim();
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Address that = (Address) obj;
        if (!host.equals(that.host)) {
            return false;
        }
        return port == that.port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    public InetSocketAddress toSocketAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
