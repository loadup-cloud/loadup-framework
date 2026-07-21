package io.github.loadup.components.gotone.channel.email.config;

import io.github.loadup.components.gotone.GotoneProvider;
import io.github.loadup.components.gotone.channel.email.SmtpEmailProvider;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.gotone.binder.email.smtp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(
            @org.springframework.beans.factory.annotation.Value("${spring.mail.host:smtp.gmail.com}") String host,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.port:587}") int port,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}") String username,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.password:}") String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }

    @Bean
    @ConditionalOnMissingBean(name = "smtpEmailProvider")
    public GotoneProvider smtpEmailProvider(JavaMailSender mailSender) {
        log.info(">>> [GOTONE] SmtpEmailProvider initialized");
        return new SmtpEmailProvider(mailSender);
    }
}
