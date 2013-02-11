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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hhildebrand
 * 
 */
public class ValueFactory implements ValueConverter {

    public static String arrayToString(Object array) {
        Object[] objectArray = (Object[]) array;
        StringBuffer result = new StringBuffer("[");

        for (int counter = 0; counter < objectArray.length; counter++) {
            result.append(objectArray[counter].toString());
            if (counter + 1 < objectArray.length) {
                result.append("\t");
            }
        }
        result.append("]");
        return result.toString();

    }

    public static ValueFactory getDefault() {
        ValueConverter converter = new DefaultValueConverter();
        ValueFactory valueFactory = new ValueFactory(converter);
        valueFactory.registerValueConverter(converter);
        valueFactory.registerValueConverter(new PropertiesValueConverter());
        valueFactory.registerValueConverter(new SetValueConverter());
        valueFactory.registerValueConverter(new DateValueConverter());
        return valueFactory;
    }

    public static String toString(Object value) {
        String stringValue = null;
        if (value != null && value.getClass().isArray()) {
            stringValue = arrayToString(value);
        } else if (value != null) {
            stringValue = value.toString();
        }

        return stringValue;
    }

    private Map<String, ValueConverter> converters;
    private ValueConverter              defaultConverter;

    private ValueFactory() {
        this(new DefaultValueConverter());
    }

    private ValueFactory(ValueConverter defaultConverter) {
        this(defaultConverter, new TreeMap<String, ValueConverter>());
    }

    private ValueFactory(ValueConverter defaultConverter,
                         Map<String, ValueConverter> converters) {
        this.converters = converters;
        this.defaultConverter = defaultConverter;
    }

    @Override
    public String[] getSupportedTypes() {
        return (String[]) converters.keySet().toArray();
    }

    public void registerValueConverter(ValueConverter converter) {
        if (converter != null) {
            String[] types = converter.getSupportedTypes();
            for (String type : types) {
                converters.put(type, converter);
            }
        }
    }

    @Override
    public Object valueOf(String value, String type) throws Exception {
        ValueConverter converter = converters.get(type);
        if (converter == null) {
            Class<?> theClass = Thread.currentThread().getContextClassLoader().loadClass(type);
            if (theClass != null & theClass.isArray()) {
                return valueOfArray(value, type);
            } else {
                converter = findConverterForClass(theClass);
            }
        }

        if (converter == null) {
            converter = defaultConverter;
        }

        return converter.valueOf(value, type);

    }

    private ValueConverter findConverterForClass(Class<?> theClass) {

        ValueConverter converter = converters.get(theClass.getName());
        if (converter == null) {
            Class<?> superClass = theClass.getSuperclass();
            if (superClass != null) {
                converter = findConverterForClass(superClass);
            }

            if (converter == null) {
                Class<?>[] interfaces = theClass.getInterfaces();
                for (Class<?> interface1 : interfaces) {
                    converter = findConverterForClass(interface1);
                    if (converter != null) {
                        break;
                    }
                }
            }
        }

        return converter;
    }

    private Object[] valueOfArray(String value, String type) throws Exception {
        Class<?> arrayClass = Thread.currentThread().getContextClassLoader().loadClass(type);
        if (arrayClass != null & arrayClass.isArray()) {
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(
                                                                                value,
                                                                                " []{}\t\n");
            ArrayList<Object> result = new ArrayList<Object>();
            while (tokenizer.hasMoreTokens()) {
                String arrayValue = tokenizer.nextToken();
                result.add(valueOf(arrayValue,
                                   arrayClass.getComponentType().getName()));
            }

            // convert the ArrayList to an actual array of the specified type
            //
            Object[] resultArray = (Object[]) java.lang.reflect.Array.newInstance(arrayClass.getComponentType(),
                                                                                  result.size());

            int counter = 0;
            for (Object o : result) {
                resultArray[counter++] = o;
            }
            return resultArray;
        } else {
            throw new IllegalArgumentException("Was expecting [" + type
                                               + "] to be an array.");
        }
    }
}
