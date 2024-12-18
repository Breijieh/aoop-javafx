package com.assignment2.analytics;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Generic Analytics class for performing operations on data.
 *
 * @param <T> The type of data records.
 */
public class Analytics<T> {
    private final List<T> data;

    public Analytics(List<T> records) {
        this.data = new ArrayList<>(records);
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    public Analytics<T> filter(Predicate<T> predicate) {
        List<T> filtered = data.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return new Analytics<>(filtered);
    }

    /**
     * Sorts the data based on the provided comparator.
     *
     * @param comparator Comparator to define the sort order.
     * @return A new Analytics instance containing the sorted data.
     */
    public Analytics<T> sortBy(Comparator<T> comparator) {
        List<T> sorted = data.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
        return new Analytics<>(sorted);
    }

    /**
     * Groups the data based on the provided key mapper and applies the given
     * collector.
     *
     * @param keyMapper Function to extract the grouping key from each record.
     * @param collector Collector to apply to the grouped data.
     * @param <K>       Type of the grouping key.
     * @param <U>       Type of the collector result.
     * @return A map of grouped keys to the collector results.
     */
    public <K, U> Map<K, U> groupBy(
            Function<T, K> keyMapper,
            Collector<T, ?, U> collector) {
        return data.stream()
                .collect(Collectors.groupingBy(keyMapper, collector));
    }

    /**
     * Calculates the sum of a numeric field mapped by the provided function.
     *
     * @param mapper Function to extract the numeric value from each record.
     * @return The sum of the mapped numeric values.
     */
    public double sum(ToDoubleFunction<T> mapper) {
        return data.stream()
                .mapToDouble(mapper)
                .sum();
    }

    /**
     * Calculates the average of a numeric field mapped by the provided function.
     *
     * @param mapper Function to extract the numeric value from each record.
     * @return The average of the mapped numeric values, or 0.0 if no data is
     *         present.
     */
    public double average(ToDoubleFunction<T> mapper) {
        return data.stream()
                .mapToDouble(mapper)
                .average()
                .orElse(0.0);
    }

    /**
     * Finds the maximum value of a numeric field mapped by the provided function.
     *
     * @param mapper Function to extract the numeric value from each record.
     * @return An Optional containing the maximum value, or empty if no data is
     *         present.
     */
    public Optional<Double> max(ToDoubleFunction<T> mapper) {
        OptionalDouble max = data.stream()
                .mapToDouble(mapper)
                .max();
        return max.isPresent() ? Optional.of(max.getAsDouble()) : Optional.empty();
    }

    /**
     * Finds the minimum value of a numeric field mapped by the provided function.
     *
     * @param mapper Function to extract the numeric value from each record.
     * @return An Optional containing the minimum value, or empty if no data is
     *         present.
     */
    public Optional<Double> min(ToDoubleFunction<T> mapper) {
        OptionalDouble min = data.stream()
                .mapToDouble(mapper)
                .min();
        return min.isPresent() ? Optional.of(min.getAsDouble()) : Optional.empty();
    }

    /**
     * Counts the number of records that match the provided predicate.
     *
     * @param predicate Predicate to apply for counting.
     * @return The count of matching records.
     */
    public long count(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .count();
    }

    /**
     * Maps the data to a different type using the provided mapper function.
     *
     * @param mapper Function to transform each record.
     * @param <R>    Type of the resulting records.
     * @return A new Analytics instance containing the mapped data.
     */
    public <R> Analytics<R> map(Function<T, R> mapper) {
        List<R> mapped = data.stream()
                .map(mapper)
                .collect(Collectors.toList());
        return new Analytics<>(mapped);
    }

    /**
     * Reduces the data into a single result using the provided functions.
     *
     * @param identity    The initial value for the reduction.
     * @param accumulator Function to incorporate each record into the result.
     * @param combiner    Function to combine partial results.
     * @param <R>         Type of the result.
     * @return The reduced result.
     */
    public <R> R reduce(R identity, BiFunction<R, T, R> accumulator, BinaryOperator<R> combiner) {
        return data.stream()
                .reduce(identity, accumulator, combiner);
    }

    /**
     * Collects the data using the provided collector.
     *
     * @param collector Collector to apply to the data.
     * @param <R>       Type of the collected result.
     * @return The collected result.
     */
    public <R> R collect(Collector<T, ?, R> collector) {
        return data.stream()
                .collect(collector);
    }

    /**
     * Finds the first record matching the provided predicate.
     *
     * @param predicate Predicate to apply for finding.
     * @return An Optional containing the first matching record, or empty if none
     *         found.
     */
    public Optional<T> findFirst(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Finds any record matching the provided predicate.
     *
     * @param predicate Predicate to apply for finding.
     * @return An Optional containing a matching record, or empty if none found.
     */
    public Optional<T> findAny(Predicate<T> predicate) {
        return data.stream()
                .filter(predicate)
                .findAny();
    }

    /**
     * Checks if all records match the provided predicate.
     *
     * @param predicate Predicate to apply.
     * @return True if all records match, else false.
     */
    public boolean allMatch(Predicate<T> predicate) {
        return data.stream()
                .allMatch(predicate);
    }

    /**
     * Checks if any record matches the provided predicate.
     *
     * @param predicate Predicate to apply.
     * @return True if any record matches, else false.
     */
    public boolean anyMatch(Predicate<T> predicate) {
        return data.stream()
                .anyMatch(predicate);
    }

    /**
     * Checks if no records match the provided predicate.
     *
     * @param predicate Predicate to apply.
     * @return True if no records match, else false.
     */
    public boolean noneMatch(Predicate<T> predicate) {
        return data.stream()
                .noneMatch(predicate);
    }
}
