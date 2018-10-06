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
    private static final String END_REFRESHING_LOG_MSG = "Refreshing of online users list is completed. Removed " +
            "users: {}, marked users: {}";
    private static final String START_REFRESHING_LOG_MSG = "Refreshing of online users list is started. Total " +
            "tracked users: {}, online users: {}";
    private static final Logger logger = LoggerFactory.getLogger(SimpleUserAvailabilityService.class);
    private static final ConcurrentHashMap<Long, Boolean> onlineUsersId = new ConcurrentHashMap<>();

    private final PersonService personService;

    @Autowired
    public SimpleUserAvailabilityService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean isOnline(long id) {
        return onlineUsersId.containsKey(id);
    }

    @Override
    public void setOnline(long id) {
        if (onlineUsersId.containsKey(id) || personService.findById(id).isPresent()) {
            onlineUsersId.put(id, true);
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
        Set<Entry<Long, Boolean>> entries = onlineUsersId.entrySet();
        logger.info(START_REFRESHING_LOG_MSG, entries.size(), entries.stream().filter(Entry::getValue).count());
        Iterator<Entry<Long, Boolean>> iterator = entries.iterator();
        int removed = 0;
        int marked = 0;
        while (iterator.hasNext()) {
            Entry<Long, Boolean> entry = iterator.next();
            if (!entry.getValue()) {
                iterator.remove();
                removed++;
            } else {
                entry.setValue(false);
                marked++;
            }
        }
        logger.info(END_REFRESHING_LOG_MSG, removed, marked);
    }
}
