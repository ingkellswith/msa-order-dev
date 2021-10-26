package dev.practice.order.domain.order.gift;

import dev.practice.order.common.exception.IllegalStatusException;
import dev.practice.order.domain.order.Order;
import dev.practice.order.domain.order.OrderCommand;
import dev.practice.order.domain.order.OrderReader;
import dev.practice.order.domain.order.payment.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftOrderServiceImpl implements GiftOrderService {
    private final OrderReader orderReader;
    private final PaymentProcessor paymentProcessor;
    private final GiftMessageChannelSender giftMessageChannelSender;

    @Override
    @Transactional
    public void paymentOrder(OrderCommand.PaymentRequest paymentRequest) {
        log.info("GiftOrderService.paymentOrder request = {}", paymentRequest);
        var order = orderReader.getOrder(paymentRequest.getOrderToken());

        // 아래 로직을 추가하면, paymentProcessor.pay 실행 이후의 보상 트랜잭션 발생을 막을 수 있다
        if (order.getStatus() != Order.Status.INIT) throw new IllegalStatusException();

        paymentProcessor.pay(order, paymentRequest);
        // 28번쨰 라인은 order.orderComplete()에 포함되어있는 로직이지만, 한 번 더 선언했으므로 중복로직이다.
        // 하지만 외부 결제 모듈 정상 호출후에 exception이 발생할 시 @Transactional로 롤백이 불가능하므로 보상 트랜잭션이 수행되어야 한다.
        // 보상 트랜잭션 최소화를 하는 것이 가치가 더 크다고 생각했기 때문에 중복로직을 포함한 것이다.
        order.orderComplete();

        giftMessageChannelSender.paymentComplete(new GiftPaymentCompleteMessage(order.getOrderToken()));
    }
}
