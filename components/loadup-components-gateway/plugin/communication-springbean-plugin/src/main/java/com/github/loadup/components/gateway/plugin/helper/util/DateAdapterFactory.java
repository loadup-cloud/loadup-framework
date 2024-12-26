package com.github.loadup.components.gateway.plugin.helper.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.util.Date;

/**
 *
 */
public class DateAdapterFactory<T> implements TypeAdapterFactory {

    /**
     * create adapter
     */
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (rawType != Date.class) {
            return null;
        }
        return (TypeAdapter<T>) new DateAdapter();
    }
}