import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
public class Main{
	
	public static void main(String[] args) {
		
		ArrayList<Equipment> equipments = new ArrayList<>();
		
		// ===== CONSOLE =====
		equipments.add(new Equipment("grandMA3 Light", "CONSOLE", 1));
		equipments.add(new Equipment("HOG4", "CONSOLE", 1));
		equipments.add(new Equipment("Road HOG4", "CONSOLE", 1));
		equipments.add(new Equipment("Avolites Pearl Expert", "CONSOLE", 1));
		equipments.add(new Equipment("32ch調光卓 LUCIOL", "CONSOLE", 1));
		equipments.add(new Equipment("24ch調光卓 ETC SmartFade ML", "CONSOLE", 4));
		equipments.add(new Equipment("12ch調光卓 ETC SmartFade1248", "CONSOLE", 4));

		// ===== MOVING LIGHT =====
		equipments.add(new Equipment("ROBE MegaPointe", "MOVING LIGHT", 24));
		equipments.add(new Equipment("ROBE ROBIN Pointe", "MOVING LIGHT", 12));
		equipments.add(new Equipment("Martin MAC Aura XIP", "MOVING LIGHT", 12));
		equipments.add(new Equipment("Martin MAC301 WASH", "MOVING LIGHT", 10));

		// ===== LED SPOT =====
		equipments.add(new Equipment("Silver Star SUPER SOLAR ze/ETZ MK4", "LED SPOT", 20));
		equipments.add(new Equipment("Silver Star MY NOVA", "LED SPOT", 20));
		equipments.add(new Equipment("Silver Star CAM3/EMZ CCS3", "LED SPOT", 20));
		equipments.add(new Equipment("PEGASYS V4", "LED SPOT", 10));
		equipments.add(new Equipment("EK PRO BL2", "LED SPOT", 15));
		equipments.add(new Equipment("EK PRO BL2 IP", "LED SPOT", 15));
		equipments.add(new Equipment("EK Blinder II", "LED SPOT", 10));

		// ===== SPOT =====
		equipments.add(new Equipment("ParLight", "SPOT", 50));
		equipments.add(new Equipment("フレネル/凸レンズスポット", "SPOT", 40));
		equipments.add(new Equipment("Source Four", "SPOT", 60));
		equipments.add(new Equipment("ITO", "SPOT", 30));
		equipments.add(new Equipment("ミニブル", "SPOT", 20));
		equipments.add(new Equipment("LHQ ハロゲン85W 12灯", "SPOT", 10));

		// ===== DIMMER =====
		equipments.add(new Equipment("JANDS FPX Dimmer 36ch", "DIMMER", 5));
		equipments.add(new Equipment("丸茂 Zemtour 12ch", "DIMMER", 5));
		equipments.add(new Equipment("PORTABLE DIMSTAR II", "DIMMER", 5));
		equipments.add(new Equipment("LITEPUTER DX-402A", "DIMMER", 5));

		// ===== PIN SPOT =====
		equipments.add(new Equipment("Xebex SUPERSOL 2Kw", "PIN SPOT", 8));
		equipments.add(new Equipment("Xebex SUPERSOL 1Kw", "PIN SPOT", 8));
		equipments.add(new Equipment("KJ-6 HMI-400W", "PIN SPOT", 6));
		equipments.add(new Equipment("MIP650 ハロゲン650W", "PIN SPOT", 6));

		// ===== FOG / HAZE =====
		equipments.add(new Equipment("Concept MK-Ⅴ/Ⅵ", "FOG MACHINE", 3));
		equipments.add(new Equipment("Look Solutions VIPER-NT", "FOG MACHINE", 3));
		equipments.add(new Equipment("ANTARI HZ500", "HAZE MACHINE", 3));
		equipments.add(new Equipment("ANTARI HZ100", "HAZE MACHINE", 3));

		// ===== OTHER =====
		equipments.add(new Equipment("電源ケーブル各種", "OTHER", 999));
		equipments.add(new Equipment("スタンド各種", "OTHER", 999));
		
		for (Equipment equipment : equipments) {
		}
		
		ArrayList<Project> projects = new ArrayList<>();

		projects.add(new Project("市民文化ホール 大ホール", "常設"));
		projects.add(new Project("市民文化ホール 中ホール", "常設"));
		projects.add(new Project("市民文化ホール 小ホール", "常設"));
		projects.add(new Project("文化センター ホール", "常設"));
		
		// ===== 現場詳細テーブル =====
		String[] detailColumns = {
		        "機材名",
		        "カテゴリ",
		        "数量"
		};

		DefaultTableModel detailModel =
		        new DefaultTableModel(detailColumns, 0);

		JTable detailTable =
		        new JTable(detailModel);

		JScrollPane detailScroll =
		        new JScrollPane(detailTable);

		
		JComboBox<Project> projectComboBox =
		        new JComboBox<>();
		for (Project project : projects) {
		    projectComboBox.addItem(project);
		}

		projectComboBox.addActionListener(e -> {

		    Project selectedProject =
		            (Project) projectComboBox.getSelectedItem();

		    detailModel.setRowCount(0);

		    if (selectedProject == null) {
		        return;
		    }

		    for (RequestItem item : selectedProject.getItems()) {

		        detailModel.addRow(new Object[] {
		                item.getEquipment().getName(),
		                item.getEquipment().getCategory(),
		                item.getQuantity()
		        });
		    }
		});

		for (Project project : projects) {
		    projectComboBox.addItem(project);
		}
		
		JFrame frame = new JFrame();
		
		frame.setTitle("照明機材管理システム");
		
		frame.setSize(900, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//レイアウト
		frame.setLayout(new BorderLayout());
		
		//パネル
		
		JPanel equipmentPanel = new JPanel();
		JPanel projectPanel = new JPanel();
		JPanel dataPanel = new JPanel();
		
		//表示エリア（スクロール付き）
		JTextArea area = new JTextArea(8, 40);
		area.setEditable(false);
		JScrollPane scroll = new JScrollPane(area);
		
		
		String[] equipmentColumns = {
		        "機材名",
		        "カテゴリ",
		        "在庫"
		};

		DefaultTableModel equipmentTableModel =
		        new DefaultTableModel(equipmentColumns, 0);

		JTable equipmentTable =
		        new JTable(equipmentTableModel);

		JScrollPane equipmentTableScroll =
		        new JScrollPane(equipmentTable);

		equipmentTableScroll.setPreferredSize(
		        new Dimension(800, 300));

		for (Equipment equipment : equipments) {
		    equipmentTableModel.addRow(new Object[]{
		            equipment.getName(),
		            equipment.getCategory(),
		            equipment.getStock()
		    });
		}
		
		
		
		//ボタン
		JButton button = new JButton("機材一覧");
		JButton addButton = new JButton("機材追加");
		JButton searchButton = new JButton("機材検索");
		JButton restockButton = new JButton("在庫補充");
		JButton categoryButton = new JButton("カテゴリ検索");
		JButton addProjectButton = new JButton("現場追加");
		JButton projectListButton = new JButton("現場一覧");
		JButton registerButton = new JButton("現場へ機材登録");
		JButton detailButton = new JButton("現場詳細");
		JButton pickingButton = new JButton("ピッキングリスト");
		JButton deleteItemButton = new JButton("現場機材削除");
		JButton brokenButton = new JButton("故障機材登録");
		JButton brokenListButton = new JButton("故障機材一覧");
		JButton repairButton = new JButton("修理完了");
		JButton saveButton = new JButton("保存");
		JButton loadButton = new JButton("読込");
		
		JTextField nameField = new JTextField(10);
		JTextField categoryField = new JTextField(10);
		JTextField stockField = new JTextField(5);
		JTextField projectNameField = new JTextField(10);
		JTextField projectDateField = new JTextField(10);
		JTextField registerQuantityField = new JTextField(5);
		
		
		
		DefaultListModel<Equipment> equipmentModel =
		        new DefaultListModel<>();

		JList<Equipment> equipmentList =
		        new JList<>(equipmentModel);

		JScrollPane equipmentListScroll =
		        new JScrollPane(equipmentList);
		
		for (Equipment equipment : equipments) {
		    equipmentModel.addElement(equipment);
		}
		equipmentList.addMouseListener(new MouseAdapter() {

		    @Override
		    public void mouseClicked(MouseEvent e) {

		        if (e.getClickCount() == 2) {

		            Equipment selectedEquipment =
		                    equipmentList.getSelectedValue();

		            if (selectedEquipment == null) {
		                return;
		            }

		            String quantityText =
		                    JOptionPane.showInputDialog(
		                            selectedEquipment.getName()
		                            + " の数量を入力してください");

		            if (quantityText == null) {
		                return;
		            }

		            int quantity =
		                    Integer.parseInt(quantityText);

		            Project selectedProject =
		                    (Project) projectComboBox.getSelectedItem();

		            if (selectedProject == null) {

		                JOptionPane.showMessageDialog(
		                        frame,
		                        "現場を選択してください");

		                return;
		            }

		            if (quantity > selectedEquipment.getStock()) {

		                JOptionPane.showMessageDialog(
		                        frame,
		                        "在庫不足です");

		                return;
		            }

		            selectedProject.getItems().add(
		                    new RequestItem(
		                            selectedEquipment,
		                            quantity));

		            selectedEquipment.removeStock(quantity);

		            area.setText(
		                    selectedProject.getName()
		                    + " に "
		                    + selectedEquipment.getName()
		                    + " を "
		                    + quantity
		                    + " 台登録しました");
		        }
		    }
		});
		
		// 機材系
		JPanel equipmentButtonPanel = new JPanel();

		equipmentButtonPanel.add(button);
		equipmentButtonPanel.add(searchButton);
		equipmentButtonPanel.add(restockButton);
		equipmentButtonPanel.add(categoryButton);
		
		String[] categories = {
		        "CONSOLE",
		        "MOVING LIGHT",
		        "LED SPOT",
		        "SPOT",
		        "DIMMER",
		        "PIN SPOT",
		        "FOG MACHINE",
		        "OTHER"
		};

		JPanel categoryPanel = new JPanel(
		        new GridLayout(2, 4, 10, 10));

		for (String category : categories) {

		    JTextArea categoryArea = new JTextArea();
		    categoryArea.setEditable(false);

		    for (Equipment equipment : equipments) {

		        if (equipment.getCategory().equals(category)) {

		            categoryArea.append(
		                    equipment.getName()
		                    + " : "
		                    + equipment.getStock()
		                    + "\n");
		        }
		    }

		    JScrollPane pane = new JScrollPane(categoryArea);

		    pane.setBorder(
		            BorderFactory.createTitledBorder(category));

		    categoryPanel.add(pane);
		}

		equipmentPanel.setLayout(new BorderLayout());
		equipmentPanel.add(categoryPanel, BorderLayout.CENTER);
		equipmentPanel.add(equipmentButtonPanel, BorderLayout.SOUTH);
		
		// 現場系
		JPanel projectCreatePanel = new JPanel(new GridLayout(1, 5));

		projectCreatePanel.add(new JLabel("【現場作成】現場名"));
		projectCreatePanel.add(projectNameField);

		projectCreatePanel.add(new JLabel("【現場作成】日付"));
		projectCreatePanel.add(projectDateField);

		projectCreatePanel.add(addProjectButton);
		
		projectCreatePanel.add(new JLabel("選択中の現場"));
		projectCreatePanel.add(projectComboBox);
		
		// 機材登録エリア
		JPanel registerPanel = new JPanel(new BorderLayout());
		JPanel registerFormPanel = new JPanel(new GridLayout(3, 2));

		registerFormPanel.add(new JLabel("現場"));
		registerFormPanel.add(projectComboBox);

		registerFormPanel.add(new JLabel("数量"));
		registerFormPanel.add(registerQuantityField);
		
		registerPanel.setBorder(
			    BorderFactory.createTitledBorder("機材登録")
			);

		registerFormPanel.add(new JLabel(""));
		registerFormPanel.add(registerButton);
		
		equipmentList.setVisibleRowCount(15);
		
		registerPanel.add(equipmentListScroll, BorderLayout.CENTER);
		registerPanel.add(registerFormPanel, BorderLayout.SOUTH);
		
		//その他操作
		JPanel projectActionPanel = new JPanel();
		
		projectPanel.add(detailButton);
		projectPanel.add(pickingButton);
		projectPanel.add(deleteItemButton);
		
		projectPanel.setLayout(new BorderLayout());

		projectActionPanel.add(projectListButton);
		projectActionPanel.add(detailButton);
		projectActionPanel.add(pickingButton);
		projectActionPanel.add(deleteItemButton);

		projectPanel.add(projectCreatePanel, BorderLayout.NORTH);
		projectPanel.add(registerPanel, BorderLayout.CENTER);
		projectPanel.add(projectActionPanel, BorderLayout.SOUTH);
		
		// データ系
		dataPanel.add(brokenButton);
		dataPanel.add(brokenListButton);
		dataPanel.add(repairButton);
		dataPanel.add(saveButton);
		dataPanel.add(loadButton);

		// ===== まとめるパネル（重要）=====
		JTabbedPane tab = new JTabbedPane();

		tab.addTab("機材", equipmentPanel);
		tab.addTab("現場", projectPanel);
		tab.addTab("データ管理", dataPanel);
		// ===== 出力パネル =====
		

		// ===== 画面配置 =====
		frame.add(tab, BorderLayout.CENTER);
		frame.add(scroll, BorderLayout.SOUTH);
		
		addButton.addActionListener(e -> {

		    String name = nameField.getText();
		    String category = categoryField.getText();
		    int stock = Integer.parseInt(stockField.getText());

		    equipments.add(new Equipment(name, category, stock));
		    
		    equipmentTableModel.addRow(new Object[]{
		            name,
		            category,
		            stock
		    });

		    area.setText("追加しました: " + name);

		    nameField.setText("");
		    categoryField.setText("");
		    stockField.setText("");
		});
		
		button.addActionListener(E -> {
			area.setText("");
			for(Equipment equipment : equipments) {
				
				area.append(
						equipment.getName()
						+ "/"
						+ equipment.getCategory()
						+ "/ 在庫:"
						+ equipment.getStock()
						+ "\n");
			}
		});
		
		searchButton.addActionListener(e -> {

			String keyword =
					JOptionPane.showInputDialog("検索する機材名");

			area.setText("");

			boolean found = false;

			for(Equipment equipment : equipments) {

				if(equipment.getName().contains(keyword)) {

					area.append(
							equipment.getName()
							+ "/"
							+ equipment.getCategory()
							+ "/ 在庫:"
							+ equipment.getStock()
							+ "\n");

					found = true;
				}
			}

			if(!found) {
				area.setText("該当する機材がありません");
			}
		});
		
		restockButton.addActionListener(e -> {

		    String name =
		            JOptionPane.showInputDialog("機材名");

		    String addText =
		            JOptionPane.showInputDialog("補充数");

		    int addStock =
		            Integer.parseInt(addText);

		    boolean found = false;

		    for(Equipment equipment : equipments){

		        if(equipment.getName().equals(name)){

		            equipment.addStock (addStock);

		            area.setText(
		                    equipment.getName()
		                    + " の在庫を "
		                    + addStock
		                    + " 追加しました\n現在庫:"
		                    + equipment.getStock());

		            found = true;
		        }
		    }

		    if(!found){
		        area.setText("機材が見つかりません");
		    }
		});
		
		categoryButton.addActionListener(e -> {

		    String category =
		            JOptionPane.showInputDialog("カテゴリ名");

		    area.setText("");

		    boolean found = false;

		    for(Equipment equipment : equipments){

		        if(equipment.getCategory().equals(category)){

		            area.append(
		                    equipment.getName()
		                    + " / 在庫:"
		                    + equipment.getStock()
		                    + "\n");

		            found = true;
		        }
		    }

		    if(!found){
		        area.setText("該当する機材がありません");
		    }
		});
		
		addProjectButton.addActionListener(e -> {

		    String name = projectNameField.getText();
		    String date = projectDateField.getText();

		    Project project = new Project(name, date);

		    projects.add(project);

		    projectComboBox.addItem(project);
		    
		    projectComboBox.addItem(
		            projects.get(projects.size() - 1));

		    area.setText("現場作成: " + name);

		    projectNameField.setText("");
		    projectDateField.setText("");
		});
		
		projectListButton.addActionListener(e -> {

		    area.setText("");

		    for(Project project : projects){

		        area.append(
		                project.name
		                + " ("
		                + project.date
		                + ")\n");
		    }
		});
		
		registerButton.addActionListener(e -> {

			Project selectedProject =
			        (Project) projectComboBox.getSelectedItem();

		    Equipment selectedEquipment =
		            equipmentList.getSelectedValue();

		    int quantity =
		            Integer.parseInt(registerQuantityField.getText());

		    

		    if (selectedProject == null) {
		        area.setText("現場が見つかりません");
		        return;
		    }

		    if (selectedEquipment == null) {
		        area.setText("機材を選択してください");
		        return;
		    }

		    if (quantity > selectedEquipment.getStock()) {
		        area.setText("在庫不足です");
		        return;
		    }

		    selectedProject.items.add(
		            new RequestItem(selectedEquipment, quantity));

		    selectedEquipment.removeStock(quantity);

		    area.setText(
		            selectedProject.name + " に "
		            + selectedEquipment.getName() + " を "
		            + quantity + "台登録しました");
		    
		    detailModel.setRowCount(0);

		    for (RequestItem item : selectedProject.getItems()) {

		        detailModel.addRow(new Object[] {
		                item.getEquipment().getName(),
		                item.getEquipment().getCategory(),
		                item.getQuantity()
		        });
		    }

		    registerQuantityField.setText("");

		    equipmentList.clearSelection();
		});
		
		
		
		
		pickingButton.addActionListener(e -> {

		    area.setText("");

		    for(Project project : projects){

		        area.append(
		                "現場:"
		                + project.name
		                + "\n");

		        if(project.items.size() == 0){

		            area.append(
		                    "機材未登録\n\n");

		        }else{

		            for(RequestItem item : project.items){

		                area.append(
		                        item.equipment.getName()
		                        + " / "
		                        + item.quantity
		                        + "台\n");
		            }

		            area.append("\n");
		        }
		    }
		});
		
		deleteItemButton.addActionListener(e -> {

		    String projectName =
		            JOptionPane.showInputDialog(
		                    "現場名");

		    Project selectedProject = null;

		    for(Project project : projects){

		        if(project.name.equals(projectName)){
		            selectedProject = project;
		        }
		    }

		    if(selectedProject == null){
		        area.setText("現場が見つかりません");
		        return;
		    }

		    String equipmentName =
		            JOptionPane.showInputDialog(
		                    "削除する機材名");

		    RequestItem targetItem = null;

		    for(RequestItem item : selectedProject.items){

		        if(item.equipment.getName().equals(equipmentName)){
		            targetItem = item;
		        }
		    }

		    if(targetItem == null){
		        area.setText("機材が見つかりません");
		        return;
		    }

		    targetItem.equipment.addStock(targetItem.quantity);

		    selectedProject.items.remove(targetItem);

		    area.setText(
		            targetItem.equipment.getName()
		            + " を削除しました");
		});
		
		brokenButton.addActionListener(e -> {

		    String equipmentName =
		            JOptionPane.showInputDialog(
		                    "故障した機材名");

		    Equipment selectedEquipment = null;

		    for(Equipment equipment : equipments){

		        if(equipment.getName().equals(equipmentName)){
		            selectedEquipment = equipment;
		        }
		    }

		    if(selectedEquipment == null){
		        area.setText("機材が見つかりません");
		        return;
		    }

		    String brokenText =
		            JOptionPane.showInputDialog(
		                    "故障数");

		    int brokenCount =
		            Integer.parseInt(brokenText);

		    if(brokenCount > selectedEquipment.getStock()){

		        area.setText("在庫以上は登録できません");
		        return;
		    }

		    selectedEquipment.removeStock(brokenCount);
		    selectedEquipment.addBrokenStock(brokenCount);

		    area.setText(
		            selectedEquipment.getName()
		            + " を "
		            + brokenCount
		            + "台故障登録しました");
		});
		
		brokenListButton.addActionListener(e -> {

		    area.setText("");

		    boolean found = false;

		    for(Equipment equipment : equipments){

		        if(equipment.getBrokenStock() > 0){

		            area.append(
		                    equipment.getName()
		                    + " / 故障:"
		                    + equipment.getBrokenStock()
		                    + "台\n");

		            found = true;
		        }
		    }

		    if(!found){
		        area.setText("故障機材はありません");
		    }
		});
		
		repairButton.addActionListener(e -> {

		    String equipmentName =
		            JOptionPane.showInputDialog(
		                    "修理した機材名");

		    Equipment selectedEquipment = null;

		    for(Equipment equipment : equipments){

		        if(equipment.getName().equals(equipmentName)){
		            selectedEquipment = equipment;
		        }
		    }

		    if(selectedEquipment == null){

		        area.setText("機材が見つかりません");
		        return;
		    }

		    String repairText =
		            JOptionPane.showInputDialog(
		                    "修理台数");

		    int repairCount =
		            Integer.parseInt(repairText);

		    if(repairCount >
		            selectedEquipment.getBrokenStock()){

		        area.setText("故障数を超えています");
		        return;
		    }

		    selectedEquipment.removeBrokenStock(repairCount);
		    selectedEquipment.addStock(repairCount);

		    area.setText(
		            selectedEquipment.getName()
		            + " を "
		            + repairCount
		            + "台修理しました\n"
		            + "現在在庫:"
		            + selectedEquipment.getStock()
		            + "\n故障在庫:"
		            + selectedEquipment.getBrokenStock());
		});
		
		saveButton.addActionListener(e -> {

		    try{
		    		//機材保存
		        PrintWriter pw =
		                new PrintWriter(
		                        new File(
		                                "equipments.txt"));

		        for(Equipment equipment : equipments){

		            pw.println(
		                    equipment.getName() + ","
		                    + equipment.getCategory() + ","
		                    + equipment.getStock() + ","
		                    + equipment.getBrokenStock());
		        }

		        pw.close();
		        //現場保存
		        PrintWriter projectWriter =
		                new PrintWriter(
		                        new File("projects.txt"));

		        for(Project project : projects){

		            projectWriter.println(
		                    project.name + ","
		                    + project.date);
		        }

		        projectWriter.close();
		        
		        PrintWriter itemWriter =
		                new PrintWriter(
		                        new File("requestItems.txt"));

		        for(Project project : projects){

		            for(RequestItem item : project.items){

		                itemWriter.println(
		                        project.name + ","
		                        + item.equipment.getName() + ","
		                        + item.quantity);
		            }
		        }

		        itemWriter.close();
		        area.setText(
		                "保存完了しました");

		    }catch(FileNotFoundException ex){

		        area.setText(
		                "保存エラー");
		    }
		});
		
		loadButton.addActionListener(e -> {

		    try{

		        Scanner fileScanner =
		                new Scanner(
		                        new File(
		                                "equipments.txt"));

		        equipments.clear();

		        while(fileScanner.hasNextLine()){

		            String line =
		                    fileScanner.nextLine();

		            String[] data =
		                    line.split(",");

		            Equipment equipment =
		                    new Equipment(
		                            data[0],
		                            data[1],
		                            Integer.parseInt(
		                                    data[2]));

		            equipment.setBrokenStock(
		                    Integer.parseInt(data[3]));

		            equipments.add(
		                    equipment);
		        }

		        fileScanner.close();

		        
		        Scanner projectScanner =
		                new Scanner(
		                        new File("projects.txt"));

		        projects.clear();

		        while(projectScanner.hasNextLine()){

		            String line =
		                    projectScanner.nextLine();

		            String[] data =
		                    line.split(",");

		            projects.add(
		                    new Project(
		                            data[0],
		                            data[1]));
		        }

		        projectScanner.close();
		        
		        Scanner itemScanner =
		                new Scanner(
		                        new File(
		                                "requestItems.txt"));

		        while(itemScanner.hasNextLine()){

		            String line =
		                    itemScanner.nextLine();

		            String[] data =
		                    line.split(",");

		            String projectName =
		                    data[0];

		            String equipmentName =
		                    data[1];

		            int quantity =
		                    Integer.parseInt(
		                            data[2]);

		            Project targetProject = null;

		            for(Project project : projects){

		                if(project.name.equals(
		                        projectName)){

		                    targetProject =
		                            project;

		                    break;
		                }
		            }

		            Equipment targetEquipment = null;

		            for(Equipment equipment : equipments){

		                if(equipment.getName().equals(
		                        equipmentName)){

		                    targetEquipment =
		                            equipment;

		                    break;
		                }
		            }

		            if(targetProject != null
		                    && targetEquipment != null){

		                targetProject.items.add(
		                        new RequestItem(
		                                targetEquipment,
		                                quantity));
		            }
		        }

		        itemScanner.close();
		        
		        area.setText("読込完了");
		        
		    }catch(Exception ex){

		        area.setText(
		                "読込失敗");
		    }
		});
		
		detailButton.addActionListener(e -> {

		    String projectName =
		            JOptionPane.showInputDialog("現場名");

		    Project selectedProject = null;

		    for (Project project : projects) {

		        if (project.getName().equals(projectName)) {
		            selectedProject = project;
		            break;
		        }
		    }

		    if (selectedProject == null) {
		        area.setText("現場が見つかりません");
		        return;
		    }

		    

		    for (RequestItem item : selectedProject.getItems()) {

		        
		    }

		    area.setText(
		            "現場名: " + selectedProject.getName()
		            + "\n日付: " + selectedProject.getDate());
		});
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}