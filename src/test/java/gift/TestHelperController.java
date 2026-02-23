package gift;

import gift.model.Member;
import gift.model.MemberRepository;
import gift.model.Option;
import gift.model.OptionRepository;
import gift.model.Product;
import gift.model.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestHelperController {

    private final MemberRepository memberRepository;
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public TestHelperController(MemberRepository memberRepository,
                                OptionRepository optionRepository,
                                ProductRepository productRepository) {
        this.memberRepository = memberRepository;
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/members")
    public Member createMember(@RequestBody Map<String, String> request) {
        var member = new Member(request.get("name"), request.get("name") + "@test.com");
        return memberRepository.save(member);
    }

    @PostMapping("/options")
    public Option createOption(@RequestBody Map<String, Object> request) {
        var productId = ((Number) request.get("productId")).longValue();
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        var option = new Option(
                (String) request.get("name"),
                ((Number) request.get("quantity")).intValue(),
                product
        );
        return optionRepository.save(option);
    }

    @GetMapping("/options/{id}")
    public Option getOption(@PathVariable Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Option not found: " + id));
    }
}
