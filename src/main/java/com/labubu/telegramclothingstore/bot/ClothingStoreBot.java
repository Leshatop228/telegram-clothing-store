package com.labubu.telegramclothingstore.bot;

import com.labubu.telegramclothingstore.catalog.CategoryEntity;
import com.labubu.telegramclothingstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClothingStoreBot implements LongPollingSingleThreadUpdateConsumer {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final CategoryService categoryService;
    private TelegramClient telegramClient;

    public ClothingStoreBot(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostConstruct
    public void init() throws Exception {
        telegramClient = new OkHttpTelegramClient(botToken);

        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(botToken, this);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendCategories(chatId);
            }
        }
    }

    private void sendCategories(long chatId) {
        List<CategoryEntity> categories = categoryService.getAllCategories();

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (CategoryEntity category : categories) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(category.getName())
                    .callbackData("category_" + category.getId())
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выбери категорию:")
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}