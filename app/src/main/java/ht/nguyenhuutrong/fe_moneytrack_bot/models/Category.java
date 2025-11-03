package ht.nguyenhuutrong.fe_moneytrack_bot.models;

public class Category {

    private int id;
    private String name;
    private String type; // 'income' hoặc 'expense'

    // --- Constructors ---
    public Category() {
    }

    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // --- Getters & Setters ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // --- Optional: để hiển thị dễ hơn khi debug hoặc trong Spinner ---
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
