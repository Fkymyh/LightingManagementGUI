/** 1つの現場へ割り当てられた機材と数量の組を表す。 */
public class RequestItem {

	// 表示・保存処理から明細内容を参照するためのアクセサ。
	public Equipment getEquipment() { return equipment; }
	public int getQuantity() { return quantity; }
	
    // 割り当て対象の機材と、その使用台数。
    Equipment equipment;
    int quantity;

    /** 指定した機材の持ち出し明細を作成する。 */
    public RequestItem(
            Equipment equipment,
            int quantity){

        this.equipment = equipment;
        this.quantity = quantity;
    }
}
