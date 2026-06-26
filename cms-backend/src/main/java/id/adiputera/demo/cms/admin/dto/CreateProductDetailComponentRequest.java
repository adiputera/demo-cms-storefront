package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateProductDetailComponentRequest extends CreateComponentRequest {
    private String title;
    private Boolean showPrice;
    private Boolean showDescription;
}
