import javax.swing.JFrame;


/**
 * 最小構成のウィンドウを表示する旧エントリーポイント。
 * 実際の管理機能を利用する場合は {@link Main} を起動する。
 */
public class LightingManagementGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	
	/** タイトルと基本的な終了動作を設定して空のウィンドウを表示する。 */
	public LightingManagementGUI() {
		
		setTitle("照明機材管理システム");
		setSize(800, 600);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLocationRelativeTo(null);
		
		setVisible(true);
	}
	
	/** 旧画面を単独で起動する。 */
	public static void main(String[] args) {
		
		new LightingManagementGUI();
	}

}
