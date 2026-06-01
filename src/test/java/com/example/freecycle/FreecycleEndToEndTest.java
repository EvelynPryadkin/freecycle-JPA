package com.example.freecycle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FreecycleEndToEndTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void donorCanPostSelectScheduleAndCompleteTransfer() {
        String suffix = uniqueSuffix();
        String donorEmail = "donor-" + suffix + "@example.com";
        String recipientOneEmail = "recipient1-" + suffix + "@example.com";
        String recipientTwoEmail = "recipient2-" + suffix + "@example.com";

        Long donorId = createUser(donorEmail, "Dana", "Donor");
        Long recipientOneId = createUser(recipientOneEmail, "Riley", "Recipient");
        Long recipientTwoId = createUser(recipientTwoEmail, "Casey", "Recipient");

        String donorToken = login(donorEmail);
        String recipientOneToken = login(recipientOneEmail);
        String recipientTwoToken = login(recipientTwoEmail);

        Long itemId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
                .body(Map.of(
                        "donorId", recipientOneId,
                        "title", "Desk lamp",
                        "description", "Working desk lamp with LED bulb",
                        "category", "Household",
                        "condition", "Good",
                        "size", "Small",
                        "quantity", 1
                ))
                .when()
                .post("/api/items")
                .then()
                .statusCode(201)
                .body("title", equalTo("Desk lamp"))
                .body("state", equalTo("POSTED"))
                .body("donor.id", equalTo(donorId.intValue()))
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .when()
                .get("/api/items?state=POSTED")
                .then()
                .statusCode(200)
                .body("id", hasItem(itemId.intValue()));

        Long interestOneId = expressInterest(itemId, recipientOneToken, donorId, "I can pick it up today.", recipientOneId);
        Long interestTwoId = expressInterest(itemId, recipientTwoToken, donorId, "Happy to take it.", recipientTwoId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
                .when()
                .post("/api/items/{itemId}/select/{interestId}", itemId, interestOneId)
                .then()
                .statusCode(200)
                .body("id", equalTo(itemId.intValue()))
                .body("state", equalTo("PENDING"));

        given()
                .when()
                .get("/api/items/{itemId}/interests", itemId)
                .then()
                .statusCode(200)
                .body("find { it.id == " + interestOneId + " }.status", equalTo("SELECTED"))
                .body("find { it.id == " + interestTwoId + " }.status", equalTo("PENDING"));

        Long siteId = createTransferSite(donorToken);
        Long timeSlotId = createTimeSlot(donorToken, siteId);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
                .body(Map.of(
                        "timeSlotId", timeSlotId,
                        "notes", "Meet by the front desk."
                ))
                .when()
                .post("/api/items/{itemId}/schedule", itemId)
                .then()
                .statusCode(200)
                .body("status", equalTo("SCHEDULED"))
                .body("donor.id", equalTo(donorId.intValue()))
                .body("recipient.id", equalTo(recipientOneId.intValue()))
                .body("timeSlot.id", equalTo(timeSlotId.intValue()));

        given()
                .when()
                .get("/api/appointments/item/{itemId}", itemId)
                .then()
                .statusCode(200)
                .body("recipient.id", equalTo(recipientOneId.intValue()))
                .body("timeSlot.transferSite.id", equalTo(siteId.intValue()));

        given()
                .header("Authorization", bearer(donorToken))
                .when()
                .post("/api/items/{itemId}/complete", itemId)
                .then()
                .statusCode(200)
                .body("id", equalTo(itemId.intValue()))
                .body("state", equalTo("DONE"));

        given()
                .when()
                .get("/api/items/{itemId}", itemId)
                .then()
                .statusCode(200)
                .body("state", equalTo("DONE"));
    }

    @Test
    void itemCreationAndDonorTransitionsUseAuthenticatedUser() {
        String suffix = uniqueSuffix();
        String donorEmail = "auth-donor-" + suffix + "@example.com";
        String recipientEmail = "auth-recipient-" + suffix + "@example.com";

        Long donorId = createUser(donorEmail, "Alex", "Donor");
        Long recipientId = createUser(recipientEmail, "Jordan", "Recipient");

        String donorToken = login(donorEmail);
        String recipientToken = login(recipientEmail);

        Long itemId = given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
                .body(Map.of(
                        "donorId", recipientId,
                        "title", "Bookshelf",
                        "description", "Small bookshelf",
                        "category", "Furniture",
                        "condition", "Used",
                        "size", "Medium",
                        "quantity", 1
                ))
                .when()
                .post("/api/items")
                .then()
                .statusCode(201)
                .body("donor.id", equalTo(donorId.intValue()))
                .extract()
                .jsonPath()
                .getLong("id");

        Long interestId = expressInterest(itemId, recipientToken, donorId, "I would like this.", recipientId);

        given()
                .header("Authorization", bearer(recipientToken))
                .when()
                .post("/api/items/{itemId}/select/{interestId}", itemId, interestId)
                .then()
                .statusCode(403);
    }

    private Long createUser(String email, String firstName, String lastName) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "password",
                        "firstName", firstName,
                        "lastName", lastName,
                        "phoneNumber", "555-111-2222",
                        "address", "100 Main St"
                ))
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private String login(String email) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "password"
                ))
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .asString();
    }

    private Long expressInterest(Long itemId, String token, Long fakeUserId, String message, Long expectedUserId) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(token))
                .body(Map.of(
                        "userId", fakeUserId,
                        "message", message
                ))
                .when()
                .post("/api/items/{itemId}/interests", itemId)
                .then()
                .statusCode(201)
                .body("message", equalTo(message))
                .body("status", equalTo("PENDING"))
                .body("user.id", equalTo(expectedUserId.intValue()))
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Long createTransferSite(String donorToken) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
                .body(Map.of(
                        "name", "Community Center",
                        "address", "100 Main St",
                        "city", "Springfield",
                        "state", "IL",
                        "zipCode", "62701",
                        "contactName", "Jane Smith",
                        "phoneNumber", "555-111-2222",
                        "email", "center@example.com",
                        "description", "Safe public meeting location"
                ))
                .when()
                .post("/api/transfer-sites")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private Long createTimeSlot(String donorToken, Long siteId) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime endTime = startTime.plusHours(1);

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", bearer(donorToken))
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
                .jsonPath()
                .getLong("id");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueSuffix() {
        return Long.toString(System.nanoTime());
    }
}
