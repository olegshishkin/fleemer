package com.fleemer.web.controller;

import com.fleemer.aop.LogAfterReturning;
import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.model.enums.CategoryType;
import com.fleemer.service.CategoryService;
import com.fleemer.service.OperationService;
import com.fleemer.service.exception.ServiceException;
import com.fleemer.web.form.validator.CategoryValidator;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    private static final String CATEGORY_UPDATE_VIEW = "category_update";
    private static final String PERSON_SESSION_ATTR = "person";
    private static final String REDIRECT_CATEGORIES_URL = "redirect:/categories";
    private static final String ROOT_VIEW = "categories";
    private static final Comparator<Category> comparator = Comparator.comparing(Category::getName);
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;
    private final CategoryValidator categoryValidator;
    private final OperationService operationService;

    @Autowired
    public CategoryController(CategoryService categoryService, CategoryValidator categoryValidator,
                              OperationService operationService) {
        this.categoryService = categoryService;
        this.categoryValidator = categoryValidator;
        this.operationService = operationService;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(categoryValidator);
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
        List<Category> categories = categoryService.findAllByTypeAndPerson(type, person);
        categories.sort(comparator);
        return categories;
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
        formCategory.setPerson(optional.get().getPerson());
        try {
            categoryService.save(formCategory);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock: {}", e.getMessage());
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

    private void fillModel(@NotNull Model model, Iterable<Category> collection) {
        model.addAttribute("categories", collection);
        model.addAttribute("categoryTypes", CategoryType.values());
    }
}
