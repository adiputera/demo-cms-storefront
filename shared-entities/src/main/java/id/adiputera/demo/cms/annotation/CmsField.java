package id.adiputera.demo.cms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import id.adiputera.demo.cms.converter.CmsFormatter;
import id.adiputera.demo.cms.converter.CmsValueConverter;
import id.adiputera.demo.cms.converter.DefaultFormatter;
import id.adiputera.demo.cms.converter.DefaultValueConverter;
import id.adiputera.demo.cms.entity.ItemModel;

/**
 * Annotation to define schema metadata for a field in a CMS Component.
 *
 * @author Yusuf F. Adiputera
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmsField {

    /**
     * Gets the display name of the field.
     *
     * @return The display name.
     */
    String displayName();

    /**
     * Gets the data type of the field.
     *
     * @return The field type.
     */
    CmsFieldType type() default CmsFieldType.STRING;

    /**
     * Indicates if the field is required.
     *
     * @return True if required.
     */
    boolean required() default false;

    /**
     * Indicates if the field is editable during updates.
     *
     * @return True if editable on update.
     */
    boolean editableOnUpdate() default true;

    /**
     * Gets the placeholder text for form fields.
     *
     * @return The placeholder text.
     */
    String placeholder() default "";

    /**
     * Indicates if the field is searchable.
     *
     * @return True if searchable.
     */
    boolean searchable() default false;

    /**
     * Indicates if the field should be shown as a table column in the grid.
     *
     * @return True if shown as column.
     */
    boolean showAsColumn() default true;

    /**
     * Gets the value converter class for this field.
     *
     * @return The converter class.
     */
    Class<? extends CmsValueConverter> converter() default DefaultValueConverter.class;

    /**
     * Gets the value formatter class for this field.
     *
     * @return The formatter class.
     */
    Class<? extends CmsFormatter> formatter() default DefaultFormatter.class;

    /**
     * Gets the display order of the field in forms and tables.
     *
     * @return The display order.
     */
    int order() default 1;

    /**
     * Gets the referenced entity class if this is a REFERENCE field.
     *
     * @return The referenced class.
     */
    Class<? extends ItemModel> targetEntity() default ItemModel.class;

    /**
     * Gets the cardinality if this is a REFERENCE field.
     *
     * @return The cardinality.
     */
    ReferenceCardinality cardinality() default ReferenceCardinality.SINGLE;
}

