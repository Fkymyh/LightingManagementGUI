import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
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
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
            CATEGORY_HAZE_MACHINE,
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

    /** 操作結果を消さずにログ欄へ追記し、最新行まで自動スクロールする。 */
    private static void appendLog(JTextArea area, String message) {
        if (area.getDocument().getLength() > 0) {
            area.append("\n");
        }
        area.append(message);
        area.setCaretPosition(area.getDocument().getLength());
    }

    /** 選択した現場に現在登録されている機材をログ欄へ表示する。 */
    private static void appendProjectSummary(JTextArea area, Project project) {
        if (project == null) return;

        StringBuilder summary = new StringBuilder();
        summary.append("【登録状況】").append(project.getName());
        if (project.getItems().isEmpty()) {
            summary.append("：機材未登録");
        } else {
            for (RequestItem item : project.getItems()) {
                summary.append("\n  ")
                        .append(item.getEquipment().getName())
                        .append(" × ")
                        .append(item.getQuantity());
            }
        }
        appendLog(area, summary.toString());
    }

    /** 選択済みの機材について故障数だけを入力し、在庫を故障在庫へ移す。 */
    private static boolean registerBrokenEquipment(
            JFrame owner, JTextArea area, Equipment equipment) {
        String brokenText = JOptionPane.showInputDialog(
                owner,
                equipment.getName() + " の故障数",
                "故障機材登録",
                JOptionPane.QUESTION_MESSAGE);
        Integer brokenCount = parsePositiveInt(owner, brokenText, "故障数");
        if (brokenCount == null) return false;

        if (brokenCount > equipment.getStock()) {
            JOptionPane.showMessageDialog(owner, "在庫以上は登録できません");
            return false;
        }

        equipment.removeStock(brokenCount);
        equipment.addBrokenStock(brokenCount);
        appendLog(area,
                equipment.getName() + " を "
                        + brokenCount + "台故障登録しました");
        return true;
    }

    /** 在庫と故障数が一目で分かる機材リスト用の表示部品を作る。 */
    private static DefaultListCellRenderer createEquipmentStatusRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Equipment equipment) {
                    setText(equipment.getName()
                            + "（在庫:" + equipment.getStock()
                            + " / 故障:" + equipment.getBrokenStock() + "）");
                }
                return this;
            }
        };
    }

    /** 最大化操作なしで内容を読みやすい、画面内に収まる大きさへ調整する。 */
    private static void setComfortablePreviewSize(Window window) {
        Rectangle screenBounds = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int margin = 60;
        window.setSize(
                Math.max(900, screenBounds.width - margin),
                Math.max(650, screenBounds.height - margin));
    }

    /** 故障中の機材を、現場画面と同じカテゴリ別レイアウトで表示する。 */
    private static JPanel createBrokenEquipmentCategoryPanel(
            ArrayList<Equipment> equipments,
            Consumer<Equipment> doubleClickAction) {
        JPanel panel = new JPanel(new GridLayout(3, 3, 10, 10));

        for (String category : CATEGORIES) {
            DefaultListModel<Equipment> model = new DefaultListModel<>();
            for (Equipment equipment : equipments) {
                if (equipment.getBrokenStock() > 0
                        && equipment.getCategory().equals(category)) {
                    model.addElement(equipment);
                }
            }

            JList<Equipment> list = new JList<>(model);
            list.setCellRenderer(createEquipmentStatusRenderer());
            if (doubleClickAction != null) {
                list.setToolTipText("機材をダブルクリックして選択");
                list.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() != 2) return;
                        Equipment selected = list.getSelectedValue();
                        if (selected != null) {
                            doubleClickAction.accept(selected);
                        }
                    }
                });
            }

            JScrollPane pane = new JScrollPane(list);
            pane.setBorder(BorderFactory.createTitledBorder(category));
            panel.add(pane);
        }
        return panel;
    }

    /** 故障中の機材だけをカテゴリ別の別ウィンドウで表示する。 */
    private static void showBrokenEquipmentPreview(
            JFrame owner, ArrayList<Equipment> equipments) {
        boolean hasBrokenEquipment =
                equipments.stream().anyMatch(e -> e.getBrokenStock() > 0);
        if (!hasBrokenEquipment) {
            JOptionPane.showMessageDialog(owner, "故障機材はありません");
            return;
        }

        JFrame previewFrame = new JFrame("故障機材一覧");
        setComfortablePreviewSize(previewFrame);
        previewFrame.add(
                createBrokenEquipmentCategoryPanel(equipments, null),
                BorderLayout.CENTER);
        previewFrame.setLocationRelativeTo(owner);
        previewFrame.setVisible(true);
    }

    /** カテゴリ別の故障一覧から、修理する機材をダブルクリックで選択する。 */
    private static Equipment selectBrokenEquipmentForRepair(
            JFrame owner, ArrayList<Equipment> equipments) {
        if (equipments.stream().noneMatch(e -> e.getBrokenStock() > 0)) {
            JOptionPane.showMessageDialog(owner, "故障機材はありません");
            return null;
        }

        Equipment[] selectedEquipment = new Equipment[1];
        JDialog dialog = new JDialog(owner, "修理完了", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(
                new JLabel("修理する機材をダブルクリックしてください"),
                BorderLayout.NORTH);
        dialog.add(
                createBrokenEquipmentCategoryPanel(equipments, equipment -> {
                    selectedEquipment[0] = equipment;
                    dialog.dispose();
                }),
                BorderLayout.CENTER);
        setComfortablePreviewSize(dialog);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return selectedEquipment[0];
    }

    /** 全機材のカテゴリ別一覧から、対象機材をダブルクリックで選択する。 */
    private static Equipment selectEquipmentFromCategories(
            JFrame owner, ArrayList<Equipment> equipments,
            String title, String instruction) {
        Equipment[] selectedEquipment = new Equipment[1];
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel(instruction), BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(3, 3, 10, 10));
        for (String category : CATEGORIES) {
            DefaultListModel<Equipment> model = new DefaultListModel<>();
            for (Equipment equipment : equipments) {
                if (equipment.getCategory().equals(category)) {
                    model.addElement(equipment);
                }
            }

            JList<Equipment> list = new JList<>(model);
            list.setCellRenderer(createEquipmentStatusRenderer());
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    Equipment selected = list.getSelectedValue();
                    if (selected != null) {
                        selectedEquipment[0] = selected;
                        dialog.dispose();
                    }
                }
            });
            JScrollPane pane = new JScrollPane(list);
            pane.setBorder(BorderFactory.createTitledBorder(category));
            panel.add(pane);
        }

        dialog.add(panel, BorderLayout.CENTER);
        setComfortablePreviewSize(dialog);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return selectedEquipment[0];
    }

    /** 選択中の現場に登録された機材から、削除対象を選択する。 */
    private static RequestItem selectProjectItemForDeletion(
            JFrame owner, Project project) {
        if (project == null || project.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(owner, "登録済みの機材はありません");
            return null;
        }

        RequestItem[] selectedItem = new RequestItem[1];
        JDialog dialog = new JDialog(owner, "現場機材削除", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(
                new JLabel(project.getName()
                        + "：削除する機材をダブルクリックしてください"),
                BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(3, 3, 10, 10));
        for (String category : CATEGORIES) {
            DefaultListModel<RequestItem> model = new DefaultListModel<>();
            for (RequestItem item : project.getItems()) {
                if (item.getEquipment().getCategory().equals(category)) {
                    model.addElement(item);
                }
            }

            JList<RequestItem> list = new JList<>(model);
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> source, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(
                            source, value, index, isSelected, cellHasFocus);
                    if (value instanceof RequestItem item) {
                        setText(item.getEquipment().getName()
                                + " × " + item.getQuantity() + "台");
                    }
                    return this;
                }
            });
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    RequestItem selected = list.getSelectedValue();
                    if (selected != null) {
                        selectedItem[0] = selected;
                        dialog.dispose();
                    }
                }
            });
            JScrollPane pane = new JScrollPane(list);
            pane.setBorder(BorderFactory.createTitledBorder(category));
            panel.add(pane);
        }

        dialog.add(panel, BorderLayout.CENTER);
        setComfortablePreviewSize(dialog);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return selectedItem[0];
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
                    equipment.getName(), equipment.getCategory(),
                    equipment.getStock(), equipment.getBrokenStock()
            });
            listModel.addElement(equipment);
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(equipment.getCategory())) {
                    categoryAreas.get(i).append(
                            equipment.getName()
                                    + "（在庫:" + equipment.getStock()
                                    + " / 故障:" + equipment.getBrokenStock()
                                    + "）\n");
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
        setComfortablePreviewSize(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel equipmentPanel = new JPanel();
        JPanel projectPanel = new JPanel();
        JPanel dataPanel = new JPanel();

        JTextArea area = new JTextArea(5, 40);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder("操作ログ・登録状況"));

        projectComboBox.addActionListener(e ->
                appendProjectSummary(area, (Project) projectComboBox.getSelectedItem()));

        // 機材テーブル（今後縮小してもOK）
        String[] equipmentColumns = {
                "機材名",
                "カテゴリ",
                "在庫",
                "故障在庫"
        };
        DefaultTableModel equipmentTableModel = new DefaultTableModel(equipmentColumns, 0);
        JTable equipmentTable = new JTable(equipmentTableModel);
        JScrollPane equipmentTableScroll = new JScrollPane(equipmentTable);
        equipmentTableScroll.setPreferredSize(new Dimension(800, 300));

        for (Equipment equipment : equipments) {
            equipmentTableModel.addRow(new Object[]{
                    equipment.getName(),
                    equipment.getCategory(),
                    equipment.getStock(),
                    equipment.getBrokenStock()
            });
        }

        // ボタン
        JButton button = new JButton("機材一覧");
        JButton addButton = new JButton("機材追加");
        JButton searchButton = new JButton("機材検索");
        JButton restockButton = new JButton("在庫補充");
        JButton projectRestockButton = new JButton("在庫補充");
        JButton categoryButton = new JButton("カテゴリ検索");
        JButton addProjectButton = new JButton("現場追加");
        JButton projectListButton = new JButton("現場一覧");
        JButton registerButton = new JButton("現場へ機材登録");
        JButton detailButton = new JButton("現場詳細");
        JButton pickingButton = new JButton("ピッキングリスト表示");
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

                    appendLog(area,
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

        JPanel categoryPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        for (String category : categories) {

            JTextArea categoryArea = new JTextArea();
            categoryArea.setEditable(false);
            categoryArea.setToolTipText("機材名をダブルクリックすると故障登録できます");
            categoryAreas.add(categoryArea);

            for (Equipment equipment : equipments) {
                if (equipment.getCategory().equals(category)) {
                    categoryArea.append(
                            equipment.getName()
                                    + "（在庫:"
                                    + equipment.getStock()
                                    + " / 故障:"
                                    + equipment.getBrokenStock()
                                    + "）\n");
                }
            }

            categoryArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;

                    int offset = categoryArea.viewToModel2D(e.getPoint());
                    if (offset < 0) return;
                    int clickedLine = categoryArea.getDocument()
                            .getDefaultRootElement().getElementIndex(offset);

                    int categoryLine = 0;
                    Equipment selectedEquipment = null;
                    for (Equipment equipment : equipments) {
                        if (!equipment.getCategory().equals(category)) continue;
                        if (categoryLine == clickedLine) {
                            selectedEquipment = equipment;
                            break;
                        }
                        categoryLine++;
                    }
                    if (selectedEquipment == null) return;

                    if (registerBrokenEquipment(frame, area, selectedEquipment)) {
                        refreshEquipmentViews(
                                equipments, equipmentTableModel, equipmentModel,
                                categoryAreas, categoryModels);
                    }
                }
            });

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
        JPanel projectCreatePanel = new JPanel();
        projectCreatePanel.setBorder(
                BorderFactory.createTitledBorder("新しい現場を作成"));
        projectCreatePanel.add(new JLabel("現場名"));
        projectCreatePanel.add(projectNameField);
        projectCreatePanel.add(new JLabel("日付"));
        projectCreatePanel.add(projectDateField);
        projectCreatePanel.add(addProjectButton);

        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.setBorder(BorderFactory.createTitledBorder(
                "機材登録（機材をダブルクリック）"));

        JPanel projectSelectionPanel = new JPanel();
        projectSelectionPanel.setBorder(
                BorderFactory.createTitledBorder("作業する現場"));
        projectSelectionPanel.add(new JLabel("現場"));
        projectSelectionPanel.add(projectComboBox);

        JPanel projectTopPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        projectTopPanel.add(projectCreatePanel);
        projectTopPanel.add(projectSelectionPanel);

        // ★ 現場タブ用カテゴリ別パネル
        JPanel categoryRegisterPanel = new JPanel(new GridLayout(3, 3, 10, 10));

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

                        appendLog(area,
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

        registerPanel.add(categoryRegisterPanel, BorderLayout.CENTER);

        JPanel projectActionPanel = new JPanel();
        projectActionPanel.add(projectListButton);
        projectActionPanel.add(detailButton);
        projectActionPanel.add(pickingButton);
        projectActionPanel.add(deleteItemButton);
        projectActionPanel.add(projectRestockButton);


        projectPanel.setLayout(new BorderLayout());
        projectPanel.add(projectTopPanel, BorderLayout.NORTH);
        projectPanel.add(registerPanel, BorderLayout.CENTER);
        projectPanel.add(projectActionPanel, BorderLayout.SOUTH);

        // データ管理タブでは、機材データを確認しながら各管理操作を行う。
        JPanel dataButtonPanel = new JPanel();
        dataButtonPanel.add(brokenButton);
        dataButtonPanel.add(brokenListButton);
        dataButtonPanel.add(repairButton);
        dataButtonPanel.add(saveButton);
        dataButtonPanel.add(loadButton);

        JPanel dataCategoryPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        for (int i = 0; i < CATEGORIES.length; i++) {
            JList<Equipment> dataEquipmentList =
                    new JList<>(categoryModels.get(i));
            dataEquipmentList.setCellRenderer(createEquipmentStatusRenderer());
            dataEquipmentList.setToolTipText(
                    "機材をダブルクリックすると故障数を入力できます");
            dataEquipmentList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) return;
                    Equipment selectedEquipment =
                            dataEquipmentList.getSelectedValue();
                    if (selectedEquipment == null) return;

                    if (registerBrokenEquipment(frame, area, selectedEquipment)) {
                        refreshEquipmentViews(
                                equipments, equipmentTableModel, equipmentModel,
                                categoryAreas, categoryModels);
                    }
                }
            });

            JScrollPane dataPane = new JScrollPane(dataEquipmentList);
            dataPane.setBorder(
                    BorderFactory.createTitledBorder(CATEGORIES[i]));
            dataCategoryPanel.add(dataPane);
        }

        dataPanel.setLayout(new BorderLayout());
        dataPanel.add(dataButtonPanel, BorderLayout.NORTH);
        dataPanel.add(dataCategoryPanel, BorderLayout.CENTER);

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
            equipmentTableModel.addRow(new Object[]{name, category, stock, 0});
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
            Equipment target = selectEquipmentFromCategories(
                    frame,
                    equipments,
                    "在庫補充",
                    "補充する機材をダブルクリックしてください");
            if (target == null) return;

            String addText = JOptionPane.showInputDialog(
                    frame,
                    target.getName() + " の補充数",
                    "在庫補充",
                    JOptionPane.QUESTION_MESSAGE);
            Integer addStock = parsePositiveInt(frame, addText, "補充数");
            if (addStock == null) return;

            updateEquipmentStock(target, addStock);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            appendLog(area,
                    target.getName()
                            + " の在庫を "
                            + addStock
                            + " 追加しました\n現在庫:"
                            + target.getStock());
        });

        // 現場画面からも機材画面と同じ在庫補充処理を実行する。
        projectRestockButton.addActionListener(e -> restockButton.doClick());

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

            appendLog(area, "現場作成: " + name);

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

            appendLog(area,
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

        // 選択中の現場を、印刷可能なカテゴリ別プレビューで表示する。
        pickingButton.addActionListener(e -> detailButton.doClick());

        deleteItemButton.addActionListener(e -> {
            Project selectedProject =
                    (Project) projectComboBox.getSelectedItem();
            if (selectedProject == null) {
                JOptionPane.showMessageDialog(frame, "現場を選択してください");
                return;
            }

            RequestItem targetItem =
                    selectProjectItemForDeletion(frame, selectedProject);
            if (targetItem == null) return;

            int confirmation = JOptionPane.showConfirmDialog(
                    frame,
                    targetItem.getEquipment().getName()
                            + " × " + targetItem.getQuantity()
                            + "台を現場から削除しますか？",
                    "現場機材削除",
                    JOptionPane.YES_NO_OPTION);
            if (confirmation != JOptionPane.YES_OPTION) return;

            targetItem.equipment.addStock(targetItem.quantity);
            selectedProject.items.remove(targetItem);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            appendLog(area,
                    selectedProject.getName() + " から "
                            + targetItem.equipment.getName() + " を削除しました");
        });

        brokenButton.addActionListener(e -> {
            String equipmentName = JOptionPane.showInputDialog("故障した機材名");
            if (equipmentName == null) return;
            Equipment selectedEquipment = findEquipmentByName(equipmentName, equipments);

            if (selectedEquipment == null) {
                area.setText(MSG_NOT_FOUND_EQUIPMENT);
                return;
            }

            if (registerBrokenEquipment(frame, area, selectedEquipment)) {
                refreshEquipmentViews(
                        equipments, equipmentTableModel, equipmentModel,
                        categoryAreas, categoryModels);
            }
        });

        brokenListButton.addActionListener(e ->
                showBrokenEquipmentPreview(frame, equipments));

        repairButton.addActionListener(e -> {
            Equipment selectedEquipment =
                    selectBrokenEquipmentForRepair(frame, equipments);
            if (selectedEquipment == null) return;

            String repairText = JOptionPane.showInputDialog(
                    frame,
                    selectedEquipment.getName() + " の修理台数",
                    "修理完了",
                    JOptionPane.QUESTION_MESSAGE);
            Integer repairCount = parsePositiveInt(frame, repairText, "修理台数");
            if (repairCount == null) return;

            if (repairCount > selectedEquipment.getBrokenStock()) {
                JOptionPane.showMessageDialog(frame, "故障数を超えています");
                return;
            }

            selectedEquipment.removeBrokenStock(repairCount);
            selectedEquipment.addStock(repairCount);
            refreshEquipmentViews(equipments, equipmentTableModel, equipmentModel,
                    categoryAreas, categoryModels);

            appendLog(area,
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
            setComfortablePreviewSize(previewFrame);
            previewFrame.setLayout(new BorderLayout());

            JLabel headerLabel = new JLabel(
                    "現場名：" + selectedProject.getName()
                    + "    日付：" + selectedProject.getDate());

            previewFrame.add(headerLabel, BorderLayout.NORTH);

            JPanel previewPanel =
                    new JPanel(new GridLayout(3, 3, 10, 10));

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
        appendProjectSummary(area, (Project) projectComboBox.getSelectedItem());
        frame.setVisible(true);
    }
}
