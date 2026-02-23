package gift;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@AcceptanceTest
class GiftAcceptanceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @Sql("/sql/선물_보내기_정상.sql")
    void 정상_선물_보내기() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1)
                .body("""
                        {
                            "optionId": 1,
                            "quantity": 3,
                            "receiverId": 2,
                            "message": "생일 축하해!"
                        }
                        """)
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(200);

        var quantity = jdbcTemplate.queryForObject("SELECT quantity FROM option WHERE id = 1", Integer.class);
        assertThat(quantity).isEqualTo(7);
    }

    @Test
    @Sql("/sql/선물_보내기_재고부족.sql")
    void 재고_부족_시_실패() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1)
                .body("""
                        {
                            "optionId": 1,
                            "quantity": 10,
                            "receiverId": 2,
                            "message": "선물!"
                        }
                        """)
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(500);

        var quantity = jdbcTemplate.queryForObject("SELECT quantity FROM option WHERE id = 1", Integer.class);
        assertThat(quantity).isEqualTo(5);
    }

    @Test
    @Sql("/sql/선물_보내기_재고경계값.sql")
    void 재고_경계값_두_번째_선물이_실패() {
        var requestBody = """
                {
                    "optionId": 1,
                    "quantity": 1,
                    "receiverId": 2,
                    "message": "선물!"
                }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1)
                .body(requestBody)
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(200);

        var afterFirst = jdbcTemplate.queryForObject("SELECT quantity FROM option WHERE id = 1", Integer.class);
        assertThat(afterFirst).isEqualTo(0);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1)
                .body(requestBody)
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(500);

        var afterSecond = jdbcTemplate.queryForObject("SELECT quantity FROM option WHERE id = 1", Integer.class);
        assertThat(afterSecond).isEqualTo(0);
    }

    @Test
    @Sql("/sql/선물_보내기_옵션없음.sql")
    void 존재하지_않는_옵션으로_선물_시도() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1)
                .body("""
                        {
                            "optionId": 999999,
                            "quantity": 1,
                            "receiverId": 2,
                            "message": "선물!"
                        }
                        """)
                .when()
                .post("/api/gifts")
                .then()
                .statusCode(500);
    }
}
