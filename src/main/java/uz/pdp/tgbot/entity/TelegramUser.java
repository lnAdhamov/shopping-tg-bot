package uz.pdp.tgbot.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.tgbot.entity.enums.State;

import java.util.UUID;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private Long chatId;
    private int counter;
    private State state;
    private Integer msgId;
    @ManyToOne
    private Product selectedProduct;


}
