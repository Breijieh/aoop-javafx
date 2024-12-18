package com.assignment2.util;

/**
 * Utility class for data parsing and formatting.
 */
public class DataUtil {

    /**
     * Converts a string to title case.
     *
     * @param input The input string.
     * @return The title-cased string.
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '_') {
                nextTitleCase = true;
                titleCase.append(' ');
            } else {
                if (nextTitleCase) {
                    titleCase.append(Character.toUpperCase(c));
                    nextTitleCase = false;
                } else {
                    titleCase.append(Character.toLowerCase(c));
                }
            }
        }
        return titleCase.toString();
    }

    /**
     * Safely parses an Object to a double. Returns 0.0 if parsing fails or value is
     * null.
     *
     * @param value The value to parse.
     * @return The parsed double value or 0.0.
     */
    public static double parseSafeDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
