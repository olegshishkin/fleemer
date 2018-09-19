package com.fleemer.web.other;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class AccessLoggingFilter extends Filter<ILoggingEvent> {
    private static final String WEBSOCKET_HANDSHAKE_URL = "/chat/handshake";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMessage().startsWith(WEBSOCKET_HANDSHAKE_URL)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.ACCEPT;
        }
    }
}
