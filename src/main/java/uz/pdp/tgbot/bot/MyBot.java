package uz.pdp.tgbot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.pdp.tgbot.entity.Category;
import uz.pdp.tgbot.entity.Product;
import uz.pdp.tgbot.repo.*;

import java.io.IOException;
import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class MyBot implements CommandLineRunner {

    private final TelegramBot telegramBot;
    private final UpdateHandlerService updateHandlerService;
    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;


    @Override
    public void run(String... args) throws IOException {
        generateData();
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                updateHandlerService.handleUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }


    private void generateData() throws IOException {
        Category category1 = new Category();
        category1.setName("Ichimliklar");
        Category category2 = new Category();
        category2.setName("Yeguliklar");
        Category category3 = new Category();
        category3.setName("Maishiy texnika");
        Category category4 = new Category();
        category4.setName("CS2 skins");
        categoryRepo.save(category1);
        categoryRepo.save(category2);
        categoryRepo.save(category3);
        categoryRepo.save(category4);

        Product product1 = new Product();
        product1.setCategory(category1);
        product1.setName("Ko'k choy");
        product1.setPrice(2);
        product1.setPhoto(loadImage("kokchoy.jpg"));
        Product product2 = new Product();
        product2.setCategory(category1);
        product2.setName("Qora choy");
        product2.setPrice(2);
        product2.setPhoto(loadImage("qorachoy.jpg"));
        Product product3 = new Product();
        product3.setCategory(category2);
        product3.setName("Osh");
        product3.setPrice(3);
        product3.setPhoto(loadImage("osh.jpg"));
        Product product4 = new Product();
        product4.setCategory(category2);
        product4.setName("Somsa");
        product4.setPrice(2);
        product4.setPhoto(loadImage("somsa.jpg"));
        Product product5 = new Product();
        product5.setCategory(category3);
        product5.setName("Konditsioner");
        product5.setPrice(400);
        product5.setPhoto(loadImage("konditsioner.png"));
        Product product6 = new Product();
        product6.setCategory(category3);
        product6.setName("Chang yutgich");
        product6.setPrice(300);
        product6.setPhoto(loadImage("pilisos.jpg"));
        Product product7 = new Product();
        product7.setCategory(category4);
        product7.setName("AK-47 Head Shot");
        product7.setPrice(80);
        product7.setPhoto(loadImage("ak.png"));
        Product product8 = new Product();
        product8.setCategory(category4);
        product8.setName("AWP-Dragon Lore");
        product8.setPrice(220_000);
        product8.setPhoto(loadImage("awp.png"));
        productRepo.save(product1);
        productRepo.save(product2);
        productRepo.save(product3);
        productRepo.save(product4);
        productRepo.save(product5);
        productRepo.save(product6);
        productRepo.save(product7);
        productRepo.save(product8);

    }

    public byte[] loadImage(String imageName) throws IOException {
        String imagePath = "images/" + imageName; // Path relative to the resources directory
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(imagePath)) {
            if (inputStream == null) {
                throw new IOException("Image not found: " + imagePath);
            }
            return inputStream.readAllBytes();
        }
    }


}
