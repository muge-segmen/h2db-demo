package ing.service;

import ing.dto.Asset;
import ing.dto.Customer;
import ing.dto.Order;
import ing.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    public static Connection connection;

    @Autowired
    JdbcTemplate template;

    public void initializeDB(String customerName, String password) throws SQLException {
        //connect to DB
        String jdbcURL = "jdbc:h2:tcp://localhost/~/test";
        Connection conn = DriverManager.getConnection(jdbcURL, customerName, password);
        log.info("Connection established.");

        PreparedStatement initializeStatement;
        //create tables
        initializeStatement = conn.prepareStatement("CREATE TABLE Orders (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, orderSide VARCHAR(5) NOT NULL, size BIGINT NOT NULL, price DOUBLE PRECISION NOT NULL, status VARCHAR(10) NOT NULL, createDate DATE NOT NULL)");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("CREATE TABLE Assets (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, size BIGINT NOT NULL)");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("CREATE TABLE Customers (customerId INTEGER NOT NULL, customerName VARCHAR(50) NOT NULL, password VARCHAR(8) NOT NULL, isAdmin BOOLEAN NOT NULL, role VARCHAR(10) NOT NULL)");
        initializeStatement.executeUpdate();

        //add admin user to customers table
        initializeStatement = conn.prepareStatement("INSERT INTO Customers (customerId, customerName, password , isAdmin, role) VALUES (1, 'ADMIN', '1234', true, 'ADMIN')");
        initializeStatement.executeUpdate();

        //create user roles
        initializeStatement = conn.prepareStatement("CREATE ROLE MOD");
        initializeStatement.executeUpdate();

        //grant table rights to roles
        initializeStatement = conn.prepareStatement("GRANT SELECT ON ORDERS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT INSERT ON ORDERS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT UPDATE ON ORDERS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT DELETE ON ORDERS TO PUBLIC");
        initializeStatement.executeUpdate();

        initializeStatement = conn.prepareStatement("GRANT SELECT ON ASSETS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT INSERT ON ASSETS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT UPDATE ON ASSETS TO PUBLIC");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT DELETE ON ASSETS TO PUBLIC");
        initializeStatement.executeUpdate();

        initializeStatement = conn.prepareStatement("GRANT SELECT ON ORDERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT INSERT ON ORDERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT UPDATE ON ORDERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT DELETE ON ORDERS TO MOD");
        initializeStatement.executeUpdate();

        initializeStatement = conn.prepareStatement("GRANT SELECT ON ASSETS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT INSERT ON ASSETS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT UPDATE ON ASSETS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT DELETE ON ASSETS TO MOD");
        initializeStatement.executeUpdate();

        initializeStatement = conn.prepareStatement("GRANT SELECT ON CUSTOMERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT INSERT ON CUSTOMERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT UPDATE ON CUSTOMERS TO MOD");
        initializeStatement.executeUpdate();
        initializeStatement = conn.prepareStatement("GRANT DELETE ON CUSTOMERS TO MOD");
        initializeStatement.executeUpdate();

        initializeStatement = conn.prepareStatement("GRANT SELECT ON CUSTOMERS TO PUBLIC");
        initializeStatement.executeUpdate();
    }

    public void login(String customerName, String password) throws SQLException {
        String jdbcURL = "jdbc:h2:tcp://localhost/~/test";
        connection = DriverManager.getConnection(jdbcURL, customerName, password);
        log.info("Connection established.");
    }

    public void logout() throws SQLException {
        connection.close();
        connection = null;
        log.info("Connection terminated.");
    }

    public void checkLogin() {
        if (connection == null) {
            throw new RuntimeException("Login needed");
        }
    }

    public boolean isAdmin() throws SQLException {
        Statement statementAdmin = connection.createStatement();
        String sqlUserAdmin = "SELECT IS_ADMIN FROM INFORMATION_SCHEMA.USERS WHERE USER_NAME = SESSION_USER";
        ResultSet rsAdmin = statementAdmin.executeQuery(sqlUserAdmin);
        rsAdmin.next();

        return rsAdmin.getBoolean("IS_ADMIN");
    }

    //only available for admin users
    public void addUser(String customerName, String password, int customerId, boolean isAdmin, Role role) throws SQLException {
        //checks for login
        checkLogin();

        if (customerId < 1) {
            throw new RuntimeException("Customer id must be a positive number");
        }

        PreparedStatement sql;
        if (isAdmin)
            sql = connection.prepareStatement("CREATE USER IF NOT EXISTS " + customerName.toUpperCase() + " PASSWORD '" + password + "' admin");
        else
            sql = connection.prepareStatement("CREATE USER IF NOT EXISTS " + customerName.toUpperCase() + " PASSWORD '" + password + "'");
        sql.executeUpdate();

        //whenever add a user to db also add that user to customers table
        int items = template.update(connectionList -> connection.prepareStatement("INSERT INTO Customers (customerId,  customerName,  password,  isAdmin,  role)" +
                "VALUES (" + customerId + ", '" + customerName.toUpperCase() + "', '" + password + "', " + isAdmin + ", '" + role + "')"));

        if (items > 0) {
            log.info("Inserted a new customer.");
        }
    }

    //only available for admin users
    public void deleteUser(String customerName) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement sql = connection.prepareStatement("DROP USER IF EXISTS " + customerName);
        sql.executeUpdate();

        //whenever remove a user from db also remove that user from customers table
        int items = template.update(connectionList -> connection.prepareStatement("DELETE FROM Customers WHERE customerName = '" + customerName.toUpperCase() + "'"));

        if (items > 0) {
            log.info("Removed customer named {}", customerName);
        }
    }

    //only available for admin users
    public void createTable(String tableName) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement sql = null;
        if (tableName.equalsIgnoreCase("Orders"))
            sql = connection.prepareStatement("CREATE TABLE " + tableName + " (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, orderSide VARCHAR(5) NOT NULL, size BIGINT NOT NULL, price DOUBLE PRECISION NOT NULL, status VARCHAR(10) NOT NULL, createDate DATE NOT NULL)");
        else if (tableName.equalsIgnoreCase("Assets"))
            sql = connection.prepareStatement("CREATE TABLE " + tableName + " (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, size BIGINT NOT NULL)");
        else if (tableName.equalsIgnoreCase("Customers"))
            sql = connection.prepareStatement("CREATE TABLE " + tableName + " (customerId INTEGER NOT NULL, customerName VARCHAR(50) NOT NULL, password VARCHAR(8) NOT NULL, isAdmin BOOLEAN NOT NULL, role VARCHAR(10) NOT NULL)");

        assert sql != null;
        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("{} table is created", tableName);
        }
    }

    //only available for admin users
    public void clearTable(String tableName) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement sql = connection.prepareStatement("TRUNCATE TABLE " + tableName);
        int rows = sql.executeUpdate();

        if (rows > 0) {
            log.info("{} table is cleared", tableName);
        }
    }

    //only available for admin users
    public void deleteTable(String tableName) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement sql = connection.prepareStatement("DROP TABLE " + tableName);
        int rows = sql.executeUpdate();

        if (rows > 0) {
            log.info("{} table is deleted", tableName);
        }
    }

    public void checkPermission(int id, Status status) throws SQLException {
        PreparedStatement customer = connection.prepareStatement("SELECT * FROM Customers WHERE customerName = '" + connection.getMetaData().getUserName() + "'");
        customer.executeQuery();
        ResultSet rsCustomer = customer.getResultSet();
        rsCustomer.next();

        PreparedStatement order = connection.prepareStatement("SELECT customerId FROM Orders WHERE id = " + id);
        order.executeQuery();
        ResultSet rsOrder = order.getResultSet();
        rsOrder.next();

        String customerRole = rsCustomer.getString("ROLE");
        int customerId = rsCustomer.getInt("CUSTOMERID");
        int orderCustomerId = rsOrder.getInt("CUSTOMERID");

        if (customerRole.equalsIgnoreCase("PUBLIC") && customerId != orderCustomerId) {
            throw new RuntimeException("No permission to manipulate other customer's orders");
        } else if (customerRole.equalsIgnoreCase("PUBLIC") && customerId == orderCustomerId && String.valueOf(status).equalsIgnoreCase("MATCHED")) {
            throw new RuntimeException("No permission to approve own order");
        }
    }

    //if user role is not admin/moderator then user cannot list tables
    //note: instead of controlling whether user is admin, user role is preferred
    public int checkListingPermission() throws SQLException {
        PreparedStatement customer = connection.prepareStatement("SELECT * FROM Customers WHERE customerName = '" + connection.getMetaData().getUserName() + "'");
        customer.executeQuery();
        ResultSet rsCustomer = customer.getResultSet();
        rsCustomer.next();

        String customerRole = rsCustomer.getString("ROLE");
        int customerId = rsCustomer.getInt("CUSTOMERID");

        if (customerRole.equalsIgnoreCase("PUBLIC")) {
            return customerId;
        } else {
            return -1;
        }
    }

    public void insertAsset(Connection connection, int customerId, String assetName, int size) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("INSERT INTO Assets (customerId,  assetName,  size)" +
                "VALUES (" + customerId + ", '" + assetName + "', " + size + ")");

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Inserted a new row to Assets table.");
        }
    }

    public void deleteAsset(Connection connection, int id) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("DELETE FROM Assets WHERE id = " + id);

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Deleted asset id: {} from Assets table", id);
        }
    }

    public void updateAsset(Connection connection, int id, int size) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("UPDATE Assets SET size = " + size + " WHERE id = " + id);

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Updated asset id: {} in Assets table", id);
        }
    }

    public void checkAndUpdateAssetTable(Connection connection, ResultSet rsAsset, Boolean assetExist, Status status, String side, int size, int customerId, String assetName) throws SQLException {
        if (String.valueOf(status).equalsIgnoreCase("MATCHED") && side.equalsIgnoreCase("BUY")) {

            if (!assetExist) { //new asset will be added to Assets table
                insertAsset(connection, customerId, assetName, size);

            } else { //size of the existing asset will be updated in Assets table
                int selectAssetId = rsAsset.getInt("id");
                int sizeAsset = rsAsset.getInt("size");
                updateAsset(connection, selectAssetId, sizeAsset + size);
            }

        } else if (String.valueOf(status).equalsIgnoreCase("MATCHED") && side.equalsIgnoreCase("SELL")) {

            int selectAssetId = rsAsset.getInt("id");
            int sizeAsset = rsAsset.getInt("size");

            if (size == sizeAsset) { //asset will be deleted from Assets table
                deleteAsset(connection, selectAssetId);

            } else if (size < sizeAsset) { //size of the existing asset will be updated in Assets table
                updateAsset(connection, selectAssetId, sizeAsset - size);
            }
        }
    }

    public void insert(int customerId, String assetName, OrderSide orderSide, int size, Double price, Status status, LocalDate createDate) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement customer = connection.prepareStatement("SELECT * FROM Customers WHERE customerName = '" + connection.getMetaData().getUserName() + "'");
        customer.executeQuery();
        ResultSet rsCustomer = customer.getResultSet();
        rsCustomer.next();
        String customerRole = rsCustomer.getString("ROLE");

        //if user role is admin/moderator then user can insert order for any customer
        //in other cases, users can only insert order for themselves, so customerId is automatically assigned from login info
        //note: instead of controlling whether user is admin, user role is preferred
        PreparedStatement sql;
        if (customerRole.equalsIgnoreCase("PUBLIC")) {
            sql = connection.prepareStatement("INSERT INTO Orders (customerId,  assetName,  orderSide,  size,  price,  status,  createDate)" +
                    "VALUES (" + rsCustomer.getInt("CUSTOMERID") + ", '" + assetName + "', '" + orderSide + "', " + size + ", " + price + ", '" + status + "', PARSEDATETIME('" + createDate + "','yyyy-MM-dd'))");

        } else {
            sql = connection.prepareStatement("INSERT INTO Orders (customerId,  assetName,  orderSide,  size,  price,  status,  createDate)" +
                    "VALUES (" + customerId + ", '" + assetName + "', '" + orderSide + "', " + size + ", " + price + ", '" + status + "', PARSEDATETIME('" + createDate + "','yyyy-MM-dd'))");
        }

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Inserted a new row.");
        }
    }

    public void delete(int id) throws SQLException {
        //checks for login
        checkLogin();
        //checks whether user is admin
        if (!isAdmin()) {
            checkPermission(id, null);
        }

        PreparedStatement sql = connection.prepareStatement("DELETE FROM Orders WHERE id = " + id);

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Deleted order id: {}", id);
        }
    }

    public void update(int id, Status status) throws SQLException {
        //checks for login
        checkLogin();

        PreparedStatement select = connection.prepareStatement("SELECT * FROM Orders WHERE id = " + id);
        ResultSet rs = select.executeQuery();
        rs.next();
        int customerId = rs.getInt("customerId");
        String assetName = rs.getString("assetName");
        int size = rs.getInt("size");
        String side = rs.getString("orderSide");

        //checks whether user is admin
        if (!isAdmin()) {
            checkPermission(id, status);
        }

        PreparedStatement selectAsset = connection.prepareStatement("SELECT * FROM Assets WHERE customerId = " + customerId + " AND assetName = '" + assetName + "'");
        ResultSet rsAsset = selectAsset.executeQuery();
        rsAsset.next();
        boolean assetExist = rsAsset.getRow() != 0;

        //validate sell order size
        if (String.valueOf(status).equalsIgnoreCase("MATCHED") && side.equalsIgnoreCase("SELL")) {
            if ((!assetExist) || (size > rsAsset.getInt("size"))) {
                //automatically cancels invalid orders
                PreparedStatement sql = connection.prepareStatement("UPDATE Orders SET status = 'CANCELLED' WHERE id = " + id);
                sql.executeUpdate();
                throw new RuntimeException("Sell order cannot exceed asset size");
            }
        }

        //update order
        PreparedStatement sql = connection.prepareStatement("UPDATE Orders SET status = '" + status + "' WHERE id = " + id);

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Updated order id: {} as {}", id, status);
        }

        //update asset table accordingly
        checkAndUpdateAssetTable(connection, rsAsset, assetExist, status, side, size, customerId, assetName);
    }

    public Object listAllEntities(String tableName) throws SQLException {
        //checks for login
        checkLogin();
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list all entities");
        }

        if (tableName.equals("Orders")) {
            List<Order> items;
            items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM " + tableName), (result, rowNum) -> new Order(result.getInt("id"), result.getInt("customerId"), result.getString("assetName"), OrderSide.valueOf(result.getString("orderSide")), result.getInt("size"), result.getDouble("price"), Status.valueOf(result.getString("status")), LocalDate.parse(result.getString("createDate"))));

            //prints all table values
            int count = 1;
            for (Order item : items) {
                log.info("");
                log.info("{} #{}", tableName, count);
                log.info("{}: {}", "id", item.getId());
                log.info("{}: {}", "customerId", item.getCustomerId());
                log.info("{}: {}", "assetName", item.getAssetName());
                log.info("{}: {}", "orderSide", item.getOrderSide());
                log.info("{}: {}", "size", item.getSize());
                log.info("{}: {}", "price", item.getPrice());
                log.info("{}: {}", "status", item.getStatus());
                log.info("{}: {}", "createDate", item.getCreateDate());
                count++;
            }
            return items;
        } else {
            List<Asset> items;
            items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM " + tableName), (result, rowNum) -> new Asset(result.getInt("id"), result.getInt("customerId"), result.getString("assetName"), result.getInt("size")));

            //prints all table values
            int count = 1;
            for (Asset item : items) {
                log.info("");
                log.info("{} #{}", tableName, count);
                log.info("{}: {}", "id", item.getId());
                log.info("{}: {}", "customerId", item.getCustomerId());
                log.info("{}: {}", "assetName", item.getAssetName());
                log.info("{}: {}", "size", item.getSize());
                count++;
            }
            return items;
        }
    }

    public Object listCustomerRecords(int customerId, String tableName) throws SQLException {
        //checks for login
        checkLogin();
        //checks for user role
        //if customer is not admin/moderator then cannot list other customers' records
        int checkId = checkListingPermission();
        if (checkId != -1 && checkId != customerId) {
            throw new RuntimeException("No permission to list other customer's records");
        }

        if (tableName.equals("Orders")) {
            List<Order> items;
            items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM " + tableName + " WHERE customerId = " + customerId), (result, rowNum) -> new Order(result.getInt("id"), result.getInt("customerId"), result.getString("assetName"), OrderSide.valueOf(result.getString("orderSide")), result.getInt("size"), result.getDouble("price"), Status.valueOf(result.getString("status")), LocalDate.parse(result.getString("createDate"))));

            //prints all values for the given customer
            int count = 1;
            for (Order item : items) {
                log.info("");
                log.info("{} #{} for customer id {}", tableName, count, customerId);
                log.info("{}: {}", "id", item.getId());
                log.info("{}: {}", "customerId", item.getCustomerId());
                log.info("{}: {}", "assetName", item.getAssetName());
                log.info("{}: {}", "orderSide", item.getOrderSide());
                log.info("{}: {}", "size", item.getSize());
                log.info("{}: {}", "price", item.getPrice());
                log.info("{}: {}", "status", item.getStatus());
                log.info("{}: {}", "createDate", item.getCreateDate());
                count++;
            }
            return items;
        } else {
            List<Asset> items;
            items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM " + tableName + " WHERE customerId = " + customerId), (result, rowNum) -> new Asset(result.getInt("id"), result.getInt("customerId"), result.getString("assetName"), result.getInt("size")));

            //prints all values for the given customer
            int count = 1;
            for (Asset item : items) {
                log.info("");
                log.info("{} #{} for customer id {}", tableName, count, customerId);
                log.info("{}: {}", "id", item.getId());
                log.info("{}: {}", "customerId", item.getCustomerId());
                log.info("{}: {}", "assetName", item.getAssetName());
                log.info("{}: {}", "size", item.getSize());
                count++;
            }
            return items;
        }
    }

    public List<Order> listPendingOrders() throws SQLException {
        //checks for login
        checkLogin();
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list pending orders");
        }

        List<Order> items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM Orders WHERE status = 'PENDING'"), (result, rowNum) -> new Order(result.getInt("id"), result.getInt("customerId"), result.getString("assetName"), OrderSide.valueOf(result.getString("orderSide")), result.getInt("size"), result.getDouble("price"), Status.valueOf(result.getString("status")), LocalDate.parse(result.getString("createDate"))));

        //prints all pending orders
        int count = 1;
        for (Order item : items) {
            log.info("");
            log.info("Pending orders #{}", count);
            log.info("{}: {}", "id", item.getId());
            log.info("{}: {}", "customerId", item.getCustomerId());
            log.info("{}: {}", "assetName", item.getAssetName());
            log.info("{}: {}", "orderSide", item.getOrderSide());
            log.info("{}: {}", "size", item.getSize());
            log.info("{}: {}", "price", item.getPrice());
            log.info("{}: {}", "status", item.getStatus());
            log.info("{}: {}", "createDate", item.getCreateDate());
            count++;
        }
        return items;
    }

    public List<Customer> listCustomers() throws SQLException {
        //checks for login
        checkLogin();
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list customers");
        }

        List<Customer> items;
        items = template.query(connectionList -> connection.prepareStatement("SELECT * FROM Customers"), (result, rowNum) -> new Customer(result.getInt("customerId"), result.getString("customerName"), result.getString("password"), result.getBoolean("isAdmin"), Role.valueOf(result.getString("role"))));

        //prints all customers
        int count = 1;
        for (Customer item : items) {
            log.info("");
            log.info("Customer #{}", count);
            log.info("{}: {}", "customerId", item.getCustomerId());
            log.info("{}: {}", "customerName", item.getCustomerName());
            log.info("{}: {}", "password", item.getPassword());
            log.info("{}: {}", "isAdmin", item.isAdmin());
            log.info("{}: {}", "role", item.getRole());
            count++;
        }
        return items;
    }
}
