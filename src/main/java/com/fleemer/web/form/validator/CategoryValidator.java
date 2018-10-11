package com.fleemer.web.form.validator;

import com.fleemer.model.Category;
import com.fleemer.model.Person;
import com.fleemer.service.CategoryService;
import java.util.Locale;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CategoryValidator implements Validator {
    private static final String CATEGORY_EXISTS_ERROR_KEY = "categories.error.user-exists";
    private static final String PERSON_SESSION_ATTR = "person";

    private final CategoryService categoryService;
    private final HttpSession session;
    private final MessageSource messageSource;

    @Autowired
    public CategoryValidator(CategoryService categoryService, @Autowired(required = false) HttpSession session,
                             MessageSource messageSource) {
        this.categoryService = categoryService;
        this.session = session;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Category.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (session == null) {
            throw new IllegalArgumentException("No persons in current session");
        }
        Category category = (Category) target;
        Person person = (Person) session.getAttribute(PERSON_SESSION_ATTR);
        Optional<Category> optional = categoryService.findByNameAndPerson(category.getName(), person);
        if (optional.isPresent()) {
            Category persistedCategory = optional.get();
            if (!persistedCategory.getId().equals(category.getId())) {
                Locale locale = LocaleContextHolder.getLocale();
                String msg = messageSource.getMessage(CATEGORY_EXISTS_ERROR_KEY, null, locale);
                errors.rejectValue("name", "name.alreadyExists", msg);
            }
        }
    }
}
