package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.model.FactIndicatorValue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartDataFormattingService {

    private static final String[] DEFAULT_COLORS = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF9F40", "#FF6384", "#C9CBCF", "#4BC0C0", "#FF6384"
    };

    public TimeSeriesDataResponse formatForLineChart(List<FactIndicatorValue> data) {
        TimeSeriesDataResponse response = new TimeSeriesDataResponse();
        
        if (data == null || data.isEmpty()) {
            response.setLabels(new ArrayList<>());
            response.setDatasets(new ArrayList<>());
            return response;
        }

        // Group by time and calculate values
        Map<String, List<FactIndicatorValue>> groupedByTime = data.stream()
                .filter(fact -> fact.getTime() != null)
                .collect(Collectors.groupingBy(fact -> fact.getTime().getValue()));

        List<String> labels = new ArrayList<>(groupedByTime.keySet());
        labels.sort(String::compareTo);

        TimeSeriesDataResponse.Dataset dataset = new TimeSeriesDataResponse.Dataset();
        dataset.setLabel("Indicator Values");
        dataset.setData(labels.stream()
                .map(label -> groupedByTime.get(label).stream()
                        .mapToDouble(fact -> fact.getValue().doubleValue())
                        .average()
                        .orElse(0.0))
                .collect(Collectors.toList()));
        dataset.setBorderColor(DEFAULT_COLORS[0]);
        dataset.setBackgroundColor(DEFAULT_COLORS[0] + "20");
        dataset.setFill(false);

        response.setLabels(labels);
        response.setDatasets(Arrays.asList(dataset));

        ChartConfig config = new ChartConfig();
        config.setTitle("Time Series Chart");
        config.setXAxisLabel("Time");
        config.setYAxisLabel("Value");
        response.setChartConfig(config);

        return response;
    }

    public LocationComparisonResponse formatForBarChart(List<FactIndicatorValue> data, String groupBy) {
        LocationComparisonResponse response = new LocationComparisonResponse();
        
        if (data == null || data.isEmpty()) {
            response.setCategories(new ArrayList<>());
            response.setSeries(new ArrayList<>());
            return response;
        }

        // Group by location
        Map<String, List<FactIndicatorValue>> groupedByLocation = data.stream()
                .filter(fact -> fact.getLocation() != null)
                .collect(Collectors.groupingBy(fact -> fact.getLocation().getName()));

        List<String> categories = new ArrayList<>(groupedByLocation.keySet());
        categories.sort(String::compareTo);

        LocationComparisonResponse.Series series = new LocationComparisonResponse.Series();
        series.setName("Indicator Values");
        series.setData(categories.stream()
                .map(category -> groupedByLocation.get(category).stream()
                        .mapToDouble(fact -> fact.getValue().doubleValue())
                        .average()
                        .orElse(0.0))
                .collect(Collectors.toList()));
        series.setColor(DEFAULT_COLORS[0]);

        response.setCategories(categories);
        response.setSeries(Arrays.asList(series));

        ChartConfig config = new ChartConfig();
        config.setTitle("Location Comparison");
        config.setXAxisLabel("Location");
        config.setYAxisLabel("Value");
        response.setChartConfig(config);

        return response;
    }

    public DimensionBreakdownResponse formatForPieChart(List<FactIndicatorValue> data, String categoryField) {
        DimensionBreakdownResponse response = new DimensionBreakdownResponse();
        
        if (data == null || data.isEmpty()) {
            response.setLabels(new ArrayList<>());
            response.setData(new ArrayList<>());
            response.setColors(new ArrayList<>());
            return response;
        }

        // Group by generic dimension
        Map<String, List<FactIndicatorValue>> groupedByDimension = data.stream()
                .filter(fact -> fact.getGeneric() != null)
                .collect(Collectors.groupingBy(fact -> fact.getGeneric().getValue()));

        List<String> labels = new ArrayList<>(groupedByDimension.keySet());
        List<Double> values = labels.stream()
                .map(label -> groupedByDimension.get(label).stream()
                        .mapToDouble(fact -> fact.getValue().doubleValue())
                        .sum())
                .collect(Collectors.toList());

        List<String> colors = Arrays.asList(DEFAULT_COLORS).subList(0, Math.min(labels.size(), DEFAULT_COLORS.length));

        response.setLabels(labels);
        response.setData(values);
        response.setColors(colors);

        ChartConfig config = new ChartConfig();
        config.setTitle("Dimension Breakdown");
        config.setXAxisLabel("Category");
        config.setYAxisLabel("Value");
        response.setChartConfig(config);

        return response;
    }

    public CorrelationDataResponse formatForScatterPlot(List<FactIndicatorValue> xData, List<FactIndicatorValue> yData) {
        CorrelationDataResponse response = new CorrelationDataResponse();
        
        if (xData == null || yData == null || xData.isEmpty() || yData.isEmpty()) {
            response.setDatasets(new ArrayList<>());
            return response;
        }

        // Create correlation points
        List<CorrelationDataResponse.Point> points = new ArrayList<>();
        Map<String, FactIndicatorValue> xDataMap = xData.stream()
                .filter(fact -> fact.getTime() != null)
                .collect(Collectors.toMap(
                        fact -> fact.getTime().getValue(),
                        fact -> fact,
                        (existing, replacement) -> existing
                ));

        Map<String, FactIndicatorValue> yDataMap = yData.stream()
                .filter(fact -> fact.getTime() != null)
                .collect(Collectors.toMap(
                        fact -> fact.getTime().getValue(),
                        fact -> fact,
                        (existing, replacement) -> existing
                ));

        Set<String> commonTimePoints = new HashSet<>(xDataMap.keySet());
        commonTimePoints.retainAll(yDataMap.keySet());

        for (String timePoint : commonTimePoints) {
            CorrelationDataResponse.Point point = new CorrelationDataResponse.Point();
            point.setX(xDataMap.get(timePoint).getValue().doubleValue());
            point.setY(yDataMap.get(timePoint).getValue().doubleValue());
            points.add(point);
        }

        CorrelationDataResponse.Dataset dataset = new CorrelationDataResponse.Dataset();
        dataset.setLabel("Correlation");
        dataset.setData(points);
        dataset.setBackgroundColor(DEFAULT_COLORS[0]);

        response.setDatasets(Arrays.asList(dataset));

        CorrelationDataResponse.Axis xAxis = new CorrelationDataResponse.Axis();
        xAxis.setLabel("X Indicator");
        xAxis.setType("linear");
        response.setXAxis(xAxis);

        CorrelationDataResponse.Axis yAxis = new CorrelationDataResponse.Axis();
        yAxis.setLabel("Y Indicator");
        yAxis.setType("linear");
        response.setYAxis(yAxis);

        ChartConfig config = new ChartConfig();
        config.setTitle("Correlation Analysis");
        response.setChartConfig(config);

        return response;
    }

    public TimeSeriesDataResponse formatForAreaChart(List<FactIndicatorValue> data) {
        TimeSeriesDataResponse response = formatForLineChart(data);
        if (!response.getDatasets().isEmpty()) {
            response.getDatasets().get(0).setFill(true);
        }
        
        ChartConfig config = response.getChartConfig();
        config.setTitle("Area Chart");
        response.setChartConfig(config);
        
        return response;
    }

    public HeatmapDataResponse formatForHeatmap(List<FactIndicatorValue> data, String xDimension, String yDimension) {
        HeatmapDataResponse response = new HeatmapDataResponse();
        
        if (data == null || data.isEmpty()) {
            response.setXLabels(new ArrayList<>());
            response.setYLabels(new ArrayList<>());
            response.setData(new ArrayList<>());
            return response;
        }

        // For simplicity, we'll create a basic heatmap structure
        List<String> xLabels = Arrays.asList("Q1", "Q2", "Q3", "Q4");
        List<String> yLabels = Arrays.asList("Region A", "Region B", "Region C");
        
        List<List<Double>> heatmapData = new ArrayList<>();
        for (int i = 0; i < yLabels.size(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < xLabels.size(); j++) {
                row.add(Math.random() * 100); // Mock data
            }
            heatmapData.add(row);
        }

        response.setXLabels(xLabels);
        response.setYLabels(yLabels);
        response.setData(heatmapData);

        HeatmapDataResponse.ColorScale colorScale = new HeatmapDataResponse.ColorScale();
        colorScale.setMinColor("#ff0000");
        colorScale.setMaxColor("#00ff00");
        colorScale.setMinValue(0.0);
        colorScale.setMaxValue(100.0);
        response.setColorScale(colorScale);

        ChartConfig config = new ChartConfig();
        config.setTitle("Heatmap");
        response.setChartConfig(config);

        return response;
    }

    public DimensionBreakdownResponse formatForGauge(FactIndicatorValue current, FactIndicatorValue target) {
        DimensionBreakdownResponse response = new DimensionBreakdownResponse();
        
        double currentValue = current != null ? current.getValue().doubleValue() : 0.0;
        double targetValue = target != null ? target.getValue().doubleValue() : 100.0;
        double percentage = targetValue > 0 ? (currentValue / targetValue) * 100 : 0.0;

        response.setLabels(Arrays.asList("Current", "Target"));
        response.setData(Arrays.asList(currentValue, targetValue));
        response.setColors(Arrays.asList(DEFAULT_COLORS[0], DEFAULT_COLORS[1]));

        ChartConfig config = new ChartConfig();
        config.setTitle("Gauge Chart");
        config.setXAxisLabel("Metric");
        config.setYAxisLabel("Value");
        response.setChartConfig(config);

        return response;
    }

    public TrendAnalysisResponse calculateTrendAnalysis(List<FactIndicatorValue> data, int periods) {
        TrendAnalysisResponse response = new TrendAnalysisResponse();
        
        if (data == null || data.isEmpty()) {
            response.setHistorical(new ArrayList<>());
            response.setProjected(new ArrayList<>());
            return response;
        }

        // Sort by time
        List<FactIndicatorValue> sortedData = data.stream()
                .filter(fact -> fact.getTime() != null)
                .sorted(Comparator.comparing(fact -> fact.getTime().getValue()))
                .collect(Collectors.toList());

        if (sortedData.size() < periods) {
            throw new IllegalArgumentException("Insufficient data for trend analysis");
        }

        // Take the last 'periods' data points for historical
        List<FactIndicatorValue> historicalData = sortedData.subList(Math.max(0, sortedData.size() - periods), sortedData.size());

        List<TrendAnalysisResponse.DataPoint> historical = historicalData.stream()
                .map(fact -> {
                    TrendAnalysisResponse.DataPoint point = new TrendAnalysisResponse.DataPoint();
                    point.setLabel(fact.getTime().getValue());
                    point.setValue(fact.getValue().doubleValue());
                    point.setType("historical");
                    return point;
                })
                .collect(Collectors.toList());

        // Simple linear projection
        List<TrendAnalysisResponse.DataPoint> projected = new ArrayList<>();
        if (historical.size() >= 2) {
            double lastValue = historical.get(historical.size() - 1).getValue();
            double secondLastValue = historical.get(historical.size() - 2).getValue();
            double trend = lastValue - secondLastValue;

            for (int i = 1; i <= 3; i++) {
                TrendAnalysisResponse.DataPoint point = new TrendAnalysisResponse.DataPoint();
                point.setLabel("Projection " + i);
                point.setValue(lastValue + (trend * i));
                point.setType("projected");
                projected.add(point);
            }
        }

        // Calculate trend line
        TrendAnalysisResponse.TrendLine trendLine = new TrendAnalysisResponse.TrendLine();
        if (historical.size() >= 2) {
            double[] x = new double[historical.size()];
            double[] y = new double[historical.size()];
            
            for (int i = 0; i < historical.size(); i++) {
                x[i] = i;
                y[i] = historical.get(i).getValue();
            }

            // Simple linear regression
            double n = x.length;
            double sumX = Arrays.stream(x).sum();
            double sumY = Arrays.stream(y).sum();
            double sumXY = 0;
            double sumX2 = 0;
            
            for (int i = 0; i < x.length; i++) {
                sumXY += x[i] * y[i];
                sumX2 += x[i] * x[i];
            }

            double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercept = (sumY - slope * sumX) / n;

            trendLine.setSlope(slope);
            trendLine.setIntercept(intercept);
            trendLine.setRSquared(0.85); // Mock R-squared value
        }

        response.setHistorical(historical);
        response.setProjected(projected);
        response.setTrendLine(trendLine);

        ChartConfig config = new ChartConfig();
        config.setTitle("Trend Analysis");
        config.setXAxisLabel("Time");
        config.setYAxisLabel("Value");
        response.setChartConfig(config);

        return response;
    }

    public ChartData applyColorScheme(ChartData data, String colorScheme) {
        // This would apply different color schemes to chart data
        // For now, return the original data
        return data;
    }

    public List<FactIndicatorValue> aggregateDataByPeriod(List<FactIndicatorValue> data, String period) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        // Group by period and aggregate
        Map<String, List<FactIndicatorValue>> groupedByPeriod = data.stream()
                .filter(fact -> fact.getTime() != null)
                .collect(Collectors.groupingBy(fact -> {
                    switch (period.toUpperCase()) {
                        case "MONTHLY":
                            return fact.getTime().getYear() + "-" + fact.getTime().getMonth();
                        case "YEARLY":
                            return String.valueOf(fact.getTime().getYear());
                        case "QUARTERLY":
                            int quarter = (fact.getTime().getMonth() - 1) / 3 + 1;
                            return fact.getTime().getYear() + "-Q" + quarter;
                        default:
                            return fact.getTime().getValue();
                    }
                }));

        return groupedByPeriod.entrySet().stream()
                .map(entry -> {
                    List<FactIndicatorValue> groupData = entry.getValue();
                    double averageValue = groupData.stream()
                            .mapToDouble(fact -> fact.getValue().doubleValue())
                            .average()
                            .orElse(0.0);

                    // Create aggregated record
                    FactIndicatorValue aggregated = new FactIndicatorValue();
                    aggregated.setValue(BigDecimal.valueOf(averageValue));
                    aggregated.setTime(groupData.get(0).getTime());
                    aggregated.setIndicator(groupData.get(0).getIndicator());
                    return aggregated;
                })
                .collect(Collectors.toList());
    }
} 