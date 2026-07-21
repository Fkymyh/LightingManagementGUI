import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * 照明機材、現場への持ち出し、故障在庫を管理するSwingアプリケーション。
 *
 * <p>画面の組み立てとイベント処理はこのクラスが担当し、機材・現場・
 * 持ち出し明細のデータはそれぞれのモデルクラスで保持する。</p>
 */
public class Main {

    // ===============================
    // 定数定義
    // ===============================

    // ▼ カテゴリ一覧
    public static final String CATEGORY_CONSOLE = "CONSOLE";
    public static final String CATEGORY_MOVING_LIGHT = "MOVING LIGHT";
    public static final String CATEGORY_LED_SPOT = "LED SPOT";
    public static final String CATEGORY_SPOT = "SPOT";
    public static final String CATEGORY_DIMMER = "DIMMER";
    public static final String CATEGORY_PIN_SPOT = "PIN SPOT";
    public static final String CATEGORY_FOG_MACHINE = "FOG MACHINE";
    public static final String CATEGORY_HAZE_MACHINE = "HAZE MACHINE";
    public static final String CATEGORY_OTHER = "OTHER";

    // ▼ カテゴリ配列（UI用）
    public static final String[] CATEGORIES = {
            CATEGORY_CONSOLE,
            CATEGORY_MOVING_LIGHT,
            CATEGORY_LED_SPOT,
            CATEGORY_SPOT,
            CATEGORY_DIMMER,
            CATEGORY_PIN_SPOT,
            CATEGORY_FOG_MACHINE,
            CATEGORY_OTHER
    };

    // ▼ 保存ファイル名
    public static final String FILE_EQUIPMENTS = "equipments.txt";
    public static final String FILE_PROJECTS = "projects.txt";
    public static final String FILE_REQUEST_ITEMS = "requestItems.txt";

    // ▼ UI 表示メッセージ
    public static final String MSG_NOT_FOUND_EQUIPMENT = "機材が見つかりません";
    public static final String MSG_NOT_FOUND_PROJECT = "現場が見つかりません";
    public static final String MSG_STOCK_LACK = "在庫不足です";
    public static final String MSG_LOAD_SUCCESS = "読込完了";
    public static final String MSG_LOAD_FAIL = "読込失敗";
    public static final String MSG_SAVE_SUCCESS = "保存完了しました";
    public static final String MSG_SAVE_FAIL = "保存エラー";

    // ===============================
    // 共通メソッド
    // ===============================

    /** 名前が完全一致する機材を返す。見つからない場合はnullを返す。 */
    private static Equipment findEquipmentByName(String name, ArrayList<Equipment> equipments) {
        for (Equipment e : equipments) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    /** 名前が完全一致する現場を返す。見つからない場合はnullを返す。 */
    private static Project findProjectByName(String name, ArrayList<Project> projects) {
        for (Project p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /** 在庫を増減する。在庫が負になる変更は行わずfalseを返す。 */
    private static boolean updateEquipmentStock(Equipment equipment, int change) {
        int newStock = equipment.getStock() + change;
        if (newStock < 0) {
            return false;
        }
        equipment.addStock(change);
        return true;
    }

    /**
     * 入力文字列を正の整数へ変換する。
     * キャンセル、空欄、0以下、整数以外の場合はメッセージを表示してnullを返す。
     */
    private static Integer parsePositiveInt(JFrame owner, String text, String itemName) {
        if (text == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value <= 0) {
                JOptionPane.showMessageDialog(owner, itemName + "は1以上で入力してください");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(owner, itemName + "は整数で入力してください");
            return null;
        }
    }

    /** 機材データの変更後に、機材タブと現場登録タブの一覧を同じ内容へ更新する。 */
    private static void refreshEquipmentViews(
            ArrayList<Equipment> equipments,
            DefaultTableModel tableModel,
            DefaultListModel<Equipment> listModel,
            ArrayList<JTextArea> categoryAreas,
            ArrayList<DefaultListModel<Equipment>> categoryModels) {
        tableModel.setRowCount(0);
        listModel.clear();
        for (JTextArea categoryArea : categoryAreas) {
            categoryArea.setText("");
        }
        for (DefaultListModel<Equipment> categoryModel : categoryModels) {
            categoryModel.clear();
        }

        for (Equipment equipment : equipments) {
            tableModel.addRow(new Object[]{
                    equipment.getName(), equipment.getCategory(), equipment.getStock()
            });
            listModel.addElement(equipment);
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(equipment.getCategory())) {
                    categoryAreas.get(i).append(
                            equipment.getName() + " : " + equipment.getStock() + "\n");
                    categoryModels.get(i).addElement(equipment);
                    break;
                }
            }
        }
    }

    /** Swingのイベントディスパッチスレッド上で画面を起動する。 */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowGui);
    }

    /** 初期データを用意し、メイン画面と各イベント処理を構築する。 */
    private static void createAndShowGui() {
        ArrayList<Equipment> equipments = new ArrayList<>();

        // ===== CONSOLE =====
        equipments.add(new Equipment("grandMA3 Light", CATEGORY_CONSOLE, 1));
        equipments.add(new Equipment("HOG4", CATEGORY_CONSOLE, 1));
        equipments.add(new Equipment("Road HOG4", CATEGORY_CONSOLE, 1));
        equipments.add(new Equipment("Avolites Pearl Expert", CATEGORY_CONSOLE, 1));
        equipments.add(new Equipment("32ch調光卓 LUCIOL", CATEGORY_CONSOLE, 1));
        equipments.add(new Equipment("24ch調光卓 ETC SmartFade ML", CATEGORY_CONSOLE, 4));
        equipments.add(new Equipment("12ch調光卓 ETC SmartFade1248", CATEGORY_CONSOLE, 4));

        // ===== MOVING LIGHT =====
        equipments.add(new Equipment("ROBE MegaPointe", CATEGORY_MOVING_LIGHT, 24));
        equipments.add(new Equipment("ROBE ROBIN Pointe", CATEGORY_MOVING_LIGHT, 12));
        equipments.add(new Equipment("Martin MAC Aura XIP", CATEGORY_MOVING_LIGHT, 12));
        equipments.add(new Equipment("Martin MAC301 WASH", CATEGORY_MOVING_LIGHT, 10));

        // ===== LED SPOT =====
        equipments.add(new Equipment("Silver Star SUPER SOLAR ze/ETZ MK4", CATEGORY_LED_SPOT, 20));
        equipments.add(new Equipment("Silver Star MY NOVA", CATEGORY_LED_SPOT, 20));
        equipments.add(new Equipment("Silver Star CAM3/EMZ CCS3", CATEGORY_LED_SPOT, 20));
        equipments.add(new Equipment("PEGASYS V4", CATEGORY_LED_SPOT, 10));
        equipments.add(new Equipment("EK PRO BL2", CATEGORY_LED_SPOT, 15));
        equipments.add(new Equipment("EK PRO BL2 IP", CATEGORY_LED_SPOT, 15));
        equipments.add(new Equipment("EK Blinder II", CATEGORY_LED_SPOT, 10));

        // ===== SPOT =====
        equipments.add(new Equipment("ParLight", CATEGORY_SPOT, 50));
        equipments.add(new Equipment("フレネル/凸レンズスポット", CATEGORY_SPOT, 40));
        equipments.add(new Equipment("Source Four", CATEGORY_SPOT, 60));
        equipments.add(new Equipment("ITO", CATEGORY_SPOT, 30));
        equipments.add(new Equipment("ミニブル", CATEGORY_SPOT, 20));
        equipments.add(new Equipment("LHQ ハロゲン85W 12灯", CATEGORY_SPOT, 10));

        // ===== DIMMER =====
        equipments.add(new Equipment("JANDS FPX Dimmer 36ch", CATEGORY_DIMMER, 5));
        equipments.add(new Equipment("丸茂 Zemtour 12ch", CATEGORY_DIMMER, 5));
        equipments.add(new Equipment("PORTABLE DIMSTAR II", CATEGORY_DIMMER, 5));
        equipments.add(new Equipment("LITEPUTER DX-402A", CATEGORY_DIMMER, 5));

        // ===== PIN SPOT =====
        equipments.add(new Equipment("Xebex SUPERSOL 2Kw", CATEGORY_PIN_SPOT, 8));
        equipments.add(new Equipment("Xebex SUPERSOL 1Kw", CATEGORY_PIN_SPOT, 8));
        equipments.add(new Equipment("KJ-6 HMI-400W", CATEGORY_PIN_SPOT, 6));
        equipments.add(new Equipment("MIP650 ハロゲン650W", CATEGORY_PIN_SPOT, 6));

        // ===== FOG / HAZE =====
        equipments.add(new Equipment("Concept MK-Ⅴ/Ⅵ", CATEGORY_FOG_MACHINE, 3));
        equipments.add(new Equipment("Look Solutions VIPER-NT", CATEGORY_FOG_MACHINE, 3));
        equipments.add(new Equipment("ANTARI HZ500", CATEGORY_HAZE_MACHINE, 3));
        equipments.add(new Equipment("ANTARI HZ100", CATEGORY_HAZE_MACHINE, 3));

        // ===== OTHER =====
        equipments.add(new Equipment("電源ケーブル各種", CATEGORY_OTHER, 999));
        equipments.add(new Equipment("スタンド各種", CATEGORY_OTHER, 999));

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

        DefaultTableModel detailModel = new DefaultTableModel(detailColumns, 0);
        JTable detailTable = new JTable(detailModel);
        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setBorder(BorderFactory.createTitledBorder("選択中の現場明細"));

        JComboBox<Project> projectComboBox = new JComboBox<>();
        for (Project project : projects) {
            projectComboBox.addItem(project);
        }

        projectComboBox.addActionListener(e -> {
            Project selectedProject = (Project) projectComboBox.getSelectedItem();
            detailModel.setRowCount(0);
            if (selectedProject == null) return;

            for (RequestItem item : selectedProject.getItems()) {
                detailModel.addRow(new Object[]{
                        item.getEquipment().getName(),
                        item.getEquipment().getCategory(),
                        item.getQuantity()
                });
            }
        });

        JFrame frame = new JFrame();
        frame.setTitle("照明機材管理システム");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel equipmentPanel = new JPanel();
        JPanel projectPanel = new JPanel();
        JPanel dataPanel = new JPanel();

        JTextArea area = new JTextArea(8, 40);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);

        // 機材テーブル（今後縮小してもOK）
        String[] equipmentColumns = {
                "機材名",
                "カテゴリ",
                "在庫"
        };
        DefaultTableModel equipmentTableModel = new DefaultTableModel(equipmentColumns, 0);
        JTable equipmentTable = new JTable(equipmentTableModel);
        JScrollPane equipmentTableScroll = new JScrollPane(equipmentTable);
        equipmentTableScroll.setPreferredSize(new Dimension(800, 300));

        for (Equipment equipment : equipments) {
            equipmentTableModel.addRow(new Object[]{
                    equipment.getName(),
                    equipment.getCategory(),
                    equipment.getStock()
            });
        }

        // ボタン
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

        DefaultListModel<Equipment> equipmentModel = new DefaultListModel<>();
        JList<Equipment> equipmentList = new JList<>(equipmentModel);
        JScrollPane equipmentListScroll = new JScrollPane(equipmentList);

        for (Equipment equipment : equipments) {
            equipmentModel.addElement(equipment);
        }

        // 後で作成するカテゴリ別表示のモデルを、各イベントから共通更新する。
        ArrayList<JTextArea> categoryAreas = new ArrayList<>();
        ArrayList<DefaultListModel<Equipment>> categoryModels = new ArrayList<>();

        // ダブルクリックで現場登録（旧 UI）
        equipmentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {

                    Equipment selectedEquipment = equipmentList.getSelectedValue();
                    if (selectedEquipment == null) return;

                    String quantityText = JOptionPane.showInputDialog(
                            selectedEquipment.getName() + " の数量を入力してください");
                    if (quantityText == null) return;

                    Integer quantity = parsePositiveInt(frame, quantityText, "数量");
                    if (quantity == null) return;

                    Project selectedProject = (Project) projectComboBox.getSelectedItem();
                    if (selectedProject == null) {
                        JOptionPane.showMessageDialog(frame, "現場を選択してください");
                        return;
                    }

                    if (!updateEquipmentStock(selectedEquipment, -quantity)) {
                        JOptionPane.showMessageDialog(frame, MSG_STOCK_LACK);
                        return;
                    }

                    selectedProject.getItems().add(new RequestItem(selectedEquipment, quantity));
                    refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                            categoryAreas, categoryModels);

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

        // 機材タブ（今はそのまま残す）
        JPanel equipmentButtonPanel = new JPanel();
        equipmentButtonPanel.add(button);
        equipmentButtonPanel.add(searchButton);
        equipmentButtonPanel.add(restockButton);
        equipmentButtonPanel.add(categoryButton);

        String[] categories = CATEGORIES;

        JPanel categoryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        for (String category : categories) {

            JTextArea categoryArea = new JTextArea();
            categoryArea.setEditable(false);
            categoryAreas.add(categoryArea);

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
            pane.setBorder(BorderFactory.createTitledBorder(category));
            categoryPanel.add(pane);
        }

        equipmentPanel.setLayout(new BorderLayout());
        
        // 上段に新規機材の入力欄と全機材テーブル、中央にカテゴリ別在庫を表示する。
        JPanel equipmentAddPanel = new JPanel();
        equipmentAddPanel.setBorder(BorderFactory.createTitledBorder("機材追加"));
        equipmentAddPanel.add(new JLabel("機材名"));
        equipmentAddPanel.add(nameField);
        equipmentAddPanel.add(new JLabel("カテゴリ"));
        equipmentAddPanel.add(categoryField);
        equipmentAddPanel.add(new JLabel("在庫"));
        equipmentAddPanel.add(stockField);
        equipmentAddPanel.add(addButton);

        JPanel equipmentNorthPanel = new JPanel(new BorderLayout());
        equipmentNorthPanel.add(equipmentAddPanel, BorderLayout.NORTH);
        equipmentNorthPanel.add(equipmentTableScroll, BorderLayout.CENTER);
        equipmentPanel.add(equipmentNorthPanel, BorderLayout.NORTH);
        equipmentPanel.add(categoryPanel, BorderLayout.CENTER);
        equipmentPanel.add(equipmentButtonPanel, BorderLayout.SOUTH);

        // 現場タブ
        JPanel projectCreatePanel = new JPanel(new GridLayout(1, 5));
        projectCreatePanel.add(new JLabel("【現場作成】現場名"));
        projectCreatePanel.add(projectNameField);
        projectCreatePanel.add(new JLabel("【現場作成】日付"));
        projectCreatePanel.add(projectDateField);
        projectCreatePanel.add(addProjectButton);
        // projectComboBoxは登録フォームだけに配置する。
        // Swing部品を複数の親へ追加すると、先の親から自動的に外れるためである。

        JPanel registerPanel = new JPanel(new BorderLayout());
        JPanel registerFormPanel = new JPanel(new GridLayout(3, 2));

        registerFormPanel.add(new JLabel("現場"));
        registerFormPanel.add(projectComboBox);
        registerFormPanel.add(new JLabel("数量"));
        registerFormPanel.add(registerQuantityField);

        registerPanel.setBorder(BorderFactory.createTitledBorder("機材登録"));
        registerFormPanel.add(new JLabel(""));
        registerFormPanel.add(registerButton);

        // ★ 現場タブ用カテゴリ別パネル
        JPanel categoryRegisterPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        for (String category : CATEGORIES) {

            DefaultListModel<Equipment> model = new DefaultListModel<>();
            categoryModels.add(model);
            JList<Equipment> list = new JList<>(model);

            for (Equipment eq : equipments) {
                if (eq.getCategory().equals(category)) {
                    model.addElement(eq);
                }
            }

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (e.getClickCount() == 2) {

                        Equipment selected = list.getSelectedValue();
                        if (selected == null) return;

                        String text = JOptionPane.showInputDialog(
                                selected.getName() + " の数量を入力");
                        if (text == null) return;

                        Integer q = parsePositiveInt(frame, text, "数量");
                        if (q == null) return;

                        Project selectedProject = (Project) projectComboBox.getSelectedItem();
                        if (selectedProject == null) {
                            JOptionPane.showMessageDialog(frame, "現場を選択してください");
                            return;
                        }

                        if (!updateEquipmentStock(selected, -q)) {
                            JOptionPane.showMessageDialog(frame, MSG_STOCK_LACK);
                            return;
                        }

                        selectedProject.getItems().add(new RequestItem(selected, q));
                        refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                                categoryAreas, categoryModels);

                        area.setText(
                                selectedProject.getName()
                                        + " に "
                                        + selected.getName()
                                        + " を "
                                        + q + "台登録しました");

                        // 詳細テーブル更新
                        detailModel.setRowCount(0);
                        for (RequestItem item : selectedProject.getItems()) {
                            detailModel.addRow(new Object[]{
                                    item.getEquipment().getName(),
                                    item.getEquipment().getCategory(),
                                    item.getQuantity()
                            });
                        }
                    }
                }
            });

            JScrollPane pane = new JScrollPane(list);
            pane.setBorder(BorderFactory.createTitledBorder(category));
            categoryRegisterPanel.add(pane);
        }

        // ボタン登録用の機材リストと、ダブルクリック登録用のカテゴリ一覧を併設する。
        equipmentListScroll.setBorder(BorderFactory.createTitledBorder("登録する機材"));
        equipmentListScroll.setPreferredSize(new Dimension(220, 300));
        registerPanel.add(equipmentListScroll, BorderLayout.WEST);
        registerPanel.add(categoryRegisterPanel, BorderLayout.CENTER);
        registerPanel.add(registerFormPanel, BorderLayout.SOUTH);

        JPanel projectActionPanel = new JPanel();
        projectActionPanel.add(projectListButton);
        projectActionPanel.add(detailButton);
        projectActionPanel.add(pickingButton);
        projectActionPanel.add(deleteItemButton);
        // restockButtonは機材タブにのみ配置する（1つのSwing部品は複数配置できない）。


        projectPanel.setLayout(new BorderLayout());
        projectPanel.add(projectCreatePanel, BorderLayout.NORTH);
        JPanel projectCenterPanel = new JPanel(new BorderLayout());
        projectCenterPanel.add(registerPanel, BorderLayout.CENTER);
        projectCenterPanel.add(detailScroll, BorderLayout.SOUTH);
        projectPanel.add(projectCenterPanel, BorderLayout.CENTER);
        projectPanel.add(projectActionPanel, BorderLayout.SOUTH);

        // データ系
        dataPanel.add(brokenButton);
        dataPanel.add(brokenListButton);
        dataPanel.add(repairButton);
        dataPanel.add(saveButton);
        dataPanel.add(loadButton);

        // タブ
        JTabbedPane tab = new JTabbedPane();
        
        tab.addTab("現場", projectPanel);
        tab.addTab("データ管理", dataPanel);

        frame.add(tab, BorderLayout.CENTER);
        frame.add(scroll, BorderLayout.SOUTH);

        // ===== 各ボタンの動作 =====

        addButton.addActionListener(e -> {
            // 入力欄から新しい機材を作成し、データと一覧表示へ同時に反映する。
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            Integer stock = parsePositiveInt(frame, stockField.getText(), "在庫数");
            if (name.isEmpty() || category.isEmpty()) {
                area.setText("機材名とカテゴリを入力してください");
                return;
            }
            if (stock == null) return;

            equipments.add(new Equipment(name, category, stock));
            equipmentTableModel.addRow(new Object[]{name, category, stock});
            equipmentModel.addElement(equipments.get(equipments.size() - 1));
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            area.setText("追加しました: " + name);

            nameField.setText("");
            categoryField.setText("");
            stockField.setText("");
        });

        button.addActionListener(E -> {
            area.setText("");
            for (Equipment equipment : equipments) {
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
            String keyword = JOptionPane.showInputDialog("検索する機材名");
            if (keyword == null) return;
            area.setText("");
            boolean found = false;

            for (Equipment equipment : equipments) {
                if (equipment.getName().contains(keyword)) {
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

            if (!found) {
                area.setText("該当する機材がありません");
            }
        });

        restockButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("機材名");
            if (name == null) return;
            String addText = JOptionPane.showInputDialog("補充数");
            Integer addStock = parsePositiveInt(frame, addText, "補充数");
            if (addStock == null) return;

            Equipment target = findEquipmentByName(name, equipments);
            if (target == null) {
                area.setText(MSG_NOT_FOUND_EQUIPMENT);
                return;
            }

            updateEquipmentStock(target, addStock);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            area.setText(
                    target.getName()
                            + " の在庫を "
                            + addStock
                            + " 追加しました\n現在庫:"
                            + target.getStock());
        });

        categoryButton.addActionListener(e -> {
            String category = JOptionPane.showInputDialog("カテゴリ名");
            if (category == null) return;
            area.setText("");
            boolean found = false;

            for (Equipment equipment : equipments) {
                if (equipment.getCategory().equals(category)) {
                    area.append(
                            equipment.getName()
                                    + " / 在庫:"
                                    + equipment.getStock()
                                    + "\n");
                    found = true;
                }
            }

            if (!found) {
                area.setText("該当する機材がありません");
            }
        });

        addProjectButton.addActionListener(e -> {
            // 現場名は検索・保存時の識別子になるため空欄を許可しない。
            String name = projectNameField.getText().trim();
            String date = projectDateField.getText().trim();
            if (name.isEmpty() || date.isEmpty()) {
                area.setText("現場名と日付を入力してください");
                return;
            }
            if (findProjectByName(name, projects) != null) {
                area.setText("同じ名前の現場が既にあります");
                return;
            }

            Project project = new Project(name, date);
            projects.add(project);
            projectComboBox.addItem(project);

            area.setText("現場作成: " + name);

            projectNameField.setText("");
            projectDateField.setText("");
        });

        projectListButton.addActionListener(e -> {
            area.setText("");
            for (Project project : projects) {
                area.append(project.name + " (" + project.date + ")\n");
            }
        });

        registerButton.addActionListener(e -> {
            Project selectedProject = (Project) projectComboBox.getSelectedItem();
            Equipment selectedEquipment = equipmentList.getSelectedValue();

            if (selectedProject == null) {
                area.setText(MSG_NOT_FOUND_PROJECT);
                return;
            }
            if (selectedEquipment == null) {
                area.setText("機材を選択してください");
                return;
            }

            Integer quantity = parsePositiveInt(frame, registerQuantityField.getText(), "数量");
            if (quantity == null) return;

            if (!updateEquipmentStock(selectedEquipment, -quantity)) {
                area.setText(MSG_STOCK_LACK);
                return;
            }

            selectedProject.items.add(new RequestItem(selectedEquipment, quantity));
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            area.setText(
                    selectedProject.name + " に "
                            + selectedEquipment.getName() + " を "
                            + quantity + "台登録しました");

            detailModel.setRowCount(0);
            for (RequestItem item : selectedProject.getItems()) {
                detailModel.addRow(new Object[]{
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
            for (Project project : projects) {
                area.append("現場:" + project.name + "\n");

                if (project.items.size() == 0) {
                    area.append("機材未登録\n\n");
                } else {
                    for (RequestItem item : project.items) {
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
            String projectName = JOptionPane.showInputDialog("現場名");
            Project selectedProject = findProjectByName(projectName, projects);

            if (selectedProject == null) {
                area.setText(MSG_NOT_FOUND_PROJECT);
                return;
            }

            String equipmentName = JOptionPane.showInputDialog("削除する機材名");

            RequestItem targetItem = null;
            for (RequestItem item : selectedProject.items) {
                if (item.equipment.getName().equals(equipmentName)) {
                    targetItem = item;
                    break;
                }
            }

            if (targetItem == null) {
                area.setText(MSG_NOT_FOUND_EQUIPMENT);
                return;
            }

            targetItem.equipment.addStock(targetItem.quantity);
            selectedProject.items.remove(targetItem);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            area.setText(targetItem.equipment.getName() + " を削除しました");
        });

        brokenButton.addActionListener(e -> {
            String equipmentName = JOptionPane.showInputDialog("故障した機材名");
            if (equipmentName == null) return;
            Equipment selectedEquipment = findEquipmentByName(equipmentName, equipments);

            if (selectedEquipment == null) {
                area.setText(MSG_NOT_FOUND_EQUIPMENT);
                return;
            }

            String brokenText = JOptionPane.showInputDialog("故障数");
            Integer brokenCount = parsePositiveInt(frame, brokenText, "故障数");
            if (brokenCount == null) return;

            if (brokenCount > selectedEquipment.getStock()) {
                area.setText("在庫以上は登録できません");
                return;
            }

            selectedEquipment.removeStock(brokenCount);
            selectedEquipment.addBrokenStock(brokenCount);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            area.setText(
                    selectedEquipment.getName()
                            + " を "
                            + brokenCount
                            + "台故障登録しました");
        });

        brokenListButton.addActionListener(e -> {
            area.setText("");
            boolean found = false;

            for (Equipment equipment : equipments) {
                if (equipment.getBrokenStock() > 0) {
                    area.append(
                            equipment.getName()
                                    + " / 故障:"
                                    + equipment.getBrokenStock()
                                    + "台\n");
                    found = true;
                }
            }

            if (!found) {
                area.setText("故障機材はありません");
            }
        });

        repairButton.addActionListener(e -> {
            String equipmentName = JOptionPane.showInputDialog("修理した機材名");
            if (equipmentName == null) return;
            Equipment selectedEquipment = findEquipmentByName(equipmentName, equipments);

            if (selectedEquipment == null) {
                area.setText(MSG_NOT_FOUND_EQUIPMENT);
                return;
            }

            String repairText = JOptionPane.showInputDialog("修理台数");
            Integer repairCount = parsePositiveInt(frame, repairText, "修理台数");
            if (repairCount == null) return;

            if (repairCount > selectedEquipment.getBrokenStock()) {
                area.setText("故障数を超えています");
                return;
            }

            selectedEquipment.removeBrokenStock(repairCount);
            selectedEquipment.addStock(repairCount);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

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
            // 3種類のデータをCSV形式のテキストファイルへ保存する。
            // try-with-resourcesにより、途中で例外が起きてもファイルを確実に閉じる。
            try (PrintWriter pw = new PrintWriter(new File(FILE_EQUIPMENTS));
                    PrintWriter projectWriter = new PrintWriter(new File(FILE_PROJECTS));
                    PrintWriter itemWriter = new PrintWriter(new File(FILE_REQUEST_ITEMS))) {
                // 機材保存
                for (Equipment equipment : equipments) {
                    pw.println(
                            equipment.getName() + ","
                                    + equipment.getCategory() + ","
                                    + equipment.getStock() + ","
                                    + equipment.getBrokenStock());
                }

                // 現場保存
                for (Project project : projects) {
                    projectWriter.println(project.name + "," + project.date);
                }

                // 現場機材保存
                for (Project project : projects) {
                    for (RequestItem item : project.items) {
                        itemWriter.println(
                                project.name + ","
                                        + item.equipment.getName() + ","
                                        + item.quantity);
                    }
                }

                area.setText(MSG_SAVE_SUCCESS);

            } catch (FileNotFoundException ex) {
                area.setText(MSG_SAVE_FAIL);
            }
        });

        loadButton.addActionListener(e -> {
            // 保存ファイルをすべて開けた場合だけ、メモリ上のデータを再構築する。
            try (Scanner fileScanner = new Scanner(new File(FILE_EQUIPMENTS));
                    Scanner projectScanner = new Scanner(new File(FILE_PROJECTS));
                    Scanner itemScanner = new Scanner(new File(FILE_REQUEST_ITEMS))) {
                // 機材読込
                equipments.clear();

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] data = line.split(",");

                    Equipment equipment = new Equipment(
                            data[0],
                            data[1],
                            Integer.parseInt(data[2]));
                    equipment.setBrokenStock(Integer.parseInt(data[3]));
                    equipments.add(equipment);
                }

                // 現場読込
                projects.clear();
                projectComboBox.removeAllItems();

                while (projectScanner.hasNextLine()) {

                    String line = projectScanner.nextLine();
                    String[] data = line.split(",");

                    Project project =
                            new Project(data[0], data[1]);

                    projects.add(project);
                    projectComboBox.addItem(project);
                }

                // 現場機材読込
                while (itemScanner.hasNextLine()) {
                    String line = itemScanner.nextLine();
                    String[] data = line.split(",");

                    String projectName = data[0];
                    String equipmentName = data[1];
                    int quantity = Integer.parseInt(data[2]);

                    Project targetProject = findProjectByName(projectName, projects);
                    Equipment targetEquipment = findEquipmentByName(equipmentName, equipments);

                    if (targetProject != null && targetEquipment != null) {
                        targetProject.items.add(
                                new RequestItem(targetEquipment, quantity));
                    }
                }

                // 読み込んだ内容に合わせて、画面上のモデルも更新する。
                refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                        categoryAreas, categoryModels);

                area.setText(MSG_LOAD_SUCCESS);

            } catch (Exception ex) {
                area.setText(MSG_LOAD_FAIL);
            }
        });

        detailButton.addActionListener(e -> {

            Project selectedProject =
                    (Project) projectComboBox.getSelectedItem();

            if (selectedProject == null) {
                area.setText(MSG_NOT_FOUND_PROJECT);
                return;
            }

            JFrame previewFrame = new JFrame("現場プレビュー");
            previewFrame.setSize(700, 500);
            previewFrame.setLayout(new BorderLayout());

            JLabel headerLabel = new JLabel(
                    "現場名：" + selectedProject.getName()
                    + "    日付：" + selectedProject.getDate());

            previewFrame.add(headerLabel, BorderLayout.NORTH);

            JPanel previewPanel =
                    new JPanel(new GridLayout(2, 4, 10, 10));

            for (String category : CATEGORIES) {

                JTextArea categoryArea = new JTextArea();
                categoryArea.setEditable(false);

                for (RequestItem item : selectedProject.getItems()) {

                    if (item.getEquipment().getCategory().equals(category)) {

                        categoryArea.append(
                                item.getEquipment().getName()
                                + " × "
                                + item.getQuantity()
                                + "台\n");
                    }
                }

                JScrollPane categoryScroll =
                        new JScrollPane(categoryArea);

                categoryScroll.setBorder(
                        BorderFactory.createTitledBorder(category));

                previewPanel.add(categoryScroll);
            }

            previewFrame.add(previewPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel();

            JButton printButton = new JButton("印刷");
            printButton.addActionListener(e2 -> {

                PrinterJob job = PrinterJob.getPrinterJob();

                job.setPrintable((graphics, pageFormat, pageIndex) -> {

                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }

                    Graphics2D g2 = (Graphics2D) graphics;

                    g2.translate(
                            pageFormat.getImageableX(),
                            pageFormat.getImageableY());

                    previewPanel.printAll(g2);

                    return Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {

                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            bottomPanel.add(printButton);

            previewFrame.add(bottomPanel, BorderLayout.SOUTH);

            previewFrame.setLocationRelativeTo(frame);
            previewFrame.setVisible(true);
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
