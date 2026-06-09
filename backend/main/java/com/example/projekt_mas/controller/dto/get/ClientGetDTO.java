package com.example.projekt_mas.controller.dto.get;

import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.client.CompanyClient;
import com.example.projekt_mas.domain.client.IndividualClient;

import java.math.BigDecimal;
import java.util.List;

public record ClientGetDTO(
        Long id,
        String type,
        String address,
        String phoneNumber,
        String email,
        String firstName,
        String lastName,
        String companyName,
        String nip,
        int loyaltyLevel,
        List<Long> orderIds
) {

    public static ClientGetDTO from(Client client) {
        String type = "CLIENT";
        String firstName = null;
        String lastName = null;
        String companyName = null;
        String nip = null;

        if (client instanceof IndividualClient individualClient) {
            type = "INDIVIDUAL";
            firstName = individualClient.getFirstName();
            lastName = individualClient.getLastName();
        } else if (client instanceof CompanyClient companyClient) {
            type = "COMPANY";
            companyName = companyClient.getCompanyName();
            nip = companyClient.getNip();
        }

        List<Long> orderIds = client.getOrders().stream()
                .map(order -> order.getId())
                .toList();

        return new ClientGetDTO(
                client.getId(),
                type,
                client.getAddress(),
                client.getPhoneNumber(),
                client.getEmail(),
                firstName,
                lastName,
                companyName,
                nip,
                client.getLoyaltyLevel(),
                orderIds
        );
    }
}
