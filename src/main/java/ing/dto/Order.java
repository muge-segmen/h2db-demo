package ing.dto;

import ing.model.OrderSide;
import ing.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {

    int id;
    int customerId;
    String assetName;
    OrderSide orderSide;
    int size;
    Double price;
    Status status;
    LocalDate createDate;

    @Override
    public String toString() {
        return "Order [id=" + id + ", customerId=" + customerId + ", assetName=" + assetName + ", orderSide=" + orderSide + ", size=" + size + ", price=" + price + ", status=" + status + ", createDate=" + createDate + "]";
    }
}
