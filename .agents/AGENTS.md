# Project Agent Rules

## Java Base Package Convention

When generating any Java code for this project, the base package **must always start with `id.adiputera`**.

- **Base group ID / base package**: `id.adiputera`
- **Example for this project**: `id.adiputera.demo.cms`
- Never use `com.demo`, `com.example`, or any other prefix — always use `id.adiputera` as the root.

## JavaDoc Convention

When generating or modifying any Java class, interface, enum, or annotation in this project, you **must always include standard JavaDoc headers** immediately preceding the class declaration (above any class-level annotations).
- The JavaDoc must include a brief description of the class/interface/enum/annotation.
- It must include the static author tag: `@author Yusuf F. Adiputera`

Example:
```java
/**
 * Standard API response wrapper for dynamic rules endpoints.
 *
 * @author Yusuf F. Adiputera
 */
```

## Java Method JavaDoc Convention

When generating or modifying **any method** (including those inside records, `@JsonPOJOBuilder`, or `@Data`-annotated classes) in this project, you **must always include a JavaDoc header** immediately preceding the method declaration.

The JavaDoc for methods must include:
- A brief description of what the method does.
- The `@return` tag describing the return value.
- The `@throws` tag if the method declares or might throw checked exceptions.
- If the method has `@Override` or overrides a supertype method, you **must still include the JavaDoc** with `@see` linking to the supertype method.
- **Do not** duplicate the `@author` tag in method JavaDocs (use only on the class).

Example (for regular method):
```java
/**
 * Calculates the total price including tax.
 *
 * @param price The base price.
 * @param taxRate The tax rate percentage.
 * @return The total price after tax.
 */
public BigDecimal calculateTotal(BigDecimal price, BigDecimal taxRate) {
    return price.multiply(BigDecimal.ONE.add(taxRate));
}
```

Example (for record builder or overridden method):
```java
@JsonPOJOBuilder(withPrefix = "")
static class Builder {
    private String name;
    private int age;

    /**
     * Sets the name field.
     *
     * @param name The name to set.
     * @return The current builder instance.
     */
    public Builder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the age field.
     *
     * @return The age value.
     */
    public int getAge() {
        return age;
    }

    // ... other methods
}
```
