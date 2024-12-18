package com.assignment2;

import com.assignment2.analytics.Analytics;
import com.assignment2.model.DataRow;
import com.assignment2.AggregationFunction;
import com.assignment2.util.DataUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service class for analytics operations.
 */
public class AnalyticsService {
    private static final Logger logger = Logger.getLogger(AnalyticsService.class.getName());

    /**
     * Filters the analytics data based on the provided predicate.
     *
     * @param analytics The Analytics instance containing data.
     * @param predicate The predicate to apply for filtering.
     * @return A new Analytics instance containing the filtered data.
     */
    public static Analytics<DataRow> filter(Analytics<DataRow> analytics, Predicate<DataRow> predicate) {
        List<DataRow> filteredData = analytics.getData().stream()
                .filter(predicate)
                .collect(Collectors.toList());
        logger.info("Data filtered. Original size: " + analytics.getData().size() +
                ", Filtered size: " + filteredData.size());
        return new Analytics<>(filteredData);
    }

    /**
     * Performs group by with multiple aggregations.
     *
     * @param analytics     The Analytics instance containing data.
     * @param groupByColumn The column to group by.
     * @param aggregations  The list of aggregation functions.
     * @return A map of group by values to their aggregation results.
     */
    public static Map<Object, Map<String, Object>> performGroupByMultipleAggregations(
            Analytics<DataRow> analytics, String groupByColumn, List<AggregationFunction> aggregations) {

        // Group the data by the specified column
        Map<Object, List<DataRow>> groupedData = analytics.groupBy(
                row -> row.getField(groupByColumn),
                Collectors.toList());

        // Prepare the result map
        Map<Object, Map<String, Object>> resultMap = new HashMap<>();

        for (Map.Entry<Object, List<DataRow>> entry : groupedData.entrySet()) {
            Object groupKey = entry.getKey();
            List<DataRow> groupRows = entry.getValue();

            Map<String, Object> aggResults = new HashMap<>();

            for (AggregationFunction aggFunc : aggregations) {
                String function = aggFunc.getFunction();
                String column = aggFunc.getColumn();
                String aggName = function + "(" + DataUtil.toTitleCase(column) + ")";

                switch (function.toLowerCase()) {
                    case "count":
                        aggResults.put(aggName, (long) groupRows.size());
                        break;
                    case "sum":
                        double sum = groupRows.stream()
                                .mapToDouble(row -> DataUtil.parseSafeDouble(row.getField(column)))
                                .sum();
                        aggResults.put(aggName, sum);
                        break;
                    case "average":
                        double average = groupRows.stream()
                                .mapToDouble(row -> DataUtil.parseSafeDouble(row.getField(column)))
                                .average()
                                .orElse(0.0);
                        aggResults.put(aggName, average);
                        break;
                    case "max":
                        double max = groupRows.stream()
                                .mapToDouble(row -> DataUtil.parseSafeDouble(row.getField(column)))
                                .max()
                                .orElse(0.0);
                        aggResults.put(aggName, max);
                        break;
                    case "min":
                        double min = groupRows.stream()
                                .mapToDouble(row -> DataUtil.parseSafeDouble(row.getField(column)))
                                .min()
                                .orElse(0.0);
                        aggResults.put(aggName, min);
                        break;
                    case "list":
                        // Assuming "list" is a valid aggregation function for collecting unique values
                        List<String> list = groupRows.stream()
                                .map(row -> row.getField(column).toString())
                                .distinct()
                                .collect(Collectors.toList());
                        aggResults.put(aggName, String.join(", ", list));
                        break;
                    default:
                        logger.warning("Unsupported aggregation function: " + function);
                        aggResults.put(aggName, null);
                        break;
                }
            }

            resultMap.put(groupKey, aggResults);
        }

        return resultMap;
    }

    /**
     * Performs statistical operations on the analytics data.
     *
     * @param analytics The Analytics instance containing data.
     * @param operation The statistical operation to perform ("Sum", "Average",
     *                  "Max", "Min").
     * @param mapper    The function to extract the numeric value from each record.
     * @return The result of the statistical operation.
     * @throws IllegalArgumentException if the operation is unsupported or mapper is
     *                                  null.
     */
    public static double performStatistic(Analytics<DataRow> analytics, String operation,
            ToDoubleFunction<DataRow> mapper) {
        if (operation == null || mapper == null) {
            throw new IllegalArgumentException("Operation and mapper cannot be null.");
        }
        try {
            switch (operation.toLowerCase()) {
                case "sum":
                    return analytics.sum(mapper);
                case "average":
                    return analytics.average(mapper);
                case "max":
                    return analytics.max(mapper).orElse(Double.NaN);
                case "min":
                    return analytics.min(mapper).orElse(Double.NaN);
                default:
                    throw new IllegalArgumentException("Unsupported statistical operation: " + operation);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error performing statistic:", e);
            throw e;
        }
    }

    /**
     * Aggregates data for the Pie Chart.
     *
     * @param analytics      The Analytics instance containing data.
     * @param categoryColumn The column to group by (e.g., "Category").
     * @param valueColumn    The column to aggregate values from (e.g., "Value").
     * @return A map where keys are categories and values are aggregated sums.
     */
    public static Map<String, Double> aggregateForPieChart(Analytics<DataRow> analytics, String categoryColumn,
            String valueColumn) {
        return analytics.getData().stream()
                .filter(row -> row.hasField(categoryColumn) && row.hasField(valueColumn))
                .collect(Collectors.groupingBy(
                        row -> row.getField(categoryColumn).toString(),
                        Collectors.summingDouble(row -> {
                            Object val = row.getField(valueColumn);
                            if (val instanceof Number) {
                                return ((Number) val).doubleValue();
                            }
                            return 0.0;
                        })));
    }

    /**
     * Retrieves available numeric columns from the Analytics data.
     *
     * @param analytics The Analytics instance containing data.
     * @return A list of column names that are numeric.
     */
    public static List<String> getAvailableNumericColumns(Analytics<DataRow> analytics) {
        List<String> numericColumns = new ArrayList<>();
        if (!analytics.getData().isEmpty()) {
            DataRow firstRow = analytics.getData().get(0);
            for (String key : firstRow.getFields().keySet()) {
                Object value = firstRow.getField(key);
                if (value instanceof Number) {
                    numericColumns.add(key);
                }
            }
        }
        return numericColumns;
    }
}
