package dev.practice.order.domain.order;

import dev.practice.order.domain.order.payment.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderStore orderStore;
    private final OrderReader orderReader;
    private final OrderItemSeriesFactory orderItemSeriesFactory;
    private final PaymentProcessor paymentProcessor;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    @Transactional
    public String registerOrder(OrderCommand.RegisterOrder requestOrder) {
        Order order = orderStore.store(requestOrder.toEntity());
        orderItemSeriesFactory.store(order, requestOrder);
        return order.getOrderToken();
    }

    @Override
    @Transactional
    public void paymentOrder(OrderCommand.PaymentRequest paymentRequest) {
        var orderToken = paymentRequest.getOrderToken();
        var order = orderReader.getOrder(orderToken);
        paymentProcessor.pay(order, paymentRequest);
        order.orderComplete();
        // 외부 서비스 호출 시 오류가 발생하지 않으면 그 후에 runtimeexception이 발생해도 외부에서 호출된 로직은 복구되지 않는다.
        // 따라서 orderComplete이후에 pay를 두어 pay에서 exception이 발생할 경우 롤백되도록 하는 것이 좋다.
        // ex) order.orderComplete();
        // paymentProcessor.pay(order, paymentRequest);
        // 이점 : 보상 트랜잭션 로직을 짜지 않아도 됨
        // 단점 : 로직의 순서를 pay -> orderComplete에서 orderComplete -> pay로 바꾸었으므로 순서를 이렇게 해도 되는건지 주의해야 함
    }

    @Override
    @Transactional(readOnly = true)
    public OrderInfo.Main retrieveOrder(String orderToken) {
        var order = orderReader.getOrder(orderToken);
        var orderItemList = order.getOrderItemList();
        return orderInfoMapper.of(order, orderItemList);
    }

    @Override
    @Transactional
    public void updateReceiverInfo(String orderToken, OrderCommand.UpdateReceiverInfoRequest request) {
        var order = orderReader.getOrder(orderToken);
        order.updateDeliveryFragment(
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverZipcode(),
                request.getReceiverAddress1(),
                request.getReceiverAddress2(),
                request.getEtcMessage()
        );
        order.deliveryPrepare();
    }
}
