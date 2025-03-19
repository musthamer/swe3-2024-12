package hbv.model;
public class VaccineInventory {
    private int inventoryId;
    private int centerId;
    private String vaccine;
    private int stock;
    public VaccineInventory(int inventoryId, int centerId, String vaccine, int stock) {
        this.inventoryId = inventoryId;
        this.centerId = centerId;
        this.vaccine = vaccine;
        this.stock = stock;
    }
    public int getInventoryId() { return inventoryId; }
    public int getCenterId() { return centerId; }
    public String getVaccine() { return vaccine; }
    public int getStock() { return stock; }
}
