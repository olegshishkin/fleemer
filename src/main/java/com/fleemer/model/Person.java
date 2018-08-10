package com.fleemer.model;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
public class Person implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String hash;
}
