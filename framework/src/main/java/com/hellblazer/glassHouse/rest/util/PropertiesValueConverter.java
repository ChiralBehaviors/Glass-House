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

package com.hellblazer.glassHouse.rest.util;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author hhildebrand
 * 
 */
public class PropertiesValueConverter implements ValueConverter {

    private static String[] supported = { Properties.class.getName() };

    @Override
    public String[] getSupportedTypes() {
        return supported;
    }

    @Override
    public Object valueOf(String value, String type) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(value, " []{}\t\n\r\f");
        Properties result = new Properties();
        while (tokenizer.hasMoreTokens()) {
            String property = tokenizer.nextToken();

            StringTokenizer valueTokenizer = new StringTokenizer(property, "=");

            String propName = valueTokenizer.nextToken();
            String propValue = valueTokenizer.nextToken();

            result.setProperty(propName, propValue);
        }
        return result;
    }

}
