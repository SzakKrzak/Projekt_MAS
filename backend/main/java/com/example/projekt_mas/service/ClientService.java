package com.example.projekt_mas.service;

import com.example.projekt_mas.domain.client.CompanyClient;
import com.example.projekt_mas.domain.client.Client;
import com.example.projekt_mas.domain.client.IndividualClient;
import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.repository.ClientRepository;
import com.example.projekt_mas.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public IndividualClient createIndividual(String address, String phoneNumber, String email, String firstName, String lastName) {
        return clientRepository.save(new IndividualClient(address, phoneNumber, email, firstName, lastName));
    }

    @Transactional
    public CompanyClient createCompany(String address, String phoneNumber, String email, String companyName, String nip) {
        return clientRepository.save(new CompanyClient(address, phoneNumber, email, companyName, nip));
    }

    @Transactional
    public Order submitFeedback(Long clientId, Long orderId, String feedback) {
        Client client = getById(clientId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        client.submitFeedback(order, feedback);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Client getById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
    }

    @Transactional(readOnly = true)
    public List<Client> getAll() {
        return clientRepository.findAll();
    }
}
