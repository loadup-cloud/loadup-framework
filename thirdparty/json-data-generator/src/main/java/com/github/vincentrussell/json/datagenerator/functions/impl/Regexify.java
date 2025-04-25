package com.github.vincentrussell.json.datagenerator.functions.impl;

import com.github.vincentrussell.json.datagenerator.functions.Function;
import com.github.vincentrussell.json.datagenerator.functions.FunctionInvocation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.datafaker.service.FakeValuesService;
import net.datafaker.service.FakerContext;
import net.datafaker.service.RandomService;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Generate random strings based on regex
 */
@Function(name = "regexify")
public class Regexify {

    @SuppressWarnings("checkstyle:magicnumber")
    private static final LoadingCache<String, FakeValuesService>
            FAKE_VALUES_SERVICE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                       public FakeValuesService load(final String locale) {
                           return new FakeValuesService();
                       }
                   }
            );

    @FunctionInvocation
    public final String regexify(final String regex) {
        return regexify("en-US", regex);
    }

    @FunctionInvocation
    public final String regexify(final String locale, final String regex) {
        FakerContext context = new FakerContext(new Locale(locale), new RandomService());
        return getFakeValuesService(locale).regexify(regex, context);
    }

    private static FakeValuesService getFakeValuesService(final String locale) {
        try {
            return FAKE_VALUES_SERVICE_CACHE.get(locale);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


}
