package com.example.projekt_mas.config;

import com.example.projekt_mas.domain.branch.Branch;
import com.example.projekt_mas.domain.client.CompanyClient;
import com.example.projekt_mas.domain.client.IndividualClient;
import com.example.projekt_mas.domain.employee.Employee;
import com.example.projekt_mas.domain.employee.EmployeeRole;
import com.example.projekt_mas.domain.order.Order;
import com.example.projekt_mas.domain.product.Furniture;
import com.example.projekt_mas.domain.product.FurnitureCategory;
import com.example.projekt_mas.service.BranchService;
import com.example.projekt_mas.service.ClientService;
import com.example.projekt_mas.service.EmployeeService;
import com.example.projekt_mas.service.FurnitureService;
import com.example.projekt_mas.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final BranchService branchService;
    private final ClientService clientService;
    private final FurnitureService furnitureService;
    private final OrderService orderService;

    @Override
    public void run(String... args) {
        if (!clientService.getAll().isEmpty()) {
            return;
        }

        System.out.println("Running seeder...");

        Branch mainBranch = branchService.create("Magazynowa 67", LocalTime.of(8, 0), LocalTime.of(20, 0));

        Employee warehouseCashier = employeeService.create("Marcin", "Gruszecki",
                EnumSet.of(EmployeeRole.CASHIER, EmployeeRole.WAREHOUSE_WORKER));
        Employee designerManager = employeeService.create("Jan", "Paweł",
                EnumSet.of(EmployeeRole.DESIGNER, EmployeeRole.MANAGER));
        Employee designer = employeeService.create("Leonardo", "DaVinki",
                EnumSet.of(EmployeeRole.DESIGNER));

        warehouseCashier = employeeService.addContract(warehouseCashier.getId(),
                LocalDate.of(2024, 1, 1), BigDecimal.valueOf(4500));
        designerManager = employeeService.addContract(designerManager.getId(),
                LocalDate.of(2024, 2, 1), BigDecimal.valueOf(7200));
        designer = employeeService.addContract(designer.getId(),
                LocalDate.of(2024, 3, 1), BigDecimal.valueOf(6100));

        employeeService.assignBranch(warehouseCashier.getId(), mainBranch.getId());
        employeeService.assignBranch(designerManager.getId(), mainBranch.getId());
        employeeService.assignBranch(designer.getId(), mainBranch.getId());

        FurnitureCategory kitchen = furnitureService.createCategory("Kuchnia", "Kuchnia");
        FurnitureCategory office = furnitureService.createCategory("Biuro", "Biuro");
        FurnitureCategory livingRoom = furnitureService.createCategory("Salon", "Salon");

        Furniture oakTable = furnitureService.create("Stół Dębowy", BigDecimal.valueOf(1200),
                List.of("Dąb", "lakier"), kitchen.getId(), designerManager.getId());
        Furniture officeDesk = furnitureService.create("Biurko biurowe", BigDecimal.valueOf(900),
                List.of("Drewno", "amelinium"), office.getId(), designer.getId());
        Furniture wallShelf = furnitureService.create("Półka wisząca (fancy)", BigDecimal.valueOf(120),
                List.of("Machoń"), livingRoom.getId(), designer.getId());

        CompanyClient companyClient = clientService.createCompany(
                "Wspólna 21", "518266633", "biuro@firma.com", "Firma sp.z o.o.", "1234567890");
        IndividualClient individualClient = clientService.createIndividual(
                "Raciniewo 31", "112997998", "pawel@jumper.pl", "Pawel", "Hardcore");
        IndividualClient secondIndividualClient = clientService.createIndividual(
                "Lipinki Łużyckie 7", "555666777", "styrta@sie.pali", "Jadwiga", "Hymel");

        Order companyOrder = orderService.create(companyClient.getId(), companyClient.getAddress());
        companyOrder = orderService.addLine(companyOrder.getId(), oakTable.getId(), 2);
        companyOrder = orderService.addLine(companyOrder.getId(), officeDesk.getId(), 1);
        companyOrder = orderService.pay(companyOrder.getId(), LocalDate.now().plusDays(14));
        companyOrder = orderService.complete(companyOrder.getId(), LocalDate.now().plusDays(14));
        clientService.submitFeedback(companyClient.getId(), companyOrder.getId(), "Oszczędziliśmy na to dzięki zatrudnianiu studentów");

        Order individualOrder = orderService.create(individualClient.getId(), individualClient.getAddress());
        individualOrder = orderService.addLine(individualOrder.getId(), wallShelf.getId(), 3);
        individualOrder = orderService.pay(individualOrder.getId(), LocalDate.now().plusDays(10));

        Order openOrder = orderService.create(secondIndividualClient.getId(), secondIndividualClient.getAddress());
        orderService.addLine(openOrder.getId(), officeDesk.getId(), 1);

        Order cancelledOrder = orderService.create(companyClient.getId(), companyClient.getAddress());
        orderService.cancel(cancelledOrder.getId(), "Skarbówka się dowaliła");
    }
}
