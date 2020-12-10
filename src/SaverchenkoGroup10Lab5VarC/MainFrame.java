package SaverchenkoGroup10Lab5VarC;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

public class MainFrame extends JFrame {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    JMenuItem modifyItem, modifyConditionItem,showGridItem,
            turnLeftItem,showAxisItem,setToDefaultItem,save;

    private JFileChooser fileChooser = null;
    private boolean fileLoaded = false;

    GraphicsDisplay display = new GraphicsDisplay();
    GraphicsMenuListener menu = new GraphicsMenuListener();

    public MainFrame() {

        super("Обработка событий мыши");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH) / 2, (kit.getScreenSize().height - HEIGHT) / 2);
        Image img = kit.getImage("src/SaverchenkoGroup10Lab5VarC/icon.PNG");
        setIconImage(img);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu file = menuBar.add(new JMenu("Файл"));
        JMenu graphics = menuBar.add(new JMenu("График"));
        graphics.addMenuListener(new GraphicsMenuListener());
        JMenuItem open = file.add(new JMenuItem("Открыть файл с графиком"));
        open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        open.addActionListener(e -> {
            if (fileChooser == null) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("C:\\Users\\SergeySaber\\IdeaProjects\\lab5p"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("bin files", "bin");
                fileChooser.setFileFilter(filter);
            }
            if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                openGraphics(fileChooser.getSelectedFile());
        });

        save = file.add(new JMenuItem("Сохранить значения графика"));
        save.setEnabled(false);
        save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        save.addActionListener(e -> {
            if (fileChooser == null) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("C:\\Users\\SergeySaber\\IdeaProjects\\lab5p"));
            }
            if (fileChooser.showSaveDialog(MainFrame.this)==JFileChooser.APPROVE_OPTION)
                saveGraphics(fileChooser.getSelectedFile(), display.getGraphicsData());
        });

        JMenuItem close = file.add(new JMenuItem("Выход"));
        close.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        close.addActionListener(e -> System.exit(0));

        showAxisItem = graphics.add(new JCheckBoxMenuItem("Показать оси"));
        showAxisItem.setSelected(true);
        showAxisItem.addActionListener(e -> display.setShowAxis(showAxisItem.isSelected()));

        modifyItem = graphics.add(new JCheckBoxMenuItem("Модификация отображения"));
        modifyItem.addActionListener(e -> display.setDefaultCondition(modifyItem.isSelected()));

        modifyConditionItem = graphics.add(new JCheckBoxMenuItem("Модификация отображения с условием"));
        modifyConditionItem.addActionListener(e -> display.setModifiedCondition(modifyConditionItem.isSelected()));

        turnLeftItem = graphics.add(new JCheckBoxMenuItem("Поворот влево на 90°"));
        turnLeftItem.addActionListener(e -> display.setTurnGraph(turnLeftItem.isSelected()));

        showGridItem = graphics.add(new JCheckBoxMenuItem("Показать сетку"));
        showGridItem.addActionListener(e -> display.setShowGrid(showGridItem.isSelected()));

        setToDefaultItem = graphics.add(new JMenuItem("Отменить все изменения"));
        setToDefaultItem.setEnabled(false);
        setToDefaultItem.addActionListener(e -> {
            menu.menuGlobal(false);
            setToDefaultItem.setEnabled(false);
            display.reset();
        });
        setToDefaultItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));

        getContentPane().add(display, BorderLayout.CENTER);
    }

    public void menuStatusOnline (){

        setToDefaultItem.setEnabled(menu.atLeastOneIsSelected());
        if (modifyItem.isSelected())
            modifyConditionItem.setEnabled(true);
        else {
            modifyConditionItem.setSelected(false);
            modifyConditionItem.setEnabled(false);
            display.setModifiedCondition(false);
        }
        save.setEnabled(display.changes); //доработать для случая когда хоть 1 точка смещена

    }

    protected void openGraphics (File selectedFile) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(selectedFile))) {
            Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];
            int i = 0;
            while (in.available() > 0) {
                double x = in.readDouble();
                double y = in.readDouble();
                graphicsData[i++] = new Double[]{x, y};
            }

            if (graphicsData.length > 0) {
                fileLoaded = true;
                menu.menuGlobal(false);
                display.showGraphics(graphicsData);
            }

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        }
    }

    protected void saveGraphics (File selectedFile, Double[][] graphics) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile, false))) {

            for (Double[] graphic : graphics) {
                out.writeDouble(graphic[0]);
                out.writeDouble(graphic[1]);
            }

        } catch (IOException exc) {
            System.out.println("Ошибка записи в файл");
            exc.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setVisible(true);
        while(frame.isVisible())
            frame.menuStatusOnline();
    }

    private class GraphicsMenuListener implements MenuListener {

        public void menuSelected(MenuEvent e) {
            turnLeftItem.setEnabled(fileLoaded);
            modifyItem.setEnabled(fileLoaded);
            modifyConditionItem.setEnabled(false);
            showGridItem.setEnabled(fileLoaded);
        }

        public void menuGlobal(boolean off) {

            showAxisItem.setSelected(!off);

            modifyItem.setSelected(off);
            modifyConditionItem.setSelected(off);
            turnLeftItem.setSelected(off);
            showGridItem.setSelected(off);

            display.setShowAxis(!off);
            display.setDefaultCondition(off);
            display.setTurnGraph(off);
            display.setShowGrid(off);
        }

        public boolean atLeastOneIsSelected(){
            if (turnLeftItem.isSelected() || modifyItem.isSelected())
                return true;
            else if (!showAxisItem.isSelected())
                return true;
            else return modifyConditionItem.isSelected() || showGridItem.isSelected();
        }

        public void menuDeselected(MenuEvent e) {}

        public void menuCanceled(MenuEvent e) {}
    }
}