package com.example.freecycle;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FreecycleEndToEndTests {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void fullItemTransferFlowWorks() {
        long donorId = createUser("donor@example.com", "password123", "Donor", "User", "555-1000", "123 Donor Way");
        long recipientOneId = createUser("recipient1@example.com", "password123", "Recipient", "One", "555-2000", "456 Recipient Rd");
        long recipientTwoId = createUser("recipient2@example.com", "password123", "Recipient", "Two", "555-3000", "789 Recipient Ln");

        String donorToken = loginAndGetJwt("donor@example.com", "password123");
        String recipientOneToken = loginAndGetJwt("recipient1@example.com", "password123");
        String recipientTwoToken = loginAndGetJwt("recipient2@example.com", "password123");

        long itemId = createItem(donorToken, "Mountain Bike", "A gently used mountain bike.", "Sports", "Good", "Medium", 1L);

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("title", hasItem("Mountain Bike"));

        long interestOneId = expressInterest(recipientOneToken, itemId, "I would love to take this bike.");
        long interestTwoId = expressInterest(recipientTwoToken, itemId, "This would be perfect for my commute.");

        given()
                .auth().oauth2(donorToken)
                .when()
                .post("/api/items/" + itemId + "/select/" + interestTwoId)
                .then()
                .statusCode(200)
                .body("state", equalTo("PENDING"));

        long siteId = createTransferSite(donorToken,
                "Central Park Pickup",
                "100 Park Ave",
                "Metropolis",
                "NY",
                "10001",
                "Transfer Team",
                "555-4000",
                "Meet behind the fountain");

        long timeSlotId = createTimeSlot(donorToken, siteId,
                LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(1).withHour(16).withMinute(0).withSecond(0).withNano(0));

        given()
                .auth().oauth2(donorToken)
                .contentType(ContentType.JSON)
                .body(Map.of("timeSlotId", timeSlotId, "notes", "Meet in front of the entrance."))
                .when()
                .post("/api/items/" + itemId + "/schedule")
                .then()
                .statusCode(200)
                .body("status", equalTo("SCHEDULED"));

        given()
                .auth().oauth2(donorToken)
                .when()
                .post("/api/items/" + itemId + "/complete")
                .then()
                .statusCode(200)
                .body("state", equalTo("DONE"));
    }

    private long createUser(String email, String password, String firstName, String lastName, String phoneNumber, String address) {
        JsonPath response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", password,
                        "firstName", firstName,
                        "lastName", lastName,
                        "phoneNumber", phoneNumber,
                        "address", address
                ))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        return response.getLong("id");
    }

    private String loginAndGetJwt(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .asString();
    }

    private long createItem(String token, String title, String description, String category, String condition, String size, Long quantity) {
        JsonPath response = given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "donorId", null,
                        "title", title,
                        "description", description,
                        "category", category,
                        "condition", condition,
                        "size", size,
                        "quantity", quantity
                ))
                .when()
                .post("/api/items")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        return response.getLong("id");
    }

    private long expressInterest(String token, long itemId, String message) {
        JsonPath response = given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of("userId", null, "message", message))
                .when()
                .post("/api/items/" + itemId + "/interests")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        return response.getLong("id");
    }

    private long createTransferSite(String token,
                                   String name,
                                   String address,
                                   String city,
                                   String state,
                                   String zipCode,
                                   String contactName,
                                   String phoneNumber,
                                   String description) {
        JsonPath response = given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", name,
                        "address", address,
                        "city", city,
                        "state", state,
                        "zipCode", zipCode,
                        "contactName", contactName,
                        "phoneNumber", phoneNumber,
                        "email", "contact@transfer.example.com",
                        "description", description
                ))
                .when()
                .post("/api/transfer-sites")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        return response.getLong("id");
    }

    private long createTimeSlot(String token, long siteId, LocalDateTime startTime, LocalDateTime endTime) {
        JsonPath response = given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "transferSiteId", siteId,
                        "startTime", startTime.toString(),
                        "endTime", endTime.toString(),
                        "maxCapacity", 5
                ))
                .when()
                .post("/api/time-slots")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath();

        return response.getLong("id");
    }
}
