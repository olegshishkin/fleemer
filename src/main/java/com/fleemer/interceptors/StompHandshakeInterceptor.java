package com.fleemer.interceptors;

import com.fleemer.service.UserAvailabilityService;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class StompHandshakeInterceptor implements HandshakeInterceptor {
    private final UserAvailabilityService availabilityService;

    @Autowired
    public StompHandshakeInterceptor(UserAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception == null) {
            Principal principal = request.getPrincipal();
            availabilityService.setOnline(principal.getName());
        }
    }
}
