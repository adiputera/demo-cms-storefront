package com.demo.cms.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComponentField {
    private String name;
    private String displayName;
    private String type; // "string", "text", "boolean", "array_string"
    private boolean required;
    private String placeholder;
}
