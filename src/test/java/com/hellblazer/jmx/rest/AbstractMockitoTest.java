package com.hellblazer.jmx.rest;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMockitoTest {
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
}
