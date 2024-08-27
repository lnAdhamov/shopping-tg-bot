package uz.pdp.tgbot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.BasketProduct;
import uz.pdp.tgbot.entity.Product;
import uz.pdp.tgbot.entity.TelegramUser;
import uz.pdp.tgbot.entity.enums.State;
import uz.pdp.tgbot.repo.BasketProductRepo;
import uz.pdp.tgbot.repo.BasketRepo;
import uz.pdp.tgbot.repo.ProductRepo;
import uz.pdp.tgbot.repo.UserRepo;
import uz.pdp.tgbot.service.BasketService;
import uz.pdp.tgbot.service.OrderService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BotService {

    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final TelegramBot telegramBot;
    private final BotUtils botUtils;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final BasketService basketService;
    private final OrderService orderService;
    private final BasketRepo basketRepo;
    private final BasketProductRepo basketProductRepo;


    public void welcomeAndAskContact(TelegramUser user, Message message) {
        String name = message.chat().firstName();
        user.setFirstName(name);
        if (message.chat().lastName() != null) {
            user.setLastName(message.chat().lastName());
        }
        if (user.getPhone() == null) {
            user.setState(State.ENTER_CONTACT);
            userRepo.save(user);
            SendMessage sendMessage = new SendMessage(user.getChatId(), """
                    Assalamu aleykum %s,
                    Kontaktingiz kiriting:
                    """.formatted(name));
            sendMessage.replyMarkup(botUtils.generateContactButton());
            telegramBot.execute(sendMessage);
        } else {
            user.setState(State.ENTER_CATEGORY);
            userRepo.save(user);
            SendMessage sendMessage = new SendMessage(user.getChatId(), """
                    Assalamu aleykum %s
                    """.formatted(name));
            telegramBot.execute(sendMessage);
        }
    }


    public void acceptContactAskCategory(TelegramUser user, Message message) {
        user.setPhone(message.text());
        Contact contact = message.contact();
        user.setPhone(contact.phoneNumber());
        user.setState(State.ENTER_CATEGORY);
        userRepo.save(user);
        SendMessage sendMessage = new SendMessage(
                user.getChatId(),
                "Choose category:"
        );
        sendMessage.replyMarkup(botUtils.generateCategoryButton());
        telegramBot.execute(sendMessage);
    }

    public void acceptCategoryAskProductAndBasketCheck(TelegramUser user, Message message) {
        if (message.text().equalsIgnoreCase("basket")) {
            user.setState(State.CHECK_BASKET);
            userRepo.save(user);
            Basket basket = basketService.getBasket(user.getId());
            List<BasketProduct> basketProducts = basketProductRepo.findByBasket(basket);
            if (basketProducts.isEmpty()) {
                SendMessage sendMessage = new SendMessage(
                        user.getChatId(),
                        "Savatingiz hozircha bo'sh"
                );
                user.setState(State.RE_ENTER_PRODUCT);
                userRepo.save(user);
                telegramBot.execute(sendMessage);
            } else {
                String basketSummary = basketService.generateBasketSummary(basket);
                SendMessage sendMessage = new SendMessage(
                        user.getChatId(),
                        basketSummary
                );
                sendMessage.replyMarkup(botUtils.generateBasketButtons(basket));
                telegramBot.execute(sendMessage);
            }
        } else {
            logger.info("User selected category: {}", message.text());
            user.setState(State.ENTER_PRODUCT);
            userRepo.save(user);
            SendMessage sendMessage = new SendMessage(
                    user.getChatId(),
                    "Choose product: "
            );
            sendMessage.replyMarkup(botUtils.generateProductButtons(message.text()));
            telegramBot.execute(sendMessage);
        }
    }


    public void acceptProductAskAmount(TelegramUser user, CallbackQuery callbackQuery) {
        logger.info("User selected product: {}", callbackQuery.data());
        String productIdStr = callbackQuery.data().replace("product_", "");
        UUID productId = UUID.fromString(productIdStr);
        Optional<Product> productOpt = productRepo.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            user.setSelectedProduct(product);
            user.setCounter(1);
            userRepo.save(user);
            SendPhoto sendPhoto = new SendPhoto(user.getChatId(), product.getPhoto());
            telegramBot.execute(sendPhoto);
            double totalPrice = user.getCounter() * product.getPrice();
            String productDetails = String.format("Product: %s\nPrice: $%.2f\nAmount: %d\nTotal: $%.2f\n",
                    product.getName(), product.getPrice(), user.getCounter(), totalPrice);
            SendMessage sendMessage = new SendMessage(user.getChatId(), productDetails);
            sendMessage.replyMarkup(botUtils.generateCounterButtons(user));
            SendResponse res = telegramBot.execute(sendMessage);

            user.setMsgId(res.message().messageId());
            user.setState(State.ENTER_AMOUNT);
            userRepo.save(user);
        } else {
            SendMessage sendMessage = new SendMessage(user.getChatId(), "Product not found.");
            telegramBot.execute(sendMessage);
        }
    }

    public void acceptAmountAndAddToBasketOrCancel(TelegramUser user, CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        if (data.equalsIgnoreCase("plus")) {
            user.setCounter(user.getCounter() + 1);
        } else if (data.equalsIgnoreCase("minus")) {
            user.setCounter(Math.max(user.getCounter() - 1, 1));
        }
        userRepo.save(user);

        Product product = user.getSelectedProduct();
        double totalPrice = user.getCounter() * product.getPrice();
        String updatedDetails = String.format("Product: %s\nPrice: $%.2f\nAmount: %d\nTotal: $%.2f\n",
                product.getName(), product.getPrice(), user.getCounter(), totalPrice);

        EditMessageText editMessageText = new EditMessageText(user.getChatId(), user.getMsgId(), updatedDetails);
        editMessageText.replyMarkup(botUtils.generateCounterButtons(user));
        telegramBot.execute(editMessageText);
    }


    public void addToBasketOrCancel(TelegramUser user, CallbackQuery callbackQuery) {
        if (callbackQuery.data().equalsIgnoreCase("addtobasket")) {
            SendMessage sendMessage = new SendMessage(
                    user.getChatId(),
                    "Added to basket, anything else 👇"
            );
            basketService.addToBasket(user, user.getSelectedProduct());
            telegramBot.execute(sendMessage);
        } else if (callbackQuery.data().equalsIgnoreCase("cancel")) {
            SendMessage sendMessage = new SendMessage(
                    user.getChatId(),
                    "Choose whatever you want 👇"
            );
            basketService.removeProduct(user, user.getSelectedProduct());
            telegramBot.execute(sendMessage);
        }
    }

    public void clearBasketOrConfirmOrder(TelegramUser user, CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        Basket basket = basketRepo.findByTelegramUserId(user.getId()).orElseThrow();
        if (data.equalsIgnoreCase("clear_basket")) {
            SendMessage sendMessage = new SendMessage(
                    user.getChatId(),
                    "Basket is cleared, Come whenever you wanna order something 👇"
            );
            basketService.clearBasket(basket);
            user.setState(State.RE_ENTER_PRODUCT);
            userRepo.save(user);
            telegramBot.execute(sendMessage);
        } else if (data.equalsIgnoreCase("confirm_order")) {
            SendMessage sendMessage = new SendMessage(
                    user.getChatId(),
                    "Order is confirmed, Thank you for using our services"
            );
            user.setState(State.RE_ENTER_PRODUCT);
            userRepo.save(user);
            orderService.confirmOrder(basket);
            telegramBot.execute(sendMessage);
        }
    }
}
