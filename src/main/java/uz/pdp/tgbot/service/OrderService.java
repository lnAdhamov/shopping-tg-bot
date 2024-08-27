package uz.pdp.tgbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.BasketProduct;
import uz.pdp.tgbot.entity.Order;
import uz.pdp.tgbot.entity.OrderProduct;
import uz.pdp.tgbot.entity.enums.OrderStatus;
import uz.pdp.tgbot.repo.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepo orderRepo;
    private final BasketRepo basketRepo;
    private final OrderProductRepo opr;
    private final BasketProductRepo bpr;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void confirmOrder(Basket basket) {
        Order order = new Order();
        order.setTelegramUser(basket.getTelegramUser());
        order.setDateTime(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        orderRepo.save(order);

        List<BasketProduct> basketProducts = bpr.findByBasket(basket);
        for (BasketProduct basketProduct : basketProducts) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setOrder(order);
            orderProduct.setProduct(basketProduct.getProduct());
            orderProduct.setAmount(basketProduct.getAmount());
            opr.save(orderProduct);
        }

        bpr.deleteAllByBasket(basket);
        basketRepo.delete(basket);

        messagingTemplate.convertAndSend("/topic/orders", order);
    }


    public Order updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(newStatus);
        Order updatedOrder = orderRepo.save(order);
            messagingTemplate.convertAndSend("/topic/orders", updatedOrder);
        return updatedOrder;
    }
}
