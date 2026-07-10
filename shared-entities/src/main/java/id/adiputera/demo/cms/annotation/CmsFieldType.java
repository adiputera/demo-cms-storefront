package id.adiputera.demo.cms.annotation;

/**
 * Supported field types for CMS entity fields.
 * Used to map backend model attributes to frontend controls and validation handlers.
 *
 * @author Yusuf F. Adiputera
 */
public enum CmsFieldType {
    STRING,
    TEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    DATETIME,
    ARRAY_STRING,
    REFERENCE,
    IMAGE,
    FILE
}
