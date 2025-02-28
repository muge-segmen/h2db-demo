package ing.controller;

import ing.models.OrderSide;
import ing.models.Role;
import ing.models.Status;
import ing.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;

@RestController
@RequestMapping("/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/initializeDB")
    public void initializeDB(@RequestParam String customerName, @RequestParam String password) throws SQLException {
        demoService.initializeDB(customerName, password);
    }

    @PostMapping("/login")
    public void login(@RequestParam String customerName, @RequestParam String password) throws SQLException {
        demoService.login(customerName, password);
    }

    @PostMapping("/logout")
    public void logout() throws SQLException {
        demoService.logout();
    }

    @PostMapping("/add-user")
    public void addUser(@RequestParam String customerName, @RequestParam String password, @RequestParam int customerId, @RequestParam boolean isAdmin, @RequestParam Role role) throws SQLException {
        demoService.addUser(customerName, password, customerId, isAdmin, role);
    }

    @DeleteMapping("/delete-user")
    public void deleteUser(@RequestParam String customerName) throws SQLException {
        demoService.deleteUser(customerName);
    }

    @PostMapping("/create-table")
    public void createTable(@RequestParam String tableName) throws SQLException {
        demoService.createTable(tableName);
    }

    @PostMapping("/clear-table")
    public void clearTable(@RequestParam String tableName) throws SQLException {
        demoService.clearTable(tableName);
    }

    @DeleteMapping("/delete-table")
    public void deleteTable(@RequestParam String tableName) throws SQLException {
        demoService.deleteTable(tableName);
    }

    @PostMapping("/insert")
    public void insert(@RequestParam int customerId,
                       @RequestParam String assetName,
                       @RequestParam OrderSide orderSide,
                       @RequestParam int size,
                       @RequestParam Double price,
                       @RequestParam(defaultValue = "PENDING") Status status,
                       @RequestParam(name = "createDate", defaultValue = "#{T(java.time.LocalDate).now()}", required = false) LocalDate createDate) throws SQLException {
        demoService.insert(customerId, assetName, orderSide, size, price, status, createDate);
    }

    @PutMapping("/update")
    public void update(@RequestParam int id, @RequestParam Status status) throws SQLException {
        demoService.update(id, status);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam int id) throws SQLException {
        demoService.delete(id);
    }

    @GetMapping("/list-all-entities")
    public void listAllOrders(@RequestParam String tableName) throws SQLException {
        demoService.listAllEntities(tableName);
    }

    @GetMapping("/list-customer-records")
    public void listCustomerOrders(@RequestParam int customerId, @RequestParam String tableName) throws SQLException {
        demoService.listCustomerRecords(customerId, tableName);
    }

    @GetMapping("/list-pending-orders")
    public void listPendingOrders() throws SQLException {
        demoService.listPendingOrders();
    }

    @GetMapping("/list-customers")
    public void listCustomers() throws SQLException {
        demoService.listCustomers();
    }

//    //constructor without lombok
//    public DemoController(DemoService demoService) {
//        this.demoService = demoService;
//    }
}
