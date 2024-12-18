package com.assignment2.model;

import com.assignment2.util.DataUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single row of data with fields and their corresponding types.
 */
public class DataRow {
    private Map<String, Object> fields = new HashMap<>();
    private Map<String, DataType> fieldTypes = new HashMap<>();

    public enum DataType {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        DATE // Extend as needed
    }

    /**
     * Adds a field to the DataRow with type inference.
     *
     * @param key   The field name.
     * @param value The field value as a string.
     */
    public void addField(String key, String value) {
        Object parsedValue = parseValue(value);
        fields.put(key, parsedValue);
        fieldTypes.put(key, determineDataType(parsedValue));
    }

    /**
     * Parses a string value into an appropriate data type.
     *
     * @param value The value as a string.
     * @return The parsed value as an Object.
     */
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // Attempt to parse as Integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Not an Integer
        }
        // Attempt to parse as Double
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Not a Double
        }
        // Attempt to parse as Boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        // Return as String
        return value;
    }

    /**
     * Determines the data type of a value.
     *
     * @param value The value as an Object.
     * @return The DataType enum representing the type.
     */
    private DataType determineDataType(Object value) {
        if (value instanceof Integer) {
            return DataType.INTEGER;
        } else if (value instanceof Double) {
            return DataType.DOUBLE;
        } else if (value instanceof Boolean) {
            return DataType.BOOLEAN;
        } else {
            return DataType.STRING;
        }
    }

    /**
     * Retrieves the value of a field.
     *
     * @param key The field name.
     * @return The field value as an Object.
     */
    public Object getField(String key) {
        return fields.get(key);
    }

    /**
     * Retrieves the data type of a field.
     *
     * @param key The field name.
     * @return The DataType enum.
     */
    public DataType getFieldType(String key) {
        return fieldTypes.get(key);
    }

    /**
     * Retrieves all fields.
     *
     * @return A map of field names to their values.
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    /**
     * Retrieves all field types.
     *
     * @return A map of field names to their DataType.
     */
    public Map<String, DataType> getFieldTypes() {
        return fieldTypes;
    }

    /**
     * Checks if a field exists.
     *
     * @param key The field name.
     * @return True if the field exists, else false.
     */
    public boolean hasField(String key) {
        return fields.containsKey(key);
    }
}
