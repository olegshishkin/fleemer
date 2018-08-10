package com.fleemer.model;

import java.io.Serializable;
import javax.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(columnNames = {"id", "email"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
public class Person implements Serializable {
    @Id
    @Column(unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String hash;
}
