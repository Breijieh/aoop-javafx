package com.assignment2.parser;

/**
 * Custom exception for CSV parsing errors.
 */
public class CSVParsingException extends Exception {
    public CSVParsingException(String message) {
        super(message);
    }

    public CSVParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
