package com.fleemer.web.controller;

import com.fleemer.model.Person;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import javax.validation.Valid;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final String USER_EXISTS_ERROR_MSG_KEY = "userForm.error.user-exists";
    private static final String USER_FORM_VIEW = "userForm";

    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final PersonService personService;

    @Autowired
    public UserController(PersonService personService, BCryptPasswordEncoder passwordEncoder,
                          MessageSource messageSource) {
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ModelAndView show() {
        return new ModelAndView(USER_FORM_VIEW, "person", new Person());
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Person person, BindingResult bindingResult) throws ServiceException {
        if (bindingResult.hasErrors()) {
            return USER_FORM_VIEW;
        }
        String email = person.getEmail();
        if (personService.findByEmail(email).isPresent()) {
            String message = messageSource.getMessage(USER_EXISTS_ERROR_MSG_KEY, null, Locale.getDefault());
            bindingResult.rejectValue("email", "email.alreadyExists", message);
            return USER_FORM_VIEW;
        }
        String hash = passwordEncoder.encode(person.getHash());
        person.setHash(hash);
        personService.save(person);
        return "redirect:/";
    }
}
