package com.github.loadup.framework.api.manager;

import com.github.loadup.framework.api.binder.Binder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;

public class BinderManager<T extends Binder> {
  private final Map<String, T> binders = new ConcurrentHashMap<>();

  public BinderManager(ObjectProvider<T> provider, Class<T> binderClass) {
    List<T> list = provider.stream().collect(Collectors.toList());
    if (list.isEmpty()) {
      throw new IllegalStateException(
          "No "
              + binderClass.getSimpleName()
              + " beans found. "
              + "Please register at least one implementation as a Spring bean.");
    }
    for (T b : list) {
      binders.put(b.type(), b);
    }
  }

  public Optional<T> findBinder(String type) {
    return Optional.ofNullable(binders.get(type));
  }

  public T getBinder(String type) {
    return findBinder(type)
        .orElseThrow(() -> new IllegalStateException("no Binder for type: " + type));
  }

  public Set<String> availableTypes() {
    return Collections.unmodifiableSet(binders.keySet());
  }
}
