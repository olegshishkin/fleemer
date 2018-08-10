package com.fleemer.model;

import com.fleemer.model.enums.CategoryType;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Category implements Serializable {
    private Long id;
    private String name;
    private CategoryType type;
    private Person person;
}
