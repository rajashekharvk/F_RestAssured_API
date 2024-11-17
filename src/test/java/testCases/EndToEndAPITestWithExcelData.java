package testCases;

import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import net.minidev.json.JSONArray;
import pojos.Booking;
import pojos.BookingDates;
import testBase.BaseTest;
import utilities.FileNameConstants;

public class EndToEndAPITestWithExcelData extends BaseTest {

	private static final Logger logger = LogManager.getLogger(EndToEndAPITestWithExcelData.class);
	//private static final Logger logger = LogManager.getLogger(AllureReportGeneration.class);

	@Story("Story 1")
	//@Test(description = "end to end api testing")
	@Description("end to end testing")
	@Severity(SeverityLevel.CRITICAL)

	@Test(description = "end to end api testing",dataProvider = "ExcelTestData")
	public void e2eAPIRequest(Map<String, String> testdata) {

		logger.info("e2eAPIRequest test execution started...");
		int totalproce=Integer.parseInt(testdata.get("TotalPrice"));

		try {
			
			BookingDates bookingdates=new BookingDates(testdata.get("checkin"),testdata.get("checkout"));
			Booking booking =new Booking(testdata.get("FirstName"),
											testdata.get("LastName")
											,testdata.get("additionalneeds"),
											totalproce,
											true,
											bookingdates);
			
			//Serialization
			ObjectMapper objectMapper =new ObjectMapper();
			String requestbody =objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(booking);
//			System.out.println(requestbody);
			
			// de-serialization
			Booking bookingDetails = objectMapper.readValue(requestbody, Booking.class);
			System.out.println(bookingDetails.getFirstname());
			System.out.println(bookingDetails.getTotalprice());
			System.out.println(bookingDetails.getBookingdates().getCheckin());
			System.out.println(bookingDetails.getBookingdates().getCheckout());
			
			String tokenAPIRequestBody = FileUtils.readFileToString(new File(FileNameConstants.TOKEN_API_REQUEST_BODY),
					"UTF-8");
			String jsonSchema = FileUtils.readFileToString(new File(FileNameConstants.JSON_SCHEMA),"UTF-8");
			String putAPIRequestBody = FileUtils.readFileToString(new File(FileNameConstants.PUT_API_REQUEST_BODY),
					"UTF-8");
			
			
			// post api call
			Response response = 
					RestAssured
					.given()
						.filter(new AllureRestAssured()) //new RestAssuredListener())
						.contentType(ContentType.JSON)
						.body(requestbody)  //postAPIRequestBody)
						.baseUri("https://restful-booker.herokuapp.com/booking")
					.when()
						.post()
					.then()
						.assertThat()
						.statusCode(200)
					.extract()
						.response();

			JSONArray jsonArray = JsonPath.read(response.body().asString(), "$.booking..firstname");
			String ActualfirstName = (String) jsonArray.get(0);
				   ActualfirstName = jsonArray.get(0).toString();
			Assert.assertEquals(ActualfirstName,testdata.get("FirstName").toString() );  //"rest assured  by"); //"api testing");
			Assert.assertEquals(ActualfirstName, bookingDetails.getFirstname()); //deserialization

			int bookingId = JsonPath.read(response.body().asString(), "$.bookingid");
			System.out.println("Booking Id : " + bookingId);

			// get api call
			RestAssured
				.given()
					.filter(new AllureRestAssured())   //.filter(new RestAssuredListener())
					.contentType(ContentType.JSON)
					.baseUri("https://restful-booker.herokuapp.com/booking")
				.when()
					.get("/{bookingId}", bookingId)
				.then()
					.assertThat()
					.statusCode(200)
					.statusLine("HTTP/1.1 200 OK")
					.header("Content-Type", "application/json; charset=utf-8")
					.body(JsonSchemaValidator.matchesJsonSchema(jsonSchema))
				.extract()
					.response();

				Assert.assertTrue(response.getBody().asString().contains("bookingid"));

		// token generation
			Response tokenAPIResponse = RestAssured
					.given()
					.filter(new AllureRestAssured())   //.filter(new RestAssuredListener())
						.contentType(ContentType.JSON)
						.body(tokenAPIRequestBody)
						.baseUri("https://restful-booker.herokuapp.com/auth")
					.when()
						.post()
					.then()
						.assertThat()
						.statusCode(200)
					.extract()
						.response();

			String token = JsonPath.read(tokenAPIResponse.body().asString(), "$.token");
			System.out.println("Token Id : " + token);

			// put api call
			RestAssured
			.given()
			.filter(new AllureRestAssured())   //.filter(new RestAssuredListener())
				.contentType(ContentType.JSON)
				.body(putAPIRequestBody)
				.header("Cookie", "token=" + token)
				.baseUri("https://restful-booker.herokuapp.com/booking")
			.when()
				.put("/{bookingId}", bookingId)
			.then()
				.assertThat()
				.statusCode(200)
				.body("firstname", equalTo("Specflow"))
				.body("lastname", equalTo("Selenium C#"));

			// patch api call
			RestAssured
			.given()
			.filter(new AllureRestAssured())   //.filter(new RestAssuredListener())
				.contentType(ContentType.JSON)
				.body(requestbody)  //(patchAPIRequestBody)
				.header("Cookie", "token=" + token)
				.baseUri("https://restful-booker.herokuapp.com/booking")
			.when()
				.patch("/{bookingId}", bookingId)
			.then()
				.assertThat()
				.statusCode(200)
				.body("firstname", equalTo("Testers Talk"));

			// delete api call
			RestAssured
			.given()
			.filter(new AllureRestAssured())   //.filter(new RestAssuredListener())
				.contentType(ContentType.JSON)
				.header("Cookie", "token=" + token)
				.baseUri("https://restful-booker.herokuapp.com/booking")
			.when()
				.delete("/{bookingId}", bookingId)
			.then()
				.assertThat()
				.statusCode(201);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("e2eAPIRequest test execution ended...");

	}

}
