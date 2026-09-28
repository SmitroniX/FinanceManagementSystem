package com.finvantage.model;

/**
 * Category represents classification for income and expenses.
 * Demonstrates OOP Encapsulation and Enum type safety.
 */
public class Category extends BaseEntity {

    public enum Type {
        INCOME, EXPENSE
    }

    private Long userId;
    private String name;
    private Type type = Type.EXPENSE;
    private String colorHex = "#4A90E2";
    private String iconName = "tag";

    public Category() {
        super();
    }

    public Category(Long id, Long userId, String name, Type type, String colorHex, String iconName) {
        super(id);
        this.userId = userId;
        this.name = name;
        this.type = type != null ? type : Type.EXPENSE;
        if (colorHex != null) this.colorHex = colorHex;
        if (iconName != null) this.iconName = iconName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type != null ? type : Type.EXPENSE;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
