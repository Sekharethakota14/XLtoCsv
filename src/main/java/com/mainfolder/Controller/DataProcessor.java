package com.mainfolder.Controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataProcessor {
	private static String extractValueFromList(List<String[]> data, String label) 
	{
		for (String[] row : data) {
			for (int i = 0; i < row.length; i++) {
				if (row[i] != null && row[i].contains(label)) 
				{
					return (i + 1 < row.length) ? row[i + 1] : "";
				}
			}
		}
		return "";
	}

	public void conversionofData(String file1, String file2, String outputFilePath) throws CsvException 
	{
		try {
			// Read the CSV file
			CSVReader csvReader = new CSVReader(new FileReader(file1));
			List<String[]> csvData = csvReader.readAll();
			csvReader.close();
			// Getting these 2 details from the csv file
			List<String[]> headerData = csvData.subList(0, 2);
			String depository = extractValueFromList(headerData, "Depository:");
			String mefbco = extractValueFromList(headerData, "MEFBCO");
			// Process the CSV data by skipping the first 3 rows
			List<String[]> parsedData = new ArrayList<>(csvData.subList(3, csvData.size()));
			parsedData.add(0, csvData.get(2));
			// Set the first row as header
			// Read the Excel file
			FileInputStream fis = new FileInputStream(file2);
			Workbook workbook = new XSSFWorkbook(fis);
			Sheet sheet = workbook.getSheetAt(0);
			List<List<String>> excelData = new ArrayList<>();

			for (Row row : sheet) {
				List<String> rowData = new ArrayList<>();
				for (Cell cell : row) {
					switch (cell.getCellType()) 
					{
					case STRING:
						rowData.add(cell.getStringCellValue());
						break;
					case NUMERIC:
						rowData.add(String.valueOf(cell.getNumericCellValue()));
						break;
					case BOOLEAN:
						rowData.add(String.valueOf(cell.getBooleanCellValue()));
						break;
					case FORMULA:
						rowData.add(cell.getCellFormula());
						break;
					default:
						rowData.add("");
					}
				}
				excelData.add(rowData);
			}
			workbook.close();
			fis.close();
			// Merge parsedData with excelData on the "Security" column
			List<String[]> mergedData = new ArrayList<>();
			String[] headers = parsedData.get(0);
			String[] newHeaders = Arrays.copyOf(headers, headers.length + 3);
			newHeaders[headers.length] = "Security Code";
			newHeaders[headers.length + 1] = "Depository";
			newHeaders[headers.length + 2] = "MEFBCO";
			mergedData.add(newHeaders);
			for (int i = 1; i < parsedData.size(); i++) {
				String[] csvRow = parsedData.get(i);
				String securityValue = csvRow[Arrays.asList(headers).indexOf("Security")];
				// Initialize the security code variable
				String securityCode = "  #N/A";
				for (List<String> excelRow : excelData) {
					if (excelRow.contains(securityValue)) {
						securityCode = excelRow.get(excelRow.indexOf(securityValue) + 1); // Get the corresponding
						break;
					}
				}
				String[] mergedRow = Arrays.copyOf(csvRow, csvRow.length + 3);
				mergedRow[csvRow.length] = securityCode;
				mergedRow[csvRow.length + 1] = depository;
				mergedRow[csvRow.length + 2] = mefbco;
				mergedData.add(mergedRow);
			}
			// Remove rows where 'ISIN' is NaN
			int isinIndex = Arrays.asList(newHeaders).indexOf("ISIN");
			mergedData.removeIf(row -> row[isinIndex].isEmpty());
			// Write the output to a new CSV file
			CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFilePath));
			csvWriter.writeAll(mergedData);
			csvWriter.close();
//			System.out.println("CSV file saved successfully as " + outputFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
