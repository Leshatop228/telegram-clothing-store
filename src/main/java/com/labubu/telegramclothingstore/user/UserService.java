package com.labubu.telegramclothingstore.user;

import com.labubu.telegramclothingstore.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    public UserEntity getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new
                        RuntimeException("Пользователь с Telegram ID "
                                + telegramId + " не найден"));
    }
}
