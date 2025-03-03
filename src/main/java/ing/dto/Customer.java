package ing.dto;

import ing.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {

    int customerId;
    String customerName;
    String password;
    boolean isAdmin;
    Role role;

    @Override
    public String toString() {
        return "Customer [id=" + customerId + ", name=" + customerName + ", password=" + password + ", isAdmin=" + isAdmin + ", role=" + role + "]";
    }
}
