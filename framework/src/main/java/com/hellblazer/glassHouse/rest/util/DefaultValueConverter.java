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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hhildebrand
 *
 */
/**
 * Converts a String to its equivalent object type. This implementation supports
 * most java.lang.* primitives and their equivalent object types (e.g. int ->
 * Integer, boolean -> Boolean etc.) using the valueOf method through reflection
 * and using a constructor with a single String argument.
 * 
 */
public class DefaultValueConverter implements ValueConverter {

    private static Map<String, Class<?>> primToClass = new TreeMap<String, Class<?>>();

    private static String[]              supported   = null;

    static {
        primToClass.put("boolean", Boolean.class);
        primToClass.put("byte", Byte.class);
        primToClass.put("char", Character.class);
        primToClass.put("double", Double.class);
        primToClass.put("float", Float.class);
        primToClass.put("int", Integer.class);
        primToClass.put("long", Long.class);
        primToClass.put("short", Short.class);

        supported = new String[primToClass.size() + 1];
        supported[0] = String.class.getName();

        int counter = 1;
        for (String type : primToClass.keySet()) {
            supported[counter++] = type;
        }
    }

    @Override
    public String[] getSupportedTypes() {
        return supported;
    }

    @Override
    public Object valueOf(String value, String type) throws Exception {
        Class<?> targetClass = null;
        targetClass = primToClass.get(type);
        if (targetClass == null) {
            targetClass = java.lang.Class.forName(type);
        }

        try {
            Method valueOfMethod = null;
            try {
                Class<?>[] paramTypes = { java.lang.String.class };
                valueOfMethod = targetClass.getMethod("valueOf", paramTypes);
            } catch (NoSuchMethodException e) {
                Class<?>[] paramTypes = { java.lang.Object.class };
                valueOfMethod = targetClass.getMethod("valueOf", paramTypes);
            }
            Object[] argValues = { value };
            return valueOfMethod.invoke(null, argValues);
        } catch (NoSuchMethodException e) {
            Class<?>[] paramTypes = { String.class };
            Constructor<?> stringConstructor = targetClass.getConstructor(paramTypes);
            Object[] argValues = { value };
            return stringConstructor.newInstance(argValues);
        }

    }

}