package uz.pdp.tgbot.bot;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.pdp.tgbot.entity.TelegramUser;
import uz.pdp.tgbot.entity.enums.State;
import uz.pdp.tgbot.repo.UserRepo;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateHandlerService.class);

    private final BotService botService;
    private final UserRepo userRepo;

    @Async
    public void handleUpdate(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            Long chatId = message.chat().id();
            TelegramUser user = getUser(chatId);
            if (message.text() != null) {
                String text = message.text();
                logger.info("Received message: {}", text);
                if ("/start".equals(text)) {
                    botService.welcomeAndAskContact(user, message);
                } else if (user.getState() == State.ENTER_CATEGORY || user.getState() == State.RE_ENTER_PRODUCT) {
                    botService.acceptCategoryAskProductAndBasketCheck(user, message);
                }
            } else if (message.contact() != null) {
                if (user.getState().equals(State.ENTER_CONTACT)) {
                    botService.acceptContactAskCategory(user, message);
                }
            }
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            Long chatId = callbackQuery.from().id();
            TelegramUser user = getUser(chatId);
            logger.info("Received callback: {}", callbackQuery.data());
            if (user != null) {
                if (user.getState() == State.ENTER_PRODUCT) {
                    botService.acceptProductAskAmount(user, callbackQuery);
                } else if (user.getState() == State.ENTER_AMOUNT) {
                    botService.acceptAmountAndAddToBasketOrCancel(user, callbackQuery);
                    botService.addToBasketOrCancel(user, callbackQuery);
                } else if (user.getState() == State.CHECK_BASKET) {
                    botService.clearBasketOrConfirmOrder(user, callbackQuery);
                }
            }
        }
    }

    private TelegramUser getUser(Long chatId) {
        Optional<TelegramUser> user = userRepo.findByChatId(chatId);
        if (user.isPresent()) {
            return user.get();
        } else {
            TelegramUser user1 = new TelegramUser();
            user1.setChatId(chatId);
            user1.setState(State.START);
            return userRepo.save(user1);
        }
    }


}
