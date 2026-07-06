package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Component Schema class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@AllArgsConstructor
public class ComponentSchema {
    private String type;
    private String displayName;
    private List<ComponentField> fields;
}
