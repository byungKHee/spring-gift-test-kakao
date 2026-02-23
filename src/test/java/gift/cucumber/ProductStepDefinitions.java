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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class ProductStepDefinitions {

    @Autowired
    private SharedContext sharedContext;

    @Autowired
    private DataSource dataSource;

    @먼저("상품 2개가 등록되어 있다")
    public void 상품_2개가_등록되어_있다() {
        executeSql("sql/상품_두개_등록.sql");
    }

    @만일("카테고리 {long}에 {string} 상품을 가격 {int} 이미지 {string}로 등록한다")
    public void 상품을_등록한다(long categoryId, String name, int price, String imageUrl) {
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "price": %d,
                            "imageUrl": "%s",
                            "categoryId": %d
                        }
                        """.formatted(name, price, imageUrl, categoryId))
                .when()
                .post("/api/products");
        sharedContext.setLastResponse(response);
    }

    @만일("상품 목록을 조회한다")
    public void 상품_목록을_조회한다() {
        var response = RestAssured.given()
                .when()
                .get("/api/products");
        sharedContext.setLastResponse(response);
    }

    @그러면("상품 목록을 조회하면 {int}개가 반환된다")
    public void 상품_목록을_조회하면_N개가_반환된다(int count) {
        RestAssured.given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("$", hasSize(count));
    }

    @그러면("응답에 상품 이름 {string}이 포함되어 있다")
    public void 응답에_상품_이름이_포함(String name) {
        sharedContext.getLastResponse().then().body("name", equalTo(name));
    }

    @그러면("응답에 상품 가격 {int}이 포함되어 있다")
    public void 응답에_상품_가격이_포함(int price) {
        sharedContext.getLastResponse().then().body("price", equalTo(price));
    }

    @그러면("응답에 카테고리 이름 {string}이 포함되어 있다")
    public void 응답에_카테고리_이름이_포함(String categoryName) {
        sharedContext.getLastResponse().then().body("category.name", equalTo(categoryName));
    }

    @그러면("상품 목록에 {string} 가격 {int} 카테고리 {string}이 포함되어 있다")
    public void 상품_목록에_포함(String name, int price, String categoryName) {
        List<Map<String, Object>> products = sharedContext.getLastResponse()
                .then().extract().jsonPath().getList("$");
        assertThat(products).anySatisfy(p -> {
            assertThat(p.get("name")).isEqualTo(name);
            assertThat(p.get("price")).isEqualTo(price);
            assertThat(((Map<?, ?>) p.get("category")).get("name")).isEqualTo(categoryName);
        });
    }

    private void executeSql(String resourcePath) {
        try (var conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(resourcePath));
        } catch (Exception e) {
            throw new RuntimeException("SQL 실행 실패: " + resourcePath, e);
        }
    }
}
