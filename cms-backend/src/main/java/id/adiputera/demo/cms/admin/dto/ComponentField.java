package id.adiputera.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Component Field class.
 *
 * @author Yusuf F. Adiputera
 */
@Getter
@AllArgsConstructor
public class ComponentField {
    private String name;
    private String displayName;
    private String type; // "string", "text", "boolean", "array_string", "reference"
    private boolean required;
    private String placeholder;
    private String referenceTarget;
    private String referenceCardinality;
}
