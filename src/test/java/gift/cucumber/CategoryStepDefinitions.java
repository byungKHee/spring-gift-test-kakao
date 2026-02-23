package gift.cucumber;

import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class CategoryStepDefinitions {

    @Autowired
    private SharedContext sharedContext;

    @Autowired
    private DataSource dataSource;

    @먼저("{string} 카테고리가 등록되어 있다")
    public void 카테고리가_등록되어_있다(String name) {
        executeSql("sql/상품_등록_카테고리_준비.sql");
    }

    @먼저("카테고리 2개가 등록되어 있다")
    public void 카테고리_2개가_등록되어_있다() {
        executeSql("sql/카테고리_두개_등록.sql");
    }

    @만일("{string} 카테고리를 등록한다")
    public void 카테고리를_등록한다(String name) {
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s"
                        }
                        """.formatted(name))
                .when()
                .post("/api/categories");
        sharedContext.setLastResponse(response);
    }

    @만일("카테고리 목록을 조회한다")
    public void 카테고리_목록을_조회한다() {
        var response = RestAssured.given()
                .when()
                .get("/api/categories");
        sharedContext.setLastResponse(response);
    }

    @그러면("카테고리 목록을 조회하면 {int}개가 반환된다")
    public void 카테고리_목록을_조회하면_N개가_반환된다(int count) {
        RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200)
                .body("$", hasSize(count));
    }

    @그러면("카테고리 목록에 {string}이 포함되어 있다")
    public void 카테고리_목록에_포함(String name) {
        List<Map<String, Object>> categories = RestAssured.given()
                .when()
                .get("/api/categories")
                .then()
                .extract().jsonPath().getList("$");
        assertThat(categories).anySatisfy(c ->
                assertThat(c.get("name")).isEqualTo(name));
    }

    private void executeSql(String resourcePath) {
        try (var conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(resourcePath));
        } catch (Exception e) {
            throw new RuntimeException("SQL 실행 실패: " + resourcePath, e);
        }
    }
}
