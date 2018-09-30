package com.fleemer.service.implementation;

import com.fleemer.model.Person;
import com.fleemer.service.PersonService;
import com.fleemer.service.UserAvailabilityService;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SimpleUserAvailabilityService implements UserAvailabilityService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleUserAvailabilityService.class);
    private static final ConcurrentHashMap<Long, Boolean> online_users_id = new ConcurrentHashMap<>();

    private final PersonService personService;

    @Autowired
    public SimpleUserAvailabilityService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean isOnline(long id) {
        return online_users_id.containsKey(id);
    }

    @Override
    public void setOnline(long id) {
        if (online_users_id.containsKey(id) || personService.findById(id).isPresent()) {
            online_users_id.put(id, true);
        }
    }

    @Override
    public void setOnline(String username) {
        Optional<Person> optional = personService.findByEmail(username);
        if (!optional.isPresent()) {
            logger.warn("No person has such email: {}", username);
            return;
        }
        this.setOnline(optional.get().getId());
    }

    @Scheduled(fixedDelay = 12000L)
    private void filterOnlineUsersList() {
        logger.debug("Refreshing of online users list...");
        Iterator<Entry<Long, Boolean>> iterator = online_users_id.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Long, Boolean> entry = iterator.next();
            if (!entry.getValue()) {
                iterator.remove();
            } else {
                entry.setValue(false);
            }
        }
    }
}
