package com.monntterro.trelloflowbot.bot.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {
    private String token;
    private String url;
    private String username;
    private String path;
}
