package io.github.loadup.testify.data.engine.function;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom functions accessible via ${fn.XXX()}
 */
public class CommonFunction implements TestifyFunction {
    @Override
    public String getPrefix() {
        return "fn";
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public int random(int min, int max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
