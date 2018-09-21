package com.fleemer.web.filter;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class AccessLoggingFilter extends Filter<IAccessEvent> {
    private static final String WEBSOCKET_HANDSHAKE_URL = "/chat/handshake";

    @Override
    public FilterReply decide(IAccessEvent event) {
        if (event.getRequestURI().startsWith(WEBSOCKET_HANDSHAKE_URL)) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }
}
