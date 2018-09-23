package com.fleemer.web.controller;

import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.fleemer.service.ConfirmationService;
import com.fleemer.service.MailService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final String EMAIL_TEMPLATE = "mail";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final String PASSWD_CONFIRM_FAILED_MSG_KEY = "user.error.password-not-equals";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String SUBJECT_TEXT_MSG_KEY = "mail.subject";
    private static final String USER_CREATE_VIEW = "user_create";
    private static final String USER_EXISTS_ERROR_MSG_KEY = "user.error.user-exists";
    private static final String USER_JOINING_MSG = "New user has joined to Fleemer: ";
    private static final String USER_UPDATE_VIEW = "user_update";

    private final ConfirmationService confirmationService;
    private final MailService mailService;
    private final MessageSource messageSource;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PersonService personService;
    private final TemplateEngine templateEngine;

    @Value("${com.fleemer.owner.email}")
    private String ownerEmail;
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public UserController(TemplateEngine templateEngine, PersonService personService,
                          BCryptPasswordEncoder passwordEncoder, ConfirmationService confirmationService,
                          MailService mailService, MessageSource messageSource) {
        this.templateEngine = templateEngine;
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationService = confirmationService;
        this.mailService = mailService;
        this.messageSource = messageSource;
    }

    @GetMapping("/create")
    public ModelAndView create() {
        return new ModelAndView(USER_CREATE_VIEW, "person", new Person());
    }

    @LogAfterReturning
    @PostMapping("/create")
    public String create(HttpServletRequest request, @Valid @ModelAttribute Person person,
                         @RequestParam("confirm") String confirmPassword, BindingResult bindingResult)
            throws ServiceException, MessagingException, MalformedURLException {
        if (bindingResult.hasErrors()) {
            return USER_CREATE_VIEW;
        }
        if (!confirmPassword.equals(person.getHash())) {
            String msg = getMessage(PASSWD_CONFIRM_FAILED_MSG_KEY);
            bindingResult.rejectValue("hash", "hash.confirmNotEquals", msg);
            return USER_CREATE_VIEW;
        }
        String email = person.getEmail();
        if (personService.findByEmail(email).isPresent()) {
            String msg = getMessage(USER_EXISTS_ERROR_MSG_KEY);
            bindingResult.rejectValue("email", "email.alreadyExists", msg);
            return USER_CREATE_VIEW;
        }
        String nickname = person.getNickname();
        if (personService.findByNickname(nickname).isPresent()) {
            String msg = getMessage(USER_EXISTS_ERROR_MSG_KEY);
            bindingResult.rejectValue("nickname", "nickname.alreadyExists", msg);
            return USER_CREATE_VIEW;
        }
        String hash = passwordEncoder.encode(person.getHash());
        person.setHash(hash);
        Confirmation confirmation = new Confirmation();
        confirmation.setPerson(person);
        String token = UUID.randomUUID().toString();
        confirmation.setToken(token);
        personService.save(person);
        confirmationService.save(confirmation);
        try {
            sendConfirmationMail(request, email, token);
        } catch (MalformedURLException e) {
            personService.delete(person);
            throw e;
        }
        LOGGER.info("New user has created: {}", email);
        return "redirect:/user/create/status?confirm=notification";
    }

    @GetMapping("/create/status")
    public String status(@RequestParam String confirm, Model model) {
        model.addAttribute("confirm", confirm);
        return "user_create_status";
    }

    @GetMapping("/create/confirm")
    public String confirm(@RequestParam String email, @RequestParam String token) throws ServiceException,
            MessagingException {
        boolean success = verify(email, token);
        if (success) {
            mailService.send(senderEmail, ownerEmail, USER_JOINING_MSG + email, "");
            LOGGER.info("User has confirmed: {}", email);
        }
        return "redirect:/user/create/status?confirm=" + (success ? "success" : "failed");
    }

    @GetMapping("/update")
    public ModelAndView update(HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        return new ModelAndView(USER_UPDATE_VIEW, "person", person);
    }

    @LogAfterReturning
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute Person person, BindingResult bindingResult, HttpSession session)
            throws ServiceException {
        if (bindingResult.hasErrors()) {
            return USER_UPDATE_VIEW;
        }
        Person currentUser = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (!person.getId().equals(currentUser.getId())) {
            return "redirect:/";
        }
        String email = person.getEmail();
        if (!email.equals(currentUser.getEmail())) {
            if (personService.findByEmail(email).isPresent()) {
                String msg = getMessage(USER_EXISTS_ERROR_MSG_KEY);
                bindingResult.rejectValue("email", "email.alreadyExists", msg);
                return USER_UPDATE_VIEW;
            }
        }
        String nickname = person.getNickname();
        if (!nickname.equals(currentUser.getNickname())) {
            if (personService.findByNickname(nickname).isPresent()) {
                String msg = getMessage(USER_EXISTS_ERROR_MSG_KEY);
                bindingResult.rejectValue("nickname", "nickname.alreadyExists", msg);
                return USER_UPDATE_VIEW;
            }
        }
        String password = person.getHash();
        if (!passwordEncoder.matches(password, currentUser.getHash())) {
            person.setHash(passwordEncoder.encode(password));
        } else {
            person.setHash(currentUser.getHash());
        }
        try {
            personService.save(person);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            LOGGER.warn("Optimistic lock: {}", e.getMessage());
            session.setAttribute(PERSON_SESSION_ATTR, personService.findById(person.getId()).orElse(null));
            return "redirect:/user/update?error=lock";
        }
        session.setAttribute(PERSON_SESSION_ATTR, personService.findById(person.getId()).orElseThrow());
        return "redirect:/user/update?success";
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private void sendConfirmationMail(HttpServletRequest request, String email, String token)
            throws MalformedURLException, MessagingException {
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable("url", getBaseUrl(request) + "/user/create/confirm?email=" + email + "&token=" + token);
        String subject = getMessage(SUBJECT_TEXT_MSG_KEY);
        String text = templateEngine.process(EMAIL_TEMPLATE, context);
        mailService.send(senderEmail, email, subject, text);
    }

    private String getBaseUrl(HttpServletRequest request) throws MalformedURLException {
        URL url = new URL(request.getRequestURL().toString());
        String port = url.getPort() == -1 ? "" : ":" + url.getPort();
        return url.getProtocol() + "://" + url.getHost() + port;
    }

    private boolean verify(String email, String token) throws ServiceException {
        Optional<Confirmation> optional = confirmationService.findByPersonEmail(email);
        if (!optional.isPresent()) {
            return false;
        }
        Confirmation confirmation = optional.get();
        if (confirmation.isEnabled() || !confirmation.getToken().equals(token)) {
            return false;
        }
        confirmation.setEnabled(true);
        LOGGER.info("User's email has confirmed: {}", email);
        return confirmationService.save(confirmation) != null;
    }
}
