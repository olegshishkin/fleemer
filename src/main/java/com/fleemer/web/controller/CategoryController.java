package com.fleemer.web.controller;

import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private static final String CATEGORY_EDIT_VIEW = "category_edit";
    private static final String CATEGORY_EXISTS_ERROR_KEY = "categories.error.user-exists";
    private static final String DELETING_FORBIDDEN_ERROR_KEY = "categories.error.delete-forbidden";
    private static final String ROOT_VIEW = "categories";

    private final CategoryService categoryService;
    private final MessageSource messageSource;
    private final OperationService operationService;
    private final PersonService personService;

    @Autowired
    public CategoryController(CategoryService categoryService, PersonService personService,
                              OperationService operationService, MessageSource messageSource) {
        this.categoryService = categoryService;
        this.personService = personService;
        this.operationService = operationService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String categories(Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        fillModel(model, categoryService.findAll(person));
        model.addAttribute("category", new Category());
        return ROOT_VIEW;
    }

    @ResponseBody
    @GetMapping(value = "/json", params = {"type"})
    public List<Category> categories(@RequestParam("type") CategoryType type, Principal principal) {
        return categoryService.findAllByTypeAndPerson(type, getCurrentPerson(principal));
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Category category, BindingResult bindingResult, Model model,
                                Principal principal) throws ServiceException {
        Person person = getCurrentPerson(principal);
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
        return "redirect:/categories";
    }

    @GetMapping("/update")
    public String update(@RequestParam("id") long id, Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        Category category = categoryService.findById(id).orElseThrow();
        if (!isOwned(person, category)) {
            return "redirect:/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("categoryTypes", CategoryType.values());
        return CATEGORY_EDIT_VIEW;
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("category") Category formCategory, BindingResult bindingResult, 
                         Model model, Principal principal) throws ServiceException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryTypes", CategoryType.values());
            return CATEGORY_EDIT_VIEW;
        }
        Person person = getCurrentPerson(principal);
        Optional<Category> optional = categoryService.getByIdAndPerson(formCategory.getId(), person);
        if (!optional.isPresent()) {
            return "redirect:/categories";
        }
        Category category = optional.get();
        if (!canUseName(category, formCategory, person)) {
            bindingResult.rejectValue("name", "name.alreadyExists", getMessage(CATEGORY_EXISTS_ERROR_KEY));
            fillModel(model, categoryService.findAll(person));
            return CATEGORY_EDIT_VIEW;
        }
        category.setName(formCategory.getName());
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("id") long id, Model model, Principal principal) {
        Person person = getCurrentPerson(principal);
        Optional<Category> optional = categoryService.getByIdAndPerson(id, person);
        if (optional.isPresent()) {
            Category category = optional.get();
            List<Operation> relatedOperations = operationService.findAllByCategory(category);
            if (!relatedOperations.isEmpty()) {
                fillModel(model, categoryService.findAll(person));
                model.addAttribute("error", getMessage(DELETING_FORBIDDEN_ERROR_KEY));
                model.addAttribute("category", new Category());
                return ROOT_VIEW;
            }
            categoryService.delete(category);
        }
        return "redirect:/categories";
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
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private boolean isOwned(Person person, Category category) {
        return category.getPerson().equals(person);
    }

    private Person getCurrentPerson(@NotNull Principal principal) {
        return personService.findByEmail(principal.getName()).orElseThrow();
    }

    private void fillModel(@NotNull Model model, Iterable<Category> collection) {
        model.addAttribute("categories", collection);
        model.addAttribute("categoryTypes", CategoryType.values());
    }
}
