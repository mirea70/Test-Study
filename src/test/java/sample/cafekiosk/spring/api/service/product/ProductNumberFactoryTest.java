package sample.cafekiosk.spring.api.service.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

class ProductNumberFactoryTest extends IntegrationTestSupport {

    @Autowired
    private ProductNumberFactory productNumberFactory;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }

    @DisplayName("등록된 상품이 없을 경우, 생성되는 상품번호는 001이다.")
    @Test
    void createNextProductNumber() {
        // given
        String expected = "001";

        // when
        String result = productNumberFactory.createNextProductNumber();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("등록된 상품이 여러개일 경우, 마지막 상품번호에 1을 더한 상품번호가 반환되어야 한다.")
    @Test
    void createNextProductNumber2() {
        // given
        Product product1 = createProduct("001");
        Product product2 = createProduct("002");
        productRepository.saveAll(List.of(product1, product2));

        String lastProductNumber = productRepository.findLatestProductNumber();

        // when
        String result = productNumberFactory.createNextProductNumber();

        // then
        assertThat(Integer.parseInt(result)).isEqualTo(Integer.parseInt(lastProductNumber) + 1);
    }

    private Product createProduct(String productNumber) {
        return Product.builder()
                .productNumber(productNumber)
                .type(HANDMADE)
                .sellingStatus(SELLING)
                .name("테스트상품")
                .price(3500)
                .build();
    }
}