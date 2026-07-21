/** 照明機材1種類の名称、カテゴリ、使用可能在庫、故障在庫を保持する。 */
public class Equipment {
	
	// 機材を識別・分類する基本情報。名称は保存データの照合にも使用する。
	private String name;
	private String category;
	// stockは使用可能数、brokenStockは修理待ちの数を表す。
	private int stock;
	private int brokenStock;
	
	/** 新しい機材を作成する。初期状態では故障在庫は0台。 */
	public Equipment(
			String name,
			String category,
			int stock) {
		
		this.name = name;
		this.category = category;
		this.stock = stock;
		this.brokenStock = 0;
	}
	/** 使用可能在庫を増減する。在庫が負になる変更は無視する。 */
	public void addStock(int count) {
	    if (stock + count >= 0) stock += count;
	}
	
	/** 在庫が不足しない場合に限り、使用可能在庫を指定数だけ減らす。 */
	public void removeStock(int count) {
	    if (count <= 0 || stock - count < 0) return;
	    stock -= count;
	}
	
	/** 故障在庫を指定数だけ増やす。 */
	public void addBrokenStock(int count) {
	    if (count > 0) brokenStock += count;
	}

	/** 故障在庫が不足しない場合に限り、指定数だけ減らす。 */
	public void removeBrokenStock(int count) {
	    if (count <= 0 || brokenStock - count < 0) return;
	    brokenStock -= count;
	}
	
	/** 保存データから故障在庫を復元する。負の値は0として扱う。 */
	public void setBrokenStock(int brokenStock){
	    this.brokenStock = Math.max(0, brokenStock);
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
	/** リスト表示では機材名と現在の使用可能在庫を示す。 */
	@Override
	public String toString() {
	    return name + " (在庫:" + stock + ")";
	}

}
