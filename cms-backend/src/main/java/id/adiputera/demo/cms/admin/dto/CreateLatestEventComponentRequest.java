package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create Latest Event Component Request class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateLatestEventComponentRequest extends CreateComponentRequest {
    private String title;
    private List<String> eventIds;
}
