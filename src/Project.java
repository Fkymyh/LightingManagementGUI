import java.util.ArrayList;

/** 現場の基本情報と、その現場へ割り当てた機材明細を保持する。 */
public class Project {
	// 画面側から安全に参照するためのアクセサ。
	public String getName() { return name; }
	public String getDate() { return date; }
	public ArrayList<RequestItem> getItems() { return items; }
	
    // 現場名は保存データ上の識別子としても使われる。
    String name;
    String date;

    // 現場へ登録された機材と数量の一覧。
    ArrayList<RequestItem> items =
            new ArrayList<>();

    /** 機材がまだ登録されていない現場を作成する。 */
    public Project(
            String name,
            String date){

        this.name = name;
        this.date = date;
        }
    /** コンボボックスには現場名だけを表示する。 */
    @Override
    public String toString() {
        return name;
    }
}
