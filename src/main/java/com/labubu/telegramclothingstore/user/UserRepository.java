package com.labubu.telegramclothingstore.user;

import com.labubu.telegramclothingstore.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);



}
