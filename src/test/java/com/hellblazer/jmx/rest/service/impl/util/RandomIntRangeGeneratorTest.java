// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package com.hellblazer.jmx.rest.service.impl.util;

import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.hellblazer.jmx.rest.util.RandomIntRangeGenerator;

/* ------------------------------------------------------------ */
/**
 */
public class RandomIntRangeGeneratorTest {
    private static final int MIN = 8192;
    private static final int MAX = 65535;

    @Test
    public void testGetRandomInt() {
        for (int i = 0; i < 100000; i++) {
            int randomInt = RandomIntRangeGenerator.getRandomInt(MIN, MAX);
            // System.out.println(randomInt);
            assertTrue("not bigger than MIN: " + randomInt, randomInt >= MIN);
            assertTrue("not smaller than MAX: " + randomInt, randomInt <= MAX);
        }
    }
}
