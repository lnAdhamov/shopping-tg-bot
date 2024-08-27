package uz.pdp.tgbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uz.pdp.tgbot.entity.Order;
import uz.pdp.tgbot.entity.enums.OrderStatus;
import uz.pdp.tgbot.repo.OrderRepo;
import uz.pdp.tgbot.service.OrderService;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepo orderRepo;
    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }


    @PutMapping("/{orderId}/status/{newStatus}")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Integer orderId,
            @PathVariable OrderStatus newStatus) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        messagingTemplate.convertAndSend("/topic/orders", updatedOrder);
        return ResponseEntity.ok(updatedOrder);
    }
}
