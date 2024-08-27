package uz.pdp.tgbot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.OrderProduct;

import java.util.UUID;

@Repository
public interface OrderProductRepo extends JpaRepository<OrderProduct, UUID> {
}
