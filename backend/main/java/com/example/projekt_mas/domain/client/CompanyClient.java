package com.example.projekt_mas.domain.client;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("COMPANY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyClient extends Client {

    private static final String NIP_PATTERN = "\\d{10}";

    @Column(name = "company_name")
    private String companyName;

    @Column(unique = true)
    private String nip;

    public CompanyClient(
            String address,
            String phoneNumber,
            String email,
            String companyName,
            String nip
    ) {
        super(address, phoneNumber, email);
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("NIP cannot be blank");
        }
        String normalizedNip = nip.replaceAll("[\\s-]", "");
        if (!normalizedNip.matches(NIP_PATTERN)) {
            throw new IllegalArgumentException("NIP must contain exactly 10 digits");
        }
        this.companyName = companyName;
        this.nip = normalizedNip;
    }
}
