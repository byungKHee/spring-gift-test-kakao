package gift.cucumber;

import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;
import io.cucumber.java.ko.그러면;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ProductStepDefinitions {

    @Autowired
    private SharedContext sharedContext;

    @먼저("{string} 카테고리에 {string} 상품이 가격 {int}으로 등록되어 있다")
    public void 상품이_등록되어_있다(String categoryName, String productName, int price) {
        long categoryId = sharedContext.getId("카테고리_" + categoryName);
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "price": %d,
                            "imageUrl": "https://example.com/%s.jpg",
                            "categoryId": %d
                        }
                        """.formatted(productName, price, productName, categoryId))
                .when()
                .post("/api/products");
        long id = response.then().extract().jsonPath().getLong("id");
        sharedContext.putId("상품_" + productName, id);
    }

    @먼저("{string} 카테고리에 {string} 상품이 등록되어 있다")
    public void 상품이_등록되어_있다(String categoryName, String productName) {
        상품이_등록되어_있다(categoryName, productName, 10000);
    }

    @만일("{string} 카테고리에 {string} 상품을 가격 {int} 이미지 {string}로 등록한다")
    public void 상품을_등록한다(String categoryName, String productName, int price, String imageUrl) {
        long categoryId = sharedContext.getId("카테고리_" + categoryName);
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "price": %d,
                            "imageUrl": "%s",
                            "categoryId": %d
                        }
                        """.formatted(productName, price, imageUrl, categoryId))
                .when()
                .post("/api/products");
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 카테고리에 상품을 등록한다")
    public void 존재하지_않는_카테고리에_상품을_등록한다() {
        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "아이폰 16",
                            "price": 1500000,
                            "imageUrl": "https://example.com/iphone.jpg",
                            "categoryId": 999999
                        }
                        """)
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
}
