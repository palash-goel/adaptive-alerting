/*
 * Copyright 2018-2019 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteSource;
import com.expedia.adaptivealerting.anomdetect.util.HttpClientWrapper;
import com.expedia.adaptivealerting.anomdetect.util.MetricUtil;
import com.expedia.adaptivealerting.anomdetect.util.PropertiesUtil;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;

@Slf4j
public class DataInitializer {

    public static final String BASE_URI = "graphite.baseUri";
    public static final String EARLIEST_TIME = "graphite.earliestTime";
    public static final String MAX_DATA_POINTS = "graphite.maxDataDataPoints";

    public void initializeDetector(MappedMetricData mappedMetricData, Detector detector) {
        if (detector != null && isSeasonalNaiveDetector(detector)) {
            val data = getHistoricalData(mappedMetricData);
            val forecastingDetector = (ForecastingDetector) detector;
            val metricDefinition = mappedMetricData.getMetricData().getMetricDefinition();
            populateForecastingDetectorWithHistoricalData(forecastingDetector, data, metricDefinition);
        }
    }

    private boolean isSeasonalNaiveDetector(Detector detector) {
        return detector instanceof ForecastingDetector && "seasonalnaive".equals(detector.getName());
    }

    private List<DataSourceResult> getHistoricalData(MappedMetricData mappedMetricData) {
        val target = MetricUtil.getDataRetrievalValueOrMetricKey(mappedMetricData);
        val client = getClient();
        val dataSource = makeSource(client);
        val earliest = PropertiesUtil.getValueFromProperty(EARLIEST_TIME);
        val maxDataPoints = Integer.parseInt(PropertiesUtil.getValueFromProperty(MAX_DATA_POINTS));
        return dataSource.getMetricData(earliest, maxDataPoints, target);
    }

    private GraphiteClient getClient() {
        val graphiteBaseUri = PropertiesUtil.getValueFromProperty(BASE_URI);
        return makeClient(graphiteBaseUri);
    }

    //TODO. Using one-line methods for object creation to support unit testing. We can replace this with factories later on.
    // https://github.com/mockito/mockito/wiki/Mocking-Object-Creation#pattern-1---using-one-line-methods-for-object-creation
    GraphiteClient makeClient(String graphiteBaseUri) {
        return new GraphiteClient(graphiteBaseUri, new HttpClientWrapper(), new ObjectMapper());
    }

    DataSource makeSource(GraphiteClient client) {
        return new GraphiteSource(client);
    }

    private void populateForecastingDetectorWithHistoricalData(ForecastingDetector forecastingDetector, List<DataSourceResult> data, MetricDefinition metricDefinition) {
        for (DataSourceResult dataSourceResult : data) {
            val metricData = dataSourceResultToMetricData(dataSourceResult, metricDefinition);
            forecastingDetector.getPointForecaster().forecast(metricData);
        }
    }

    private MetricData dataSourceResultToMetricData(DataSourceResult dataSourceResult, MetricDefinition metricDefinition) {
        val dataPoint = dataSourceResult.getDataPoint();
        val epochSecond = dataSourceResult.getEpochSecond();
        return new MetricData(metricDefinition, dataPoint, epochSecond);
    }
}
