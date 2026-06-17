public class RequestItem {

	public Equipment getEquipment() { return equipment; }
	public int getQuantity() { return quantity; }
	
    Equipment equipment;
    int quantity;

    public RequestItem(
            Equipment equipment,
            int quantity){

        this.equipment = equipment;
        this.quantity = quantity;
    }
}