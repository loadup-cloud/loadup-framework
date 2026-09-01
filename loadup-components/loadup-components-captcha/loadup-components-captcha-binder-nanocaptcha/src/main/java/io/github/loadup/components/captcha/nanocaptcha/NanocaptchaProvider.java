package io.github.loadup.components.captcha.nanocaptcha;

/*-
 * #%L
 * LoadUp Captcha Binder Nanocaptcha
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

import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.CaptchaResponse;
import io.github.loadup.components.captcha.CaptchaType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.logicsquad.nanocaptcha.content.ChineseContentProducer;
import net.logicsquad.nanocaptcha.content.ContentProducer;
import net.logicsquad.nanocaptcha.content.LatinContentProducer;
import net.logicsquad.nanocaptcha.content.NumbersContentProducer;
import net.logicsquad.nanocaptcha.image.ImageCaptcha;
import net.logicsquad.nanocaptcha.image.backgrounds.GradiatedBackgroundProducer;
import net.logicsquad.nanocaptcha.image.noise.CurvedLineNoiseProducer;

/**
 * nanocaptcha backed {@link CaptchaProvider} for classic character image captchas.
 *
 * <p>Answers are kept in a bounded in-memory store keyed by captcha id; each verification is
 * single-use (the entry is removed on the first attempt, whether it succeeds or not).
 */
public class NanocaptchaProvider implements CaptchaProvider {

    private static final int MAX_PENDING = 5000;

    private final NanocaptchaProperties properties;
    private final Map<String, AnswerEntry> pending = new ConcurrentHashMap<>();

    public NanocaptchaProvider(NanocaptchaProperties properties) {
        this.properties = properties;
    }

    @Override
    public CaptchaResponse generate(String type) {
        if (type != null && !CaptchaType.WORD.equals(type)) {
            throw new IllegalArgumentException("nanocaptcha only supports type " + CaptchaType.WORD);
        }
        ImageCaptcha captcha = new ImageCaptcha.Builder(properties.getWidth(), properties.getHeight())
                .addContent(contentProducer())
                .addBackground(new GradiatedBackgroundProducer())
                .addNoise(new CurvedLineNoiseProducer())
                .addBorder()
                .build();

        String captchaId = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + properties.getExpirationSeconds() * 1000;
        pending.put(captchaId, new AnswerEntry(captcha.getContent(), expiresAt));
        purgeIfNeeded();

        return new CaptchaResponse(
                captchaId,
                CaptchaType.WORD,
                toDataUri(captcha.getImage()),
                null,
                "image/png",
                null,
                properties.getWidth(),
                properties.getHeight(),
                null,
                null,
                null);
    }

    @Override
    public boolean verify(String captchaId, Object userInput) {
        if (!(userInput instanceof String input)) {
            return false;
        }
        AnswerEntry entry = pending.remove(captchaId);
        if (entry == null || entry.expiresAt() < System.currentTimeMillis()) {
            return false;
        }
        return entry.answer().equalsIgnoreCase(input.trim());
    }

    @Override
    public String getBinderType() {
        return "nanocaptcha";
    }

    private ContentProducer contentProducer() {
        return switch (properties.getContent()) {
            case "latin" -> new LatinContentProducer(properties.getLength());
            case "chinese" -> new ChineseContentProducer(properties.getLength());
            default -> new NumbersContentProducer(properties.getLength());
        };
    }

    private static String toDataUri(BufferedImage image) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode captcha image", e);
        }
    }

    private void purgeIfNeeded() {
        if (pending.size() < MAX_PENDING) {
            return;
        }
        long now = System.currentTimeMillis();
        pending.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
    }

    private record AnswerEntry(String answer, long expiresAt) {}
}
