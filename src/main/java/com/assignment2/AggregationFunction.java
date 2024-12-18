package com.assignment2;

import com.assignment2.util.DataUtil;

/**
 * Represents an aggregation function applied to a specific column.
 */
public class AggregationFunction {

    private String function;
    private String column;

    public AggregationFunction(String function, String column) {
        this.function = function;
        this.column = column;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return function + "(" + DataUtil.toTitleCase(column) + ")";
    }
}
