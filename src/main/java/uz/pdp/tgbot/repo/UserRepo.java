package uz.pdp.tgbot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.tgbot.entity.TelegramUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<TelegramUser, UUID> {
    Optional<TelegramUser> findByChatId(Long chatId);
}
