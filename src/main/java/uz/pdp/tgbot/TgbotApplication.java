package uz.pdp.tgbot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TgbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TgbotApplication.class, args);
    }


    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot("7488878543:AAHwMhZlZ3QSiAsEOnDZoyppjS7x14dWScs") {
        };
    }




}
