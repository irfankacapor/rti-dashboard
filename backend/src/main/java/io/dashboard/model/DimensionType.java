package io.dashboard.model;

public enum DimensionType {
    TIME,           // Temporal dimension (years, months, dates)
    LOCATION,       // Geographic dimension (country, state, city)
    INDICATOR_NAME, // Indicator names/descriptions
    INDICATOR_VALUE, // Numeric values/measurements
    SOURCE,         // Data source information
    UNIT,           // Measurement units
    GOAL,           // Target/goal values
    ADDITIONAL      // Custom/additional dimensions
} 