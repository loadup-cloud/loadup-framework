/*
 * #%L
 * Loadup Gotone Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.gotone.test.channel.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.email.SmtpEmailProvider;
import io.github.loadup.components.gotone.channel.email.config.EmailChannelAutoConfiguration;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class SmtpEmailProviderTest {
    private final JavaMailSender sender = mock(JavaMailSender.class);
    private final SmtpEmailProvider provider = new SmtpEmailProvider(sender, "default@example.test");
    private final ApplicationContextRunner contexts = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EmailChannelAutoConfiguration.class));

    private static String findText(Part part, String mimeType) throws Exception {
        if (part.isMimeType(mimeType)) {
            return (String) part.getContent();
        }
        if (part.getContent() instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart body = multipart.getBodyPart(i);
                String text = findText(body, mimeType);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    @Test
    void sendsDefaultHtmlMessageWithActualMimeHeaders() throws Exception {
        when(sender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        var result = provider.send(new ChannelSendRequest(List.of("buyer@example.test"), "<b>receipt</b>", null, null));
        var message = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(message.capture());
        MimeMessage mime = message.getValue();
        mime.saveChanges();
        assertThat(mime.getFrom()[0].toString()).isEqualTo("default@example.test");
        assertThat(mime.getAllRecipients()[0].toString()).isEqualTo("buyer@example.test");
        assertThat(mime.getSubject()).isEqualTo("Notification");
        assertThat(findText(mime, "text/html")).isEqualTo("<b>receipt</b>");
        assertThat(result.isSuccess("buyer@example.test")).isTrue();
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        assertThat(result.content()).isEqualTo("<b>receipt</b>");
        assertThat(provider.getChannelType()).isEqualTo("EMAIL");
        assertThat(provider.getProviderName()).isEqualTo("smtp");
        assertThat(provider.isAvailable()).isTrue();
        assertThat(new SmtpEmailProvider(null, "default@example.test").isAvailable())
                .isFalse();
    }

    @Test
    void honorsPerChannelFromSubjectAndPlainText() throws Exception {
        when(sender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        var result = provider.send(new ChannelSendRequest(
                List.of("buyer@example.test"),
                "receipt",
                Map.of("from", "sales@example.test", "subject", "Payment received", "html", false),
                null));
        var messages = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender).send(messages.capture());
        var mime = messages.getValue();
        mime.saveChanges();
        assertThat(mime.getFrom()[0].toString()).isEqualTo("sales@example.test");
        assertThat(mime.getSubject()).isEqualTo("Payment received");
        assertThat(findText(mime, "text/plain")).isEqualTo("receipt");
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void emptyRecipientsDoNotCreateOrSendMail() {
        var result = provider.send(new ChannelSendRequest(null, "receipt", null, null));
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isZero();
        assertThat(result.receiverStatus()).isEmpty();
        assertThat(result.isSuccess()).isFalse();
        verifyNoInteractions(sender);
    }

    @Test
    void recordsSenderFailurePerRecipientAndContinues() {
        when(sender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        doThrow(new MailSendException("SMTP unavailable")).when(sender).send(any(MimeMessage.class));
        var result = provider.send(
                new ChannelSendRequest(List.of("abc", "a@example.test", "buyer@example.test"), "receipt", null, null));
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(3);
        assertThat(result.receiverStatus()).containsEntry("buyer@example.test", false);
        assertThat(result.getErrorMessage("buyer@example.test")).isEqualTo("SMTP unavailable");
        assertThat(result.receiverErrors()).hasSize(3);
    }

    @Test
    void invalidFromIsAnAttemptFailureWithoutSending() {
        when(sender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        var result = provider.send(new ChannelSendRequest(
                List.of("buyer@example.test"), "receipt", Map.of("from", "bad@@example.test"), null));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.getErrorMessage("buyer@example.test")).isNotBlank();
    }

    @Test
    void autoConfigurationUsesStandardMailPropertiesAndTlsDefaults() {
        contexts.withPropertyValues(
                        "spring.mail.host=smtp.example.test",
                        "spring.mail.port=2525",
                        "spring.mail.username=sender@example.test",
                        "spring.mail.password=synthetic-password")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    var mail = context.getBean(JavaMailSenderImpl.class);
                    assertThat(mail.getHost()).isEqualTo("smtp.example.test");
                    assertThat(mail.getPort()).isEqualTo(2525);
                    assertThat(mail.getUsername()).isEqualTo("sender@example.test");
                    assertThat(mail.getPassword()).isEqualTo("synthetic-password");
                    assertThat(mail.getJavaMailProperties())
                            .containsEntry("mail.smtp.auth", "true")
                            .containsEntry("mail.smtp.starttls.enable", "true")
                            .containsEntry("mail.transport.protocol", "smtp");
                    assertThat(context.getBean("smtpEmailProvider", NotificationChannelProvider.class)
                                    .isAvailable())
                            .isTrue();
                });
    }

    @Test
    void autoConfigurationBacksOffForCustomBeansAndDisabledBinder() {
        contexts.withBean(JavaMailSender.class, () -> sender)
                .run(context ->
                        assertThat(context.getBean(JavaMailSender.class)).isSameAs(sender));
        var custom = mock(NotificationChannelProvider.class);
        contexts.withBean("smtpEmailProvider", NotificationChannelProvider.class, () -> custom)
                .run(context -> assertThat(context.getBean("smtpEmailProvider")).isSameAs(custom));
        contexts.withPropertyValues("loadup.gotone.binder.email.smtp.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(JavaMailSender.class)
                        .doesNotHaveBean("smtpEmailProvider"));
    }
}
