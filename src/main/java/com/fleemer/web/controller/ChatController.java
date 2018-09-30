package com.fleemer.web.controller;

import com.fleemer.model.ChatMessage;
import com.fleemer.model.Person;
import com.fleemer.service.PersonService;
import com.fleemer.service.UserAvailabilityService;
import java.util.*;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {
    private static final String PERSON_SESSION_ATTR = "person";
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final UserAvailabilityService availabilityService;
    private final JmsTemplate jmsQueueTemplate;
    private final JmsTemplate jmsTopicTemplate;
    private final PersonService personService;

    @Autowired
    public ChatController(UserAvailabilityService availabilityService, JmsTemplate jmsQueueTemplate,
                          JmsTemplate jmsTopicTemplate, PersonService personService) {
        this.availabilityService = availabilityService;
        this.jmsQueueTemplate = jmsQueueTemplate;
        this.jmsTopicTemplate = jmsTopicTemplate;
        this.personService = personService;
    }

    @RequestMapping("/chat")
    public String chat(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        model.addAttribute("currentUserNickname", person.getNickname());
        model.addAttribute("currentUserId", person.getId());
        model.addAttribute("locale", LocaleContextHolder.getLocale().toLanguageTag());
        return "chat";
    }

    @RequestMapping("chat/search")
    @ResponseBody
    public Map<String, Long> search(@RequestParam String nickname, HttpSession session) {
        Pageable pageable = PageRequest.of(0, 10, Direction.ASC, "nickname");
        List<Person> people = personService.findAllByNicknamePart(nickname, pageable).getContent();
        Map<String, Long> result = getNicknamesMap(people);
        Person currentUser = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        result.remove(currentUser.getNickname());
        return result;
    }

    @RequestMapping("chat/getNickname")
    @ResponseBody
    public String getNickname(@RequestParam long id) {
        return getPerson(id).getNickname();
    }

    @PostMapping("/chat/statesUpdate")
    @ResponseBody
    public Map<Long, Boolean> getUserStates(@RequestBody Long[] ids) {
        Map<Long, Boolean> result = new HashMap<>();
        List.of(ids).forEach(id -> {
            boolean isOnline = availabilityService.isOnline(id);
            result.put(id, isOnline);
        });
        return result;
    }

    @MessageMapping("/send/one")
    public void send(Message<ChatMessage> message) {
        ChatMessage chatMessage = message.getPayload();
        jmsQueueTemplate.convertAndSend("chat/" + chatMessage.getReceiverId(), chatMessage);
    }

    @MessageMapping("/send/all")
    public void sendAll(Message<ChatMessage> message) {
        ChatMessage chatMessage = message.getPayload();
        Person sender = getPerson(chatMessage.getSenderId());
        chatMessage.setSenderNickname(sender.getNickname());
        jmsTopicTemplate.convertAndSend("chat/" + chatMessage.getReceiverId(), chatMessage);
    }

    @MessageMapping("/notify/one")
    public void notify(Message<ChatMessage> message) {
        ChatMessage chatMessage = message.getPayload();
        jmsQueueTemplate.convertAndSend("chat/notification/" + chatMessage.getReceiverId(), chatMessage);
    }

    @MessageMapping("/notify/all")
    public void notifyAll(Message<ChatMessage> message) {
        ChatMessage chatMessage = message.getPayload();
        Person person = getPerson(chatMessage.getSenderId());
        jmsTopicTemplate.convertAndSend("chat/notification/" + chatMessage.getReceiverId(), person.getNickname());
    }

    private Person getPerson(long id) {
        Optional<Person> optional = personService.findById(id);
        if (!optional.isPresent()) {
            String msg = "No person with id: " + id;
            logger.warn(msg);
            throw new RuntimeException(msg);
        }
        return optional.get();
    }

    private Map<String, Long> getNicknamesMap(List<Person> people) {
        Map<String, Long> nicknames = new HashMap<>();
        people.forEach(person -> nicknames.put(person.getNickname(), person.getId()));
        return nicknames;
    }
}
