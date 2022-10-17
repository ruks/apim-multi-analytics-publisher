package com.rukspot.sample.custompublisher;

import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultFaultMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultResponseMetricEventBuilder;

/**
 *
 */
public class CustomCounterMetric implements CounterMetric {
    private CounterMetric defaultCounterMetric;
    private CounterMetric elkCounterMetric;
    private MetricSchema schema;

    public CustomCounterMetric(CounterMetric defaultCounterMetric, CounterMetric elkCounterMetric,
            MetricSchema schema) {
        this.defaultCounterMetric = defaultCounterMetric;
        this.elkCounterMetric = elkCounterMetric;
        this.schema = schema;
    }

    @Override
    public int incrementCount(MetricEventBuilder metricEventBuilder) throws MetricReportingException {
        metricEventBuilder.build(); // build before sending events
        this.defaultCounterMetric.incrementCount(metricEventBuilder);
        this.elkCounterMetric.incrementCount(metricEventBuilder);
        return 0;
    }

    @Override
    public String getName() {
        return defaultCounterMetric.getName();
    }

    @Override
    public MetricSchema getSchema() {
        return defaultCounterMetric.getSchema();
    }

    @Override
    public MetricEventBuilder getEventBuilder() {
        switch (schema) {
        case RESPONSE:
            return new DefaultResponseMetricEventBuilder();
        case ERROR:
            return new DefaultFaultMetricEventBuilder();
        default:
            // will not happen
            return null;
        }
    }
}
