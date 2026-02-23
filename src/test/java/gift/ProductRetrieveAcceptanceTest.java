package gift;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@AcceptanceTest
class ProductRetrieveAcceptanceTest {

    @Test
    void 데이터가_없을_때_빈_목록_반환() {
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @Sql("/sql/상품_두개_등록.sql")
    void 등록한_상품이_목록에_포함_카테고리_중첩_응답() {
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].id", equalTo(1))
                .body("[0].name", equalTo("케이크"))
                .body("[0].price", equalTo(30000))
                .body("[0].imageUrl", equalTo("https://example.com/cake.jpg"))
                .body("[0].category.id", equalTo(1))
                .body("[0].category.name", equalTo("식품"))
                .body("[1].id", equalTo(2))
                .body("[1].name", equalTo("초콜릿"))
                .body("[1].price", equalTo(15000))
                .body("[1].category.id", equalTo(1));
    }
}
