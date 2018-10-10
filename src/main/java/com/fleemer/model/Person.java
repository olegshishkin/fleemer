package com.fleemer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"id", "email"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
@ToString
public class Person implements Serializable {
    @Id
    @Column(unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String firstName;

    @Size(max = 255)
    @Column
    private String lastName;

    @Size(min = 1, max = 255)
    @Column(unique = true, nullable = false)
    private String nickname;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @NotNull
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String hash;

    @JsonIgnore
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime creationTime;

    @JsonIgnore
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;

    @JsonIgnore
    @Version
    private int version;
}
