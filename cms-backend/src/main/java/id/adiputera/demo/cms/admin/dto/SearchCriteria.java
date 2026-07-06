package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search Criteria class.
 *
 * @author Yusuf F. Adiputera
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String field;
    private SearchOperator operator;
    private String value;
}
