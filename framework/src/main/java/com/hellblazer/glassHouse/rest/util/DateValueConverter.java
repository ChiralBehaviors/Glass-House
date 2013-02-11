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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author hhildebrand
 * 
 */
public class DateValueConverter implements ValueConverter {

    private static String[]           supported  = { Date.class.getName() };

    private static final DateFormat[] allFormats = new DateFormat[] {
            DateFormat.getDateInstance(),
            DateFormat.getTimeInstance(),
            DateFormat.getDateTimeInstance(),
            // first pure date format
            DateFormat.getDateInstance(DateFormat.SHORT),
            DateFormat.getDateInstance(DateFormat.MEDIUM),
            DateFormat.getDateInstance(DateFormat.LONG),
            DateFormat.getDateInstance(DateFormat.FULL),
            // pure time format
            DateFormat.getTimeInstance(DateFormat.SHORT),
            DateFormat.getTimeInstance(DateFormat.MEDIUM),
            DateFormat.getTimeInstance(DateFormat.LONG),
            DateFormat.getTimeInstance(DateFormat.FULL),
            // combinations
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL),

            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG),
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL) };

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.util.ValueConverter#getSupportedTypes()
     */
    @Override
    public String[] getSupportedTypes() {
        return supported;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.glassHouse.rest.util.ValueConverter#valueOf(java.lang.String, java.lang.String)
     */
    @Override
    public Object valueOf(String parameterValue, String type) throws Exception {
        // this is tricky since Date can be written in many formats
        // will use the Date format with current locale and several
        // different formats
        Date value = null;
        for (int i = 0; i < allFormats.length; i++) {
            synchronized (allFormats[i]) {
                try {
                    System.out.println(parameterValue + " " + allFormats[i]);
                    value = allFormats[i].parse(parameterValue);
                    // if succeful then break
                    break;
                } catch (ParseException e) {
                    // ignore, the format wasn't appropriate
                }
            }
        }
        if (value == null) {
            throw new ParseException("Not possible to parse", 0);
        }
        return value;
    }

}
