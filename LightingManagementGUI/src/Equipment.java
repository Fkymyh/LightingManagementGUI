public class Equipment {
	
	private String name;
	private String category;
	private int stock;
	private int brokenStock;
	
	public Equipment(
			String name,
			String category,
			int stock) {
		
		this.name = name;
		this.category = category;
		this.stock = stock;
		this.brokenStock = 0;
	}
	public void addStock(int count) {
	    stock += count;
	}
	
	public void removeStock(int count) {
	    if(stock - count < 0) return;
	    stock -= count;
	}
	
	public void addBrokenStock(int count) {
	    brokenStock += count;
	}

	public void removeBrokenStock(int count) {
	    brokenStock -= count;
	}
	
	public void setBrokenStock(int brokenStock){
	    this.brokenStock = brokenStock;
	}
	
	
	public String getName() {
		return name;
	}
	public String getCategory() {
		return category;
	}
	public int getStock() {
		return stock;
	}
	public int getBrokenStock() {
		return brokenStock;
	}
	@Override
	public String toString() {
	    return name + " (在庫:" + stock + ")";
	}

}
