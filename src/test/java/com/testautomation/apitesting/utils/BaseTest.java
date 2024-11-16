package com.testautomation.apitesting.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;

import io.restassured.RestAssured;

public class BaseTest {
	
	private static final Logger logger = LogManager.getLogger(BaseTest.class);
	
	@BeforeMethod
	public void beforeMethod() {
		//RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}
	
	@AfterMethod
	public void afterMethod(ITestResult result) {
		if(result.getStatus() == ITestResult.FAILURE) {
			Throwable t = result.getThrowable();
			StringWriter error = new StringWriter();
			t.printStackTrace(new PrintWriter(error));
			logger.info(error.toString());
		}
		
	}
	
	@DataProvider(name = "ExcelTestData")
	public Object[][] getTestData(){
		
		String query = "select * from Sheet1 where Run='Yes'";
		
		Object[][] objArray = null;
		Map<String,String> testData = null;
		List<Map<String,String>> testDataList = null;
		
		Fillo fillo = new Fillo();
		Connection connection = null;
		Recordset recordset = null;
		
		try {
			connection = fillo.getConnection(FileNameConstants.EXCEL_TEST_DATA);
			recordset = connection.executeQuery(query);
			
			testDataList = new ArrayList<Map<String,String>>();
			
			while(recordset.next()) {
				testData = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
				
				for (String field : recordset.getFieldNames()) {
					testData.put(field, recordset.getField(field));
				}
				
				testDataList.add(testData);
			}
			
			objArray = new Object[testDataList.size()][1];
			
			for (int i = 0; i < testDataList.size(); i++) {
				objArray[i][0] = testDataList.get(i);
			}
			
		} catch (FilloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objArray;
	}
	
}
