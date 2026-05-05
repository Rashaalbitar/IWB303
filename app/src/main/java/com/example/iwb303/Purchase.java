package com.example.iwb303;

public class Purchase {
    private int id;
    private String itemName;
    private int categoryId;
    private String categoryName; // For display
    private double price;
    private int quantity;
    private double totalCost;
    private String date;

    // Constructor for fetching from DB
    public Purchase(int id, String itemName, int categoryId, String categoryName, double price, int quantity, double totalCost, String date) {
        this.id = id;
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.date = date;
    }

    // Constructor for adding new items
    public Purchase(String itemName, int categoryId, double price, int quantity, String date) {
        this.itemName = itemName;
        this.categoryId = categoryId;
        this.price = price;
        this.quantity = quantity;
        this.totalCost = price * quantity;
        this.date = date;
    }

    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotalCost() { return totalCost; }
    public String getDate() { return date; }
}