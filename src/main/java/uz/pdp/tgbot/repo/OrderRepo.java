package uz.pdp.tgbot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.tgbot.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Integer> {
}
