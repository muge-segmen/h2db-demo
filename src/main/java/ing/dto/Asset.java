package ing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Asset {

    int id;
    int customerId;
    String assetName;
    int size;

    @Override
    public String toString() {
        return "Asset [id=" + id + ", customerId=" + customerId + ", assetName=" + assetName + ", size=" + size + "]";
    }
}

