package dev.practice.order.application.order.gift;

import dev.practice.order.domain.order.OrderCommand;
import dev.practice.order.domain.order.gift.GiftOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftFacade {
    private final GiftOrderService giftOrderService;

    // orderFacade의 paymentOrder와 다른 점은 GiftFacade의 paymentOrder는
    // SqsSender를 사용한다는 점이다.
    public void paymentOrder(OrderCommand.PaymentRequest request) {
        giftOrderService.paymentOrder(request);
    }
}
