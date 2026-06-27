import javax.swing.JFrame;


public class LightingManagementGUI extends JFrame {
	
	public LightingManagementGUI() {
		
		setTitle("照明機材管理システム");
		setSize(800, 600);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLocationRelativeTo(null);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		
		new LightingManagementGUI();
	}

}
