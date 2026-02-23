package gift;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@AcceptanceTest
class CategoryRetrieveAcceptanceTest {

    @Test
    void 데이터가_없을_때_빈_목록_반환() {
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @Sql("/sql/카테고리_두개_등록.sql")
    void 등록한_카테고리가_목록에_포함() {
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].id", equalTo(1))
                .body("[0].name", equalTo("식품"))
                .body("[1].id", equalTo(2))
                .body("[1].name", equalTo("전자기기"));
    }
}
