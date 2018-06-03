/*
 * Copyright 2018 Expedia Group, Inc.
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
package com.expedia.adaptivealerting.core.util;

/**
 * @author Willie Wheeler
 */
public class MetricPointTags {
    
    // TODO Probably want to namespace these tag names in some way to avoid conflicts. [WLW]
    public static final String OUTLIER_LEVEL = "outlierLevel";
    public static final String PREDICTION = "prediction";
    public static final String STRONG_THRESHOLD_UPPER = "strongThresholdUpper";
    public static final String STRONG_THRESHOLD_LOWER = "strongThresholdLower";
    public static final String WEAK_THRESHOLD_UPPER = "weakThresholdUpper";
    public static final String WEAK_THRESHOLD_LOWER = "weakThresholdLower";
}