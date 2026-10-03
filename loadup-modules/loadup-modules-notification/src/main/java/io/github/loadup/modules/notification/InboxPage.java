package io.github.loadup.modules.notification;

import java.util.List;

public record InboxPage(List<InboxMessage> records, long total, int page, int size) {}
