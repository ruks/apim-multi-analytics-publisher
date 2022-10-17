package com.rukspot.sample.custompublisher;

import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricReporter;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.TimerMetric;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultAnalyticsMetricReporter;
import org.wso2.am.analytics.publisher.reporter.elk.ELKMetricReporter;

import java.util.Map;

/**
 *
 */
public class CustomMetricReporter implements MetricReporter {
    private final DefaultAnalyticsMetricReporter defaultAnalyticsMetricReporter;
    private final ELKMetricReporter elkMetricReporter;
    private final Map<String, String> properties;

    public CustomMetricReporter(Map<String, String> properties) throws MetricCreationException {
        this.properties = properties;
        this.defaultAnalyticsMetricReporter = new DefaultAnalyticsMetricReporter(properties);
        this.elkMetricReporter = new ELKMetricReporter(properties);
    }

    @Override
    public CounterMetric createCounterMetric(String name, MetricSchema schema) throws MetricCreationException {
        CounterMetric defaultCounterMetric = this.defaultAnalyticsMetricReporter.createCounterMetric(name, schema);
        CounterMetric elkCounterMetric = this.elkMetricReporter.createCounterMetric(name, schema);
        CustomCounterMetric customCounterMetric = new CustomCounterMetric(defaultCounterMetric, elkCounterMetric,
                schema);
        return customCounterMetric;
    }

    @Override
    public TimerMetric createTimerMetric(String name) {
        return null;
    }

    @Override
    public Map<String, String> getConfiguration() {
        return this.properties;
    }
}
