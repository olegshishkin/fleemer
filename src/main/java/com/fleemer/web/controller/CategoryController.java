package com.fleemer.web.controller;

import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import java.util.List;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private static final String CATEGORY_UPDATE_VIEW = "category_update";
    private static final String CATEGORY_EXISTS_ERROR_KEY = "categories.error.user-exists";
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String REDIRECT_CATEGORIES_URL = "redirect:/categories";
    private static final String ROOT_VIEW = "categories";

    private final CategoryService categoryService;
    private final MessageSource messageSource;
    private final OperationService operationService;

    @Autowired
    public CategoryController(CategoryService categoryService, OperationService operationService,
                              MessageSource messageSource) {
        this.categoryService = categoryService;
        this.operationService = operationService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String categories(Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        fillModel(model, categoryService.findAll(person));
        model.addAttribute("category", new Category());
        return ROOT_VIEW;
    }

    @ResponseBody
    @GetMapping(value = "/json", params = {"type"})
    public List<Category> categories(@RequestParam("type") CategoryType type, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        return categoryService.findAllByTypeAndPerson(type, person);
    }

    @LogAfterReturning
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Category category, BindingResult bindingResult, Model model,
                         HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        if (bindingResult.hasErrors()) {
            fillModel(model, categoryService.findAll(person));
            return ROOT_VIEW;
        }
        Optional<Category> optional = categoryService.findByNameAndPerson(category.getName(), person);
        if (optional.isPresent()) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(CATEGORY_EXISTS_ERROR_KEY));
            fillModel(model, categoryService.findAll(person));
            return ROOT_VIEW;
        }
        category.setPerson(person);
        categoryService.save(category);
        return REDIRECT_CATEGORIES_URL;
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, HttpSession session) {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Category> category = categoryService.findByIdAndPerson(id, person);
        if (!category.isPresent()) {
            return REDIRECT_CATEGORIES_URL;
        }
        model.addAttribute("category", category.get());
        model.addAttribute("categoryTypes", CategoryType.values());
        return CATEGORY_UPDATE_VIEW;
    }

    @LogAfterReturning
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("category") Category formCategory, BindingResult bindingResult, 
                         Model model, HttpSession session) throws ServiceException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryTypes", CategoryType.values());
            return CATEGORY_UPDATE_VIEW;
        }
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Category> optional = categoryService.findByIdAndPerson(formCategory.getId(), person);
        if (!optional.isPresent()) {
            return REDIRECT_CATEGORIES_URL;
        }
        Category category = optional.get();
        if (!canUseName(category, formCategory, person)) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(CATEGORY_EXISTS_ERROR_KEY));
            fillModel(model, categoryService.findAll(person));
            return CATEGORY_UPDATE_VIEW;
        }
        formCategory.setPerson(category.getPerson());
        try {
            categoryService.save(formCategory);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            LOGGER.warn("Optimistic lock: {}", e.getMessage());
            return REDIRECT_CATEGORIES_URL + "?error=lock";
        }
        return REDIRECT_CATEGORIES_URL + "?success";
    }

    @LogAfterReturning
    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, HttpSession session) throws ServiceException {
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Category> optional = categoryService.findByIdAndPerson(id, person);
        if (optional.isPresent()) {
            Category category = optional.get();
            long operationsCount = operationService.countOperationsByCategory(category);
            if (operationsCount > 0) {
                return REDIRECT_CATEGORIES_URL + "?deleteForbidden";
            }
            categoryService.delete(category);
        }
        return REDIRECT_CATEGORIES_URL;
    }

    private boolean canUseName(Category category, Category formCategory, Person person) {
        String name = category.getName();
        String formName = formCategory.getName();
        if (name.equals(formName)) {
            return true;
        }
        return !categoryService.findByNameAndPerson(formName, person).isPresent();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private void fillModel(@NotNull Model model, Iterable<Category> collection) {
        model.addAttribute("categories", collection);
        model.addAttribute("categoryTypes", CategoryType.values());
    }
}
