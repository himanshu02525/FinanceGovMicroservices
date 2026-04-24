package com.finance.util;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class MessageUtil {
	private MessageSource messageSource;

	public MessageUtil(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public String getMessage(String key, Object... args) {
		return messageSource.getMessage(key, args, Locale.ENGLISH);
	}
}
