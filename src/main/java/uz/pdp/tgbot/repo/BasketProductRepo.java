package uz.pdp.tgbot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.BasketProduct;

import java.util.List;
import java.util.UUID;

@Repository
public interface BasketProductRepo extends JpaRepository<BasketProduct, UUID> {
    List<BasketProduct> findByBasket(Basket basket);

    void deleteAllByBasket(Basket basket);
}
