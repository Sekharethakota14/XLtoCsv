package com.mainfolder.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.exceptions.CsvException;

@RestController
public class ControllerClass {

	@Autowired
	private DataProcessor data;

	@PostMapping("/dataInsertion")
	public ResponseEntity<String> fetchingData() {
		String file1 = "C:\\Users\\91967\\Desktop\\input\\File A.csv";
		String file2 = "C:\\Users\\91967\\Desktop\\input\\File B.xlsx";
		String outputFilePath = "C:\\Users\\91967\\Desktop\\Output\\Output.csv";
		try {
			data.conversionofData(file1, file2, outputFilePath);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Converting the Data Successfully");
		} catch (CsvException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error is Occured while parsing Data");
		}


	}

}
