package ing;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws SQLException {
        Server server = Server.createTcpServer("-ifNotExists").start();
        SpringApplication.run(Application.class, args);
    }
}
