package gift.cucumber;

import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.equalTo;

public class GiftStepDefinitions {

    @Autowired
    private SharedContext sharedContext;

    @먼저("{string} 회원이 등록되어 있다")
    public void 회원이_등록되어_있다(String name) {
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s"
                        }
                        """.formatted(name))
                .when()
                .post("/api/test/members");
        long id = response.then().extract().jsonPath().getLong("id");
        sharedContext.putId("회원_" + name, id);
    }

    @먼저("{string} 상품에 재고 {int}개인 {string} 옵션이 등록되어 있다")
    public void 옵션이_등록되어_있다(String productName, int quantity, String optionName) {
        long productId = sharedContext.getId("상품_" + productName);
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "quantity": %d,
                            "productId": %d
                        }
                        """.formatted(optionName, quantity, productId))
                .when()
                .post("/api/test/options");
        long id = response.then().extract().jsonPath().getLong("id");
        sharedContext.putId("옵션_" + optionName, id);
    }

    @만일("{string}이 {string} 옵션을 {int}개 선물한다")
    public void 선물한다(String senderName, String optionName, int quantity) {
        long senderId = sharedContext.getId("회원_" + senderName);
        long optionId = sharedContext.getId("옵션_" + optionName);
        long receiverId = sharedContext.getId("회원_받는사람");
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", senderId)
                .body("""
                        {
                            "optionId": %d,
                            "quantity": %d,
                            "receiverId": %d,
                            "message": "선물!"
                        }
                        """.formatted(optionId, quantity, receiverId))
                .when()
                .post("/api/gifts");
        sharedContext.setLastResponse(response);
    }

    @만일("{string}이 존재하지 않는 옵션을 선물한다")
    public void 존재하지_않는_옵션을_선물한다(String senderName) {
        long senderId = sharedContext.getId("회원_" + senderName);
        long receiverId = sharedContext.getId("회원_받는사람");
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", senderId)
                .body("""
                        {
                            "optionId": 999999,
                            "quantity": 1,
                            "receiverId": %d,
                            "message": "선물!"
                        }
                        """.formatted(receiverId))
                .when()
                .post("/api/gifts");
        sharedContext.setLastResponse(response);
    }

    @그러면("{string} 옵션의 재고는 {int}개이다")
    public void 옵션의_재고_검증(String optionName, int expectedQuantity) {
        long optionId = sharedContext.getId("옵션_" + optionName);
        RestAssured.given()
                .when()
                .get("/api/test/options/" + optionId)
                .then()
                .body("quantity", equalTo(expectedQuantity));
    }
}
