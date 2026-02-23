package gift.cucumber;

import gift.DatabaseCleaner;
import io.cucumber.java.After;
import io.cucumber.java.ko.그러면;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class CommonStepDefinitions {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private SharedContext sharedContext;

    @After
    public void cleanDatabase() {
        databaseCleaner.clear();
    }

    @그러면("응답 상태코드는 {int}이다")
    public void 응답_상태코드_검증(int statusCode) {
        sharedContext.getLastResponse().then().statusCode(statusCode);
    }

    @그러면("응답에 id가 포함되어 있다")
    public void 응답에_id가_포함() {
        sharedContext.getLastResponse().then().body("id", notNullValue());
    }

    @그러면("응답 목록의 크기는 {int}이다")
    public void 응답_목록의_크기_검증(int size) {
        sharedContext.getLastResponse().then().body("$", hasSize(size));
    }
}
