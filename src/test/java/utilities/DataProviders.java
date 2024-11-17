package utilities;

import java.io.IOException;

import org.testng.annotations.DataProvider;

public class DataProviders {

	@DataProvider(name = "LoginData",parallel = true)
	public String[][] getData() throws IOException {

		String path = ".\\testData\\OpenCart_LoginData.xlsx";
		ExcelUtility xlutil = new ExcelUtility(path);
		int totalrows = xlutil.getRowCount("Sheet1");
		int totalcols = xlutil.getCellCount("Sheet1", 1);
		String logindata[][] = new String[totalrows][totalcols];
		
//		System.out.println("totalrows "+totalrows);
//		System.out.println("totalcols"+totalcols);
//		System.out.println("Arraysize"+logindata.length);
		for (int i = 1; i <= totalrows; i++) {
			for (int j = 0; j < totalcols; j++) {
				logindata[i-1][j] = xlutil.getCellData("Sheet1", i, j);
//				System.out.println("i "+(i-1)+"J "+j);
//				System.out.println(logindata[i - 1][j].toString());
			}
		}
		return logindata;
	}

}
