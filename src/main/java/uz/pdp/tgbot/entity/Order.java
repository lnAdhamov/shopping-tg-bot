package uz.pdp.tgbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uz.pdp.tgbot.entity.enums.OrderStatus;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne
    private TelegramUser telegramUser;
}
