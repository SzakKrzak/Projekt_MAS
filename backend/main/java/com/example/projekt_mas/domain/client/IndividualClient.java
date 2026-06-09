package com.example.projekt_mas.domain.client;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("INDIVIDUAL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndividualClient extends Client {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    public IndividualClient(
            String address,
            String phoneNumber,
            String email,
            String firstName,
            String lastName
    ) {
        super(address, phoneNumber, email);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
