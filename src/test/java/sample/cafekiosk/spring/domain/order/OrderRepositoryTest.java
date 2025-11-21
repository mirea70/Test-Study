package sample.cafekiosk.spring.domain.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static sample.cafekiosk.spring.domain.order.OrderStatus.PAYMENT_COMPLETED;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.product.ProductType.HANDMADE;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;


    @DisplayName("일정 기간 내에 존재하는 특정 주문 상태의 주문 정보들을 조회한다.")
    @Test
    void findOrdersBy() {
        // given
        Order order1 = createOrder(LocalDateTime.of(2025, 10, 5, 15, 55));
        Order order2 = createOrder(LocalDateTime.of(2025, 10, 7, 15, 55));

        LocalDateTime startDateTime = LocalDateTime.of(2025, 10, 4, 15, 55);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 10, 8, 15, 55);
        OrderStatus requestOrderStatus = PAYMENT_COMPLETED;
        order2.updateOrderStatus(requestOrderStatus);

        orderRepository.saveAll(List.of(order1, order2));

        // when
        List<Order> result = orderRepository.findOrdersBy(startDateTime, endDateTime, requestOrderStatus);

        // then
        assertThat(result).hasSize(1)
                .extracting("orderStatus")
                .containsExactlyInAnyOrder(requestOrderStatus);

    }

    @DisplayName("요청 시작 일자 전 등록된 주문 정보는 조회되면 안된다.")
    @Test
    void findOrdersByBeforeStartDateTime() {
        // given
        LocalDateTime targetDateTime = LocalDateTime.of(2025, 10, 7, 15, 55);

        Order order1 = createOrder(targetDateTime.minusSeconds(1));
        Order order2 = createOrder(targetDateTime);

        LocalDateTime startDateTime = targetDateTime;
        LocalDateTime endDateTime = LocalDateTime.of(2025, 10, 9, 15, 55);
        OrderStatus requestOrderStatus = PAYMENT_COMPLETED;
        order2.updateOrderStatus(requestOrderStatus);

        orderRepository.saveAll(List.of(order1, order2));

        // when
        List<Order> result = orderRepository.findOrdersBy(startDateTime, endDateTime, requestOrderStatus);

        // then : 시작날짜와 동일한 시간에 등록된 order2 조회 O, 1초 이전 등록된 order1은 조회 X
        assertThat(result).hasSize(1)
                .extracting("orderStatus")
                .containsExactlyInAnyOrder(requestOrderStatus);

    }

    @DisplayName("요청 끝 일자 이후 등록된 주문 정보는 조회되면 안된다.")
    @Test
    void findOrdersByAfterEndDateTime() {
        // given
        LocalDateTime targetDateTime = LocalDateTime.of(2025, 10, 7, 15, 55);

        Order order1 = createOrder(LocalDateTime.of(2025, 10, 5, 15, 55));
        Order order2 = createOrder(targetDateTime);

        LocalDateTime startDateTime = LocalDateTime.of(2025, 10, 3, 15, 55);
        LocalDateTime endDateTime = targetDateTime;
        OrderStatus requestOrderStatus = PAYMENT_COMPLETED;
        order2.updateOrderStatus(requestOrderStatus);

        orderRepository.saveAll(List.of(order1, order2));

        // when
        List<Order> result = orderRepository.findOrdersBy(startDateTime, endDateTime, requestOrderStatus);

        // then
        assertThat(result).hasSize(0);

    }

    private Order createOrder(LocalDateTime registeredDateTime) {
        Product product = createProduct("001", "아메리카노", 3000);

        return Order.create(
            List.of(product), registeredDateTime
        );
    }

    private Product createProduct(String productNumber, String name, int price) {
        Product product = Product.builder()
                .productNumber(productNumber)
                .type(HANDMADE)
                .sellingStatus(SELLING)
                .name(name)
                .price(price)
                .build();
        productRepository.save(product);

        return product;
    }
}