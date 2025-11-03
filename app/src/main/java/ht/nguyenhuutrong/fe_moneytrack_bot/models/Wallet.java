package ht.nguyenhuutrong.fe_moneytrack_bot.models;

public class Wallet {
    private int id;
    private String name;
    private double balance; // Số dư

    // --- Constructor ---
    public Wallet() {
        // Mặc định
    }

    public Wallet(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }

    // --- Setters (tùy chọn) ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBalance(double balance) { this.balance = balance; }

    // --- toString() (dễ debug) ---
    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
