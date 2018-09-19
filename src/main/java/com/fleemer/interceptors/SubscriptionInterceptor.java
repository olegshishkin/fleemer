package com.fleemer.interceptors;

import com.fleemer.model.Person;
import com.fleemer.service.PersonService;
import com.fleemer.service.UserAvailabilityService;
import java.security.Principal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionInterceptor.class);

    private final UserAvailabilityService availabilityService;
    private final PersonService personService;

    @Autowired
    public SubscriptionInterceptor(UserAvailabilityService availabilityService, PersonService personService) {
        this.availabilityService = availabilityService;
        this.personService = personService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        Principal principal = accessor.getUser();
        if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();
            if (!validate(principal, destination)) {
                String msg = "Subscribing failed. No permissions for the specified destination. Destination: " +
                        destination + ", user: " + (principal != null ? principal.getName() : null);
                LOGGER.warn(msg);
                throw new RuntimeException(msg);
            }
        }

        if (accessor.isHeartbeat() && principal != null) {
            availabilityService.setOnline(principal.getName());
        }
        return message;
    }

    private boolean validate(Principal principal, String destination) {
        if (principal == null) {
            return false;
        }
        Optional<Person> optional = personService.findByEmail(principal.getName());
        long id = Long.parseLong(destination.substring(destination.lastIndexOf('/') + 1));
        return optional.isPresent() && optional.get().getId().equals(id);
    }
}