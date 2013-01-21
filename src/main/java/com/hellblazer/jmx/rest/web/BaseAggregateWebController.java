
package com.hellblazer.jmx.rest.web;

import com.hellblazer.jmx.rest.service.AggregateService;
import com.hellblazer.jmx.rest.service.impl.AggregateServiceImpl;
import com.hellblazer.jmx.rest.service.impl.JMXServiceImpl;

public class BaseAggregateWebController {
    protected static AggregateService aggregateService = new AggregateServiceImpl(
                                                                                  JMXServiceImpl.getInstance());

    public void setAggregateService(AggregateService aggregateService) {
        BaseAggregateWebController.aggregateService = aggregateService;
    }
}
