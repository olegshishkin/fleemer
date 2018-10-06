package com.fleemer.service.specification;

import com.fleemer.model.Account;
import com.fleemer.model.Category;
import com.fleemer.model.Operation;
import com.fleemer.model.Person;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

public class OperationSpecification {
    private static final String CATEGORY_FIELD_NAME = "category";
    private static final String COMMENT_FIELD_NAME = "comment";
    private static final String DATE_FIELD_NAME = "date";
    private static final String IN_ACCOUNT_FIELD_NAME = "inAccount";
    private static final String OUT_ACCOUNT_FIELD_NAME = "outAccount";
    private static final String PERSON_FIELD_NAME = "person";
    private static final String SUM_FIELD_NAME = "sum";
    private static final char WILDCARD = '%';

    public static Specification<Operation> createSpecification(boolean orMode, Person person,
                                                               @Nullable LocalDate from, @Nullable LocalDate till,
                                                               @Nullable List<Account> inAccounts,
                                                               @Nullable List<Account> outAccounts,
                                                               @Nullable List<Category> categories,
                                                               @Nullable BigDecimal min, @Nullable BigDecimal max,
                                                               @Nullable String comment) {
        return new Specification<Operation>() {
            @Override
            public Predicate toPredicate(Root<Operation> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                // Non-permanent ('OR' or 'AND') predicates
                List<Predicate> nonPermList = new ArrayList<>();
                if (comment != null) {
                    String pattern = wrapByWildcard(comment).toLowerCase();
                    nonPermList.add(cb.like(cb.lower(root.get(COMMENT_FIELD_NAME)), pattern));
                }
                if (inAccounts != null) {
                    if (inAccounts.isEmpty()) {
                        nonPermList.add(cb.isNull(root.get(IN_ACCOUNT_FIELD_NAME)));
                    } else {
                        nonPermList.add(root.get(IN_ACCOUNT_FIELD_NAME).in(inAccounts));
                    }
                }
                if (outAccounts != null) {
                    if (outAccounts.isEmpty()) {
                        nonPermList.add(cb.isNull(root.get(OUT_ACCOUNT_FIELD_NAME)));
                    } else {
                        nonPermList.add(root.get(OUT_ACCOUNT_FIELD_NAME).in(outAccounts));
                    }
                }
                if (categories != null) {
                    if (categories.isEmpty()) {
                        nonPermList.add(cb.isNull(root.get(CATEGORY_FIELD_NAME)));
                    } else {
                        nonPermList.add(root.get(CATEGORY_FIELD_NAME).in(categories));
                    }
                }
                List<Predicate> predicates = new ArrayList<>();
                if (!nonPermList.isEmpty()) {
                    if (orMode) {
                        predicates.add(cb.or(nonPermList.toArray(new Predicate[]{})));
                    } else {
                        predicates.add(cb.and(nonPermList.toArray(new Predicate[]{})));
                    }
                }

                // Permanent (only 'AND') predicates
                if (min != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(SUM_FIELD_NAME), min));
                }
                if (max != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(SUM_FIELD_NAME), max));
                }
                if (from != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get(DATE_FIELD_NAME), from));
                }
                if (till != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get(DATE_FIELD_NAME), till));
                }
                Path<Object> inAccountPerson = root.join(IN_ACCOUNT_FIELD_NAME, JoinType.LEFT).get(PERSON_FIELD_NAME);
                Path<Object> outAccountPerson = root.join(OUT_ACCOUNT_FIELD_NAME, JoinType.LEFT).get(PERSON_FIELD_NAME);
                predicates.add(cb.or(cb.equal(inAccountPerson, person), cb.equal(outAccountPerson, person)));
                return cb.and(predicates.toArray(new Predicate[]{}));
            }
        };
    }

    private static String wrapByWildcard(String text) {
        return WILDCARD + text + WILDCARD;
    }
}
