package uz.pdp.tgbot.bot;

import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.tgbot.entity.Basket;
import uz.pdp.tgbot.entity.Category;
import uz.pdp.tgbot.entity.Product;
import uz.pdp.tgbot.entity.TelegramUser;
import uz.pdp.tgbot.repo.CategoryRepo;
import uz.pdp.tgbot.repo.ProductRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotUtils {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;

    public Keyboard generateContactButton() {
        KeyboardButton keyboardButton = new KeyboardButton("Share contact");
        keyboardButton.requestContact(true);
        return new ReplyKeyboardMarkup(
                keyboardButton
        );
    }

    public Keyboard generateCategoryButton() {
        List<Category> categories = categoryRepo.findAll();

        // Create buttons for each category
        KeyboardButton[] categoryButtons = categories.stream()
                .map(category -> new KeyboardButton(category.getName()))
                .toArray(KeyboardButton[]::new);

        // Create the "Basket" button
        KeyboardButton basketButton = new KeyboardButton("Basket");

        // Combine category buttons and the basket button
        KeyboardButton[] allButtons = new KeyboardButton[categoryButtons.length + 1];
        System.arraycopy(categoryButtons, 0, allButtons, 0, categoryButtons.length);
        allButtons[categoryButtons.length] = basketButton;

        // Create and return the keyboard markup
        return new ReplyKeyboardMarkup(allButtons)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }


    public Keyboard generateProductButtons(String categoryName) {
        List<Product> products = productRepo.findAllByCategoryName(categoryName);

        InlineKeyboardButton[] buttons = products.stream()
                .map(product -> new InlineKeyboardButton(product.getName()).callbackData("product_" + product.getId()))
                .toArray(InlineKeyboardButton[]::new);

        return new InlineKeyboardMarkup(buttons);
    }

    public InlineKeyboardMarkup generateCounterButtons(TelegramUser user) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("-").callbackData("minus"),
                new InlineKeyboardButton(user.getCounter() + "").callbackData("amount"),
                new InlineKeyboardButton("+").callbackData("plus"),
                new InlineKeyboardButton("Add To \uD83D\uDED2").callbackData("addtobasket"),
                new InlineKeyboardButton("Cancel").callbackData("cancel")
        );
    }

    public InlineKeyboardMarkup generateBasketButtons(Basket basket) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton("Confirm Order").callbackData("confirm_order"),
                new InlineKeyboardButton("Clear Basket").callbackData("clear_basket")
        );
    }
}
