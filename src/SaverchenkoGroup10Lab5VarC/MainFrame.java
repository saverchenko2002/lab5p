package SaverchenkoGroup10Lab5VarC;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class MainFrame extends JFrame {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    JMenuItem modifyItem;
    JMenuItem modifyConditionItem;
    JMenuItem showGridItem;
    JMenuItem turnLeftItem;
    JMenuItem showAxisItem;
    JMenuItem setToDefaultItem;

    private JFileChooser fileChooser = null;
    private boolean fileLoaded = false;

    GraphicsDisplay display = new GraphicsDisplay();
    GraphicsMenuListener menu = new GraphicsMenuListener();

    public MainFrame() {

        super("Построение графиков функций на основе заранее подготовленных файлов");
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH) / 2, (kit.getScreenSize().height - HEIGHT) / 2);
        Image img = kit.getImage("src/SaverchenkoGroup10Lab5VarC/icon.PNG");
        setIconImage(img);
        setExtendedState(MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu file = menuBar.add(new JMenu("Файл"));
        JMenu graphics = menuBar.add(new JMenu("График"));
        graphics.addMenuListener(new GraphicsMenuListener());
        JMenuItem open = file.add(new JMenuItem("Открыть файл с графиком"));
        open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("C:\\Users\\SergeySaber\\IdeaProjects\\lab5p"));
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("bin files", "bin");
                    fileChooser.setFileFilter(filter);
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        });

        JMenuItem close = file.add(new JMenuItem("Выход"));
        close.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        showAxisItem = graphics.add(new JCheckBoxMenuItem("Показать оси"));
        showAxisItem.setSelected(true);
        showAxisItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.setShowAxis(showAxisItem.isSelected());
            }
        });

        modifyItem = graphics.add(new JCheckBoxMenuItem("Модификация отображения"));
        modifyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.setDefaultCondition(modifyItem.isSelected());
            }
        });

        modifyConditionItem = graphics.add(new JCheckBoxMenuItem("Модификация отображения с условием"));
        modifyConditionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.setModifiedCondition(modifyConditionItem.isSelected());
            }
        });

        turnLeftItem = graphics.add(new JCheckBoxMenuItem("Поворот влево на 90°"));
        turnLeftItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.setTurnGraph(turnLeftItem.isSelected());
            }
        });

        showGridItem = graphics.add(new JCheckBoxMenuItem("Показать сетку"));
        showGridItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (showGridItem.isSelected()) {
                    String valueX = JOptionPane.showInputDialog(MainFrame.this,
                            "Введите сколько знаков после запятой в Х:\nminX-" + display.getIncrX(), "Ограничение Х", JOptionPane.QUESTION_MESSAGE);
                    String valueY = JOptionPane.showInputDialog(MainFrame.this,
                            "Введите сколько знаков после запятой в Y:\nminY-" + display.getIncrY(), "Ограничение Y", JOptionPane.QUESTION_MESSAGE);
                    if ((display.getIncrXDouble().intValue()==0 && display.getIncrXDouble()>Double.parseDouble(valueX) )||
                            (display.getIncrYDouble().intValue()==0 && display.getIncrYDouble()>Double.parseDouble(valueY))) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "В минимальном значении знак на дальнем разряде", "Ошибочный ввод числа знаков", JOptionPane.WARNING_MESSAGE);
                        showGridItem.setSelected(false);
                    }
                    else {
                        display.setXDigits(Integer.parseInt(valueX));
                        display.setYDigits(Integer.parseInt(valueY));
                        display.setShowGrid(showGridItem.isSelected());
                    }
                }
                else
                    display.setShowGrid(showGridItem.isSelected());
            }
        });

        setToDefaultItem = graphics.add(new JMenuItem("Отменить все изменения"));
        setToDefaultItem.setEnabled(false);
        setToDefaultItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menu.menuGlobal(false);
                setToDefaultItem.setEnabled(false);
            }
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
    }

    protected void openGraphics (File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 4][];
            int i = 0;
            while (in.available() > 0) {
                double x = in.readDouble();
                double y = in.readDouble();
                in.skipBytes(2*(Double.SIZE/8));
                graphicsData[i++] = new Double[]{x, y};
            }

            if (graphicsData.length > 0) {
                fileLoaded = true;
                menu.menuGlobal(false);
                display.showGraphics(graphicsData);
            }

            in.close();

        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
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