package com.fleemer.web.controller;

import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.service.CategoryService;
import com.fleemer.service.PersonService;
import com.fleemer.service.exception.ServiceException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private static final String ROOT_VIEW = "categories";

    private final CategoryService categoryService;
    private final PersonService personService;

    @Autowired
    public CategoryController(CategoryService categoryService, PersonService personService) {
        this.categoryService = categoryService;
        this.personService = personService;
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
        Optional<Category> lookedCategory = categoryService.findByNameAndPerson(category.getName(), person);
        if (lookedCategory.isPresent()) {
            String message = "Category with such name already exists! Try again, please.";
            bindingResult.rejectValue("name", "name.alreadyExists", message);
            fillModel(model, categoryService.findAll(person));
            return ROOT_VIEW;
        }
        category.setPerson(person);
        categoryService.save(category);
        return "redirect:/categories";
    }

    private Person getCurrentPerson(@NotNull Principal principal) {
        return personService.findByEmail(principal.getName()).orElseThrow();
    }

    private void fillModel(@NotNull Model model, Iterable<Category> collection) {
        model.addAttribute("categories", collection);
        model.addAttribute("categoryTypes", CategoryType.values());
    }
}
