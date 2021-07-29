package com.example.turaiagent.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.auth.mechanisms", "NTLM");
//        props.put("mail.smtp.auth.ntlm.domain", "DOMAIN");
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setJavaMailProperties(props);
        sender.setUsername("subscriptonmanagmentmailsender@gmail.com");
        sender.setPassword("32620155");
        return sender;
    }

}
