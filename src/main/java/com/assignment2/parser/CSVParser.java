package com.assignment2.parser;

import com.assignment2.model.DataRow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parser class for reading and parsing CSV files into DataRow objects.
 */
public class CSVParser {
    private static final Logger logger = Logger.getLogger(CSVParser.class.getName());

    /**
     * Parses a CSV file into a list of DataRow objects with type inference.
     *
     * @param filePath  Path to the CSV file.
     * @param clazz     The class type of DataRow.
     * @param delimiter Delimiter used in the CSV file (e.g., ",").
     * @return List of DataRow objects.
     * @throws CSVParsingException If an error occurs during parsing.
     */
    public static List<DataRow> parseCSVSpecific(String filePath, Class<DataRow> clazz, String delimiter)
            throws CSVParsingException {
        List<DataRow> dataRows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new CSVParsingException("CSV file is empty.");
            }
            String[] headers = headerLine.split(delimiter);

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                DataRow dataRow = new DataRow();
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < values.length ? values[i].trim() : "";
                    dataRow.addField(key, value);
                }
                dataRows.add(dataRow);
            }
        } catch (IOException e) {
            logger.severe("Error reading CSV file: " + e.getMessage());
            throw new CSVParsingException("Error reading CSV file: " + e.getMessage());
        }
        return dataRows;
    }
}
