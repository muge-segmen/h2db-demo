This is a backend api for a brokage firm so that their employees can send, list and delete stock orders for their customers.

Endpoints:

POST /initializeDB: additional endpoint to create a new DB, create tables that are needed for the project, and also create and grant table rights to user roles. this endpoint is used to initialize and set up desired DB and MUST be called at the beginning of the project to provide the requirements.

POST /login: connects to the db with the given credentials. inputs: String customerName, String password. As a response db connection established. All the following endpoints required a db connection, so this method should be called at the very beginning.

POST /logout: disconnects from the db. after termination of connection, user cannot perform the db actions.

POST /add-user: additional endpoint to create a new user/customer to db and also to Customers table. only users with admin rights can perform this action. inputs: String customerName, String password, int customerId, boolean isAdmin, Role role

DELETE /delete-user: additional enpoint to remove an existing user/customer from db and also from Customers table. only users with admin rights can perform this action. inputs: String customerName.

POST /create-table: additional endpoint to create Orders, Assets, Customers tables. only users with admin rights can perform this action. inputs: String tableName.

POST /clear-table: additional endpoint to clear all the data from given table in db. only users with admin rights can perform this action. inputs: String tableName.

DELETE /delete-table: additional endpoint to remove given table from db. only users with admin rights can perform this action. inputs: String tableName.

GET /list-all-entities: endpoint to list all entities in a given table (Orders, Assets, Customers in our case). only users with admin rights or with admin/mod roles can perform this action. public users have no permission to list entities. inputs: String tableName.

GET /list-customer-records: endpoint to list all records of a customer in a given table. users with admin rights or with admin/mod roles can list the records of any customer, but public users can only get their own records and have no permission to get other users' records. inputs: int customerId, String tableName.

GET /list-pending-orders: additional endpoint to list all pending orders in Orders table. only users with admin rights or with admin/mod roles can perform this action. public users have no permission to get pending orders list.

GET /list-customers: additional endpoint to list customers in Customers table. only users with admin rights or with admin/mod roles can perform this action. public users have no permission to get customers list.

POST /insert: endpoint to create order. inserts a new order to Orders table. while users with admin rights or with admin/mod roles can create an order for any customer, public users can only create an order for themselves. all orders are created with "PENDING" status and creation date is set automatically by the local date. inputs: int customerId, String assetName, OrderSide orderSide, int size, Double price, Status status, LocalDate createDate

DELETE /delete: endpoint to delete order. removes an order with the given id from Orders table. while users with admin rights or with admin/mod roles can delete an order for any customer, public users can only delete an order that is created by themselves.

PUT /update: endpoint to update an order's status. while users with admin rights or with admin/mod roles can update an order status for any customer as any status, public users can only update an order that is created by themselves. also, public users cannot approve/match their own orders (they cannot update an order status as "MATCHED"). before matching "SELL" orders (setting the status to "MATCHED"), checks whether the customer has enough order size to sell. if not, order status is updated as "CANCELLED" authomatically. if update order action is performed successfully then Assets table is updated accordingly. As a result of updating an order, a new record can be added to Assets table, an existing record can be deleted from Assets table, or size of an existing record can be updated in Assets table. inputs: int id, Status status.


Enum classes:

Status: PENDING, MATCHED, CANCELLED
OrderSide: BUY, SELL
Role: ADMIN, MOD, PUBLIC


Notes:
* initializeDB MUST be called at the very beginning of the tests. Otherwise, tables and rights will be missing and methods will fail.
* additional endpoints are not within the scope of the task but added for creating and setting the database for the project and test purposes.
* for authorization, instead of only checking whether user is admin, user roles are used. three different roles are defined and grant different permissions for each role. (to set up these roles and grant permissions to each roles, initializeDB method can be called before testing the other methods).
* as there is no detailed information about what is usableSize and how to use it for the orders, size value is used instead usableSize.
* h2db demo postman collection is added under the resources directory for sample requests of each endpoint.
