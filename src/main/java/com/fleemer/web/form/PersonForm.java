package com.fleemer.web.form;

import com.fleemer.model.Person;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PersonForm {
    @NotNull
    @Valid
    private Person person;

    @NotNull
    @Size(min = 1, max = 255)
    private String confirmPassword;
}
