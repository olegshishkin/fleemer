package com.fleemer.web.controller;

import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Confirmation;
import com.fleemer.model.Person;
import com.fleemer.service.ConfirmationService;
import com.fleemer.service.MailService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.web.form.PersonForm;
import com.fleemer.web.form.validator.PersonFormValidator;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final String EMAIL_TEMPLATE = "mail";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String SUBJECT_TEXT_MSG_KEY = "mail.subject";
    private static final String USER_CREATE_VIEW = "user_create";
    private static final String USER_JOINING_MSG = "New user has joined to Fleemer: ";
    private static final String USER_UPDATE_VIEW = "user_update";
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final ConfirmationService confirmationService;
    private final MailService mailService;
    private final MessageSource messageSource;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PersonService personService;
    private final PersonFormValidator personFormValidator;
    private final TemplateEngine templateEngine;

    @Value("${com.fleemer.owner.email}")
    private String ownerEmail;
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Autowired
    public UserController(TemplateEngine templateEngine, PersonService personService,
                          BCryptPasswordEncoder passwordEncoder, ConfirmationService confirmationService,
                          MailService mailService, MessageSource messageSource,
                          PersonFormValidator personFormValidator) {
        this.templateEngine = templateEngine;
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
        this.confirmationService = confirmationService;
        this.mailService = mailService;
        this.messageSource = messageSource;
        this.personFormValidator = personFormValidator;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(personFormValidator);
    }

    @GetMapping("/create")
    public ModelAndView create() {
        PersonForm personForm = new PersonForm();
        personForm.setPerson(new Person());
        return new ModelAndView(USER_CREATE_VIEW, "personForm", personForm);
    }

    @LogAfterReturning
    @PostMapping("/create")
    public String create(HttpServletRequest request, @Valid @ModelAttribute PersonForm personForm,
                         BindingResult bindingResult) throws ServiceException, MessagingException,
            MalformedURLException {
        if (bindingResult.hasErrors()) {
            return USER_CREATE_VIEW;
        }
        Person person = personForm.getPerson();
        person.setHash(passwordEncoder.encode(person.getHash()));
        String email = person.getEmail();
        String token = UUID.randomUUID().toString();
        personService.saveAndConfirm(person, token);
        try {
            sendConfirmationMail(request, email, token);
        } catch (MalformedURLException e) {
            personService.delete(person);
            throw e;
        }
        logger.info("New user has created: {}", email);
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
            logger.info("User has confirmed: {}", email);
        }
        return "redirect:/user/create/status?confirm=" + (success ? "success" : "failed");
    }

    @GetMapping("/update")
    public ModelAndView update(HttpSession session) {
        PersonForm personForm = new PersonForm();
        personForm.setPerson((Person) session.getAttribute(PERSON_SESSION_ATTR));
        return new ModelAndView(USER_UPDATE_VIEW, "personForm", personForm);
    }

    @LogAfterReturning
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute PersonForm personForm, BindingResult bindingResult, HttpSession session)
            throws ServiceException {//todo make email update confirmation and password recovery
        if (bindingResult.hasErrors()) {
            return USER_UPDATE_VIEW;
        }
        Person persistedPerson = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Person formPerson = personForm.getPerson();
        if (!formPerson.getId().equals(persistedPerson.getId())) {
            return "redirect:/";
        }
        String password = formPerson.getHash();
        if (!passwordEncoder.matches(password, persistedPerson.getHash())) {
            formPerson.setHash(passwordEncoder.encode(password));
        } else {
            formPerson.setHash(persistedPerson.getHash());
        }
        try {
            personService.save(formPerson);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock: {}", e.getMessage());
            session.setAttribute(PERSON_SESSION_ATTR, personService.findById(formPerson.getId()).orElse(null));
            return "redirect:/user/update?error=lock";
        }
        session.setAttribute(PERSON_SESSION_ATTR, personService.findById(formPerson.getId()).orElseThrow());
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
        logger.info("User's email has confirmed: {}", email);
        return confirmationService.save(confirmation) != null;
    }
}
