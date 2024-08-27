package uz.pdp.tgbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.BasketProduct;
import uz.pdp.tgbot.entity.Product;
import uz.pdp.tgbot.entity.TelegramUser;
import uz.pdp.tgbot.entity.enums.State;
import uz.pdp.tgbot.repo.BasketProductRepo;
import uz.pdp.tgbot.repo.BasketRepo;
import uz.pdp.tgbot.repo.UserRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasketService {
    private final BasketRepo basketRepo;
    private final UserRepo userRepo;
    private final BasketProductRepo bpr;

    public void addToBasket(TelegramUser user, Product selectedProduct) {
        Basket basket = getBasket(user.getId());
        BasketProduct product = new BasketProduct();
        product.setBasket(basket);
        product.setProduct(selectedProduct);
        product.setAmount(user.getCounter());
        bpr.save(product);
        user.setCounter(0);
        user.setSelectedProduct(null);
        user.setState(State.RE_ENTER_PRODUCT);
        userRepo.save(user);
    }

    public void removeProduct(TelegramUser user, Product selectedProduct) {
        user.setSelectedProduct(null);
        user.setState(State.RE_ENTER_PRODUCT);
        user.setCounter(0);
        userRepo.save(user);
    }


    public Basket getBasket(UUID userId) {
        Optional<Basket> basket = basketRepo.findByTelegramUserId(userId);
        if (basket.isPresent()) {
            return basket.get();
        } else {
            TelegramUser telegramUser = userRepo.findById(userId).orElseThrow();
            Basket basket1 = new Basket();
            basket1.setTelegramUser(telegramUser);
            return basketRepo.save(basket1);
        }
    }

    public String generateBasketSummary(Basket basket) {
        List<BasketProduct> basketProducts = bpr.findByBasket(basket);
        StringBuilder summary = new StringBuilder("Your Basket:\n\n");

        double totalBasketPrice = 0;

        for (BasketProduct basketProduct : basketProducts) {
            Product product = basketProduct.getProduct();
            int amount = basketProduct.getAmount();
            double totalPrice = product.getPrice() * amount;
            totalBasketPrice += totalPrice;

            summary.append(String.format("Product: %s\nAmount: %d\nTotal: $%.2f\n\n",
                    product.getName(), amount, totalPrice));
        }

        summary.append(String.format("Total Basket Price: $%.2f", totalBasketPrice));
        return summary.toString();
    }

    public void clearBasket(Basket basket) {
        basketRepo.delete(basket);
    }
}
