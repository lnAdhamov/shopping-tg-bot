package uz.pdp.tgbot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.tgbot.entity.Basket;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BasketRepo extends JpaRepository<Basket, UUID> {
    Optional<Basket> findByTelegramUserId(UUID userId);
}
