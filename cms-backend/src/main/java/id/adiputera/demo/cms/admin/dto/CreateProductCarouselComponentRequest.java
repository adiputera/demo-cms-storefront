package id.adiputera.demo.cms.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateProductCarouselComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> productCodes;
}
