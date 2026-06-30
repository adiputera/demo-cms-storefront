package id.adiputera.demo.cms.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateLatestEventComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> eventIds;
}
