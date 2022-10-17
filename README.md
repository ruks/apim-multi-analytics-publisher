# apim-multi-analytics-publisher
Custom data publisher to push apis events to multiple endpoints

There can be requirements to publish analytics data to multiple endpoints. Ex: ELK, Choreo, Google analytics etc. But there is no direct way to define multiple publishers(Reporters) in APIM. The solution for this is to use a custom reporter. Most common requirement was to use choreo analytics and ELK analytics. 
Both ELK and Choreo reporters are availble in the APIM 400 onwards by default. But the limiations was only one at a time can be used. 

With this custom publisher APIM can used to publish analytics to two enpodint choreo and ELK. For this we need to implment custom reporter and counter metric.

# Custom reporter
```
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
```

# customer counter metric
```
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
```

# Registering the custom reporter in deployment.toml
```
[apim.analytics]
enable = true
auth_token = "<auth tokken>"
config_endpoint = "https://analytics-event-auth.choreo.dev/auth/v1"
properties."publisher.reporter.class" = "com.rukspot.sample.custompublisher.CustomMetricReporter"
```
