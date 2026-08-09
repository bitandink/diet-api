package io.github.bitandink.diet_api.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String phone;

    public User(
            String email,
            String password,
            String name,
            String phone
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }
}
