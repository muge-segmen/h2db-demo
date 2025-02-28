package ing.service;

import ing.models.OrderSide;
import ing.models.Role;
import ing.models.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    public static Connection connection;

    public void initializeDB(String customerName, String password) throws SQLException {
        //connect to DB
        String jdbcURL = "jdbc:h2:tcp://localhost/~/test";
        Connection conn = DriverManager.getConnection(jdbcURL, customerName, password);
        log.info("Connection established.");
        Statement initializeStatement = conn.createStatement();

        //create tables
        String sqlInitialize = "CREATE TABLE Orders (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, orderSide VARCHAR(5) NOT NULL, size BIGINT NOT NULL, price DOUBLE PRECISION NOT NULL, status VARCHAR(10) NOT NULL, createDate DATE NOT NULL)";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "CREATE TABLE Assets (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, customerId INTEGER NOT NULL, assetName VARCHAR(50) NOT NULL, size BIGINT NOT NULL)";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "CREATE TABLE Customers (customerId INTEGER NOT NULL, customerName VARCHAR(50) NOT NULL, password VARCHAR(8) NOT NULL, isAdmin BOOLEAN NOT NULL, role VARCHAR(10) NOT NULL)";
        initializeStatement.executeQuery(sqlInitialize);

        //create user roles
        sqlInitialize = "CREATE ROLE MOD";
        initializeStatement.executeQuery(sqlInitialize);

        //grant table rights to roles
        sqlInitialize = "GRANT SELECT ON ORDERS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT INSERT ON ORDERS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT UPDATE ON ORDERS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT DELETE ON ORDERS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);

        sqlInitialize = "GRANT SELECT ON ASSETS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT INSERT ON ASSETS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT UPDATE ON ASSETS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT DELETE ON ASSETS TO PUBLIC";
        initializeStatement.executeQuery(sqlInitialize);

        sqlInitialize = "GRANT SELECT ON ORDERS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT INSERT ON ORDERS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT UPDATE ON ORDERS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT DELETE ON ORDERS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);

        sqlInitialize = "GRANT SELECT ON ASSETS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT INSERT ON ASSETS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT UPDATE ON ASSETS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
        sqlInitialize = "GRANT DELETE ON ASSETS TO MOD";
        initializeStatement.executeQuery(sqlInitialize);
    }

    public void login(String customerName, String password) throws SQLException {
        String jdbcURL = "jdbc:h2:tcp://localhost/~/test";
        connection = DriverManager.getConnection(jdbcURL, customerName, password);
        log.info("Connection established.");
    }

    public void logout() throws SQLException {
        connection.close();
        log.info("Connection terminated.");
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
        sql = connection.prepareStatement("INSERT INTO Customers (customerId,  customerName,  password,  isAdmin,  role)" +
                "VALUES (" + customerId + ", '" + customerName.toUpperCase() + "', '" + password + "', " + isAdmin + ", '" + role + "')");

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Inserted a new customer.");
        }
    }

    //only available for admin users
    public void deleteUser(String customerName) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("DROP USER IF EXISTS " + customerName);
        sql.executeUpdate();

        //whenever remove a user from db also remove that user from customers table
        sql = connection.prepareStatement("DELETE FROM Customers WHERE customerName = '" + customerName.toUpperCase() + "'");

        int rows = sql.executeUpdate();
        if (rows > 0) {
            log.info("Removed customer named {}", customerName);
        }
    }

    //only available for admin users
    public void createTable(String tableName) throws SQLException {
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
        PreparedStatement sql = connection.prepareStatement("TRUNCATE TABLE " + tableName);
        int rows = sql.executeUpdate();

        if (rows > 0) {
            log.info("{} table is cleared", tableName);
        }
    }

    //only available for admin users
    public void deleteTable(String tableName) throws SQLException {
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

    public void listAllEntities(String tableName) throws SQLException {
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list all entities");
        }

        PreparedStatement sql = connection.prepareStatement("SELECT * FROM " + tableName);
        sql.executeQuery();

        ResultSet rs = sql.getResultSet();
        ResultSetMetaData meta = rs.getMetaData();

        //prints all table values
        int count = 1;
        while (rs.next()) {
            log.info("");
            log.info("{} #{}", tableName, count);
            for (int i = 0; i < meta.getColumnCount(); i++) {
                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
            }
            count++;
        }
    }

    public void listCustomerRecords(int customerId, String tableName) throws SQLException {
        //checks for user role
        //if customer is not admin/moderator then cannot list other customers' records
        int checkId = checkListingPermission();
        if (checkId != -1 && checkId != customerId) {
            throw new RuntimeException("No permission to list other customer's orders");
        }

        PreparedStatement sql = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE customerId = " + customerId);
        sql.executeQuery();

        ResultSet rs = sql.getResultSet();
        ResultSetMetaData meta = rs.getMetaData();

        //prints all values for the given customer
        int count = 1;
        while (rs.next()) {
            log.info("");
            log.info("{} #{} for customer id {}", tableName, count, customerId);
            for (int i = 0; i < meta.getColumnCount(); i++) {
                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
            }
            count++;
        }
    }

    public void listPendingOrders() throws SQLException {
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list pending orders");
        }

        PreparedStatement sql = connection.prepareStatement("SELECT * FROM Orders WHERE status = 'PENDING'");
        sql.executeQuery();

        ResultSet rs = sql.getResultSet();
        ResultSetMetaData meta = rs.getMetaData();

        //prints all pending orders
        int count = 1;
        while (rs.next()) {
            log.info("");
            log.info("Pending orders #{}", count);
            for (int i = 0; i < meta.getColumnCount(); i++) {
                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
            }
            count++;
        }
    }

    public void listCustomers() throws SQLException {
        //checks for user role
        int checkId = checkListingPermission();
        if (checkId != -1) {
            throw new RuntimeException("No permission to list customers");
        }

        PreparedStatement sql = connection.prepareStatement("SELECT * FROM Customers");
        sql.executeQuery();

        ResultSet rs = sql.getResultSet();
        ResultSetMetaData meta = rs.getMetaData();

        //prints all customers
        int count = 1;
        while (rs.next()) {
            log.info("");
            log.info("Customer #{}", count);
            for (int i = 0; i < meta.getColumnCount(); i++) {
                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
            }
            count++;
        }
    }
}
