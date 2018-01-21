package org.ryanairbot.handlers;

import org.ryanairbot.BotConfig;
import org.ryanairbot.bot.RyanairService;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class RyanairHandlers extends TelegramLongPollingBot {
    private static final String LOGTAG = "RAEHANDLERS";

    private final RyanairService ryanairService = new RyanairService();

    @Override
    public String getBotToken() {
        return BotConfig.RYANAIR_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            //check if the message has text. it could also  contain for example a location ( message.hasLocation() )
            if (message.hasText()) {

                //create a object that contains the information to send back the message
                SendMessage sendMessageRequest = new SendMessage();
                sendMessageRequest.setChatId(message.getChatId().toString());
                sendMessageRequest.enableMarkdown(true);
                sendMessageRequest.setText(callService(message.getText()));

                try {
                    sendMessage(sendMessageRequest);
                } catch (TelegramApiException e) {
                    //do some error handling
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.RYANAIR_USER;
    }


    public String callService(String message) {

        return ryanairService.processMessage(message);
    }

}
