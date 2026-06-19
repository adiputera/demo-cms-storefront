package com.demo.cms.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComponentSchema {
    private String type;
    private String displayName;
    private List<ComponentField> fields;
}
