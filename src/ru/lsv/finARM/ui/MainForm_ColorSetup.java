package ru.lsv.finARM.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Цветовое выделение
 */
public class MainForm_ColorSetup {
    private JButton saveBtn;
    private JButton cancelBtn;
    private JPanel mainPanel;
    private JButton a1Button;
    private JButton a3Button;
    private JButton a2Button;
    private JPanel panel1;
    private JLabel label1;
    private JPanel panel2;
    private JLabel label2;
    private JButton a4Button;
    private JLabel label3;
    private JLabel label4;
    private JButton a5Button;
    private JLabel label5;

    private Color[] colors = {Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK};

    private JDialog dialog;

    private boolean modalResult = false;

    public MainForm_ColorSetup(Frame owner) {
        dialog = new JDialog(owner, "Параметры цветового выделения");
        dialog.setModal(true);
        dialog.getContentPane().add(mainPanel);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doOnClosing();
            }
        });
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doOnClosing();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doNormalClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        //
        //
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOnClosing();
            }
        });
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обрабатываем нормально закрытие
                doNormalClose();
            }
        });
        a1Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colors[0] = JColorChooser.showDialog(mainPanel, "Цвет выделения закрытых договоров", colors[0]);
                label1.setForeground(colors[0]);
            }
        });
        a2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colors[1] = JColorChooser.showDialog(mainPanel, "Цвет выделения открытых договоров", colors[1]);
                label2.setForeground(colors[1]);
            }
        });
        a3Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colors[2] = JColorChooser.showDialog(mainPanel, "Цвет выделения авансов", colors[2]);
                label3.setForeground(colors[2]);
            }
        });
        a4Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colors[3] = JColorChooser.showDialog(mainPanel, "Цвет выделения расходов", colors[3]);
                label4.setForeground(colors[3]);
            }
        });
        a5Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                colors[4] = JColorChooser.showDialog(mainPanel, "Цвет выделения закрытых по зарплате договоров", colors[4]);
                label5.setForeground(colors[4]);
            }
        });
        // Загружаем...
        Properties props = new Properties();
        try {
            props.loadFromXML(new FileInputStream(System.getProperty("user.home") + "/.finARM/colors.properties"));
            for (int i = 0; i < colors.length; i++) {
                colors[i] = new Color(Integer.parseInt(props.getProperty("color" + (i + 1), "" + Color.BLACK.getRGB())));
            }
            recolorLabels();
        } catch (IOException ignored) {
        }
    }

    /**
     * Сохраняет цвета выделений
     */
    private void storeColors() {
        Properties colorProps = new Properties();
        for (int i = 0; i < colors.length; i++) {
            colorProps.setProperty("color" + (i + 1), "" + colors[i].getRGB());
        }
        try {
            File file = new File(System.getProperty("user.home") + "/.finARM/");
            if ((!file.exists()) && (!file.mkdir())) {
                throw new IOException("");
            }
            colorProps.storeToXML(new FileOutputStream(System.getProperty("user.home") + "/.finARM/colors.properties"), "Цвета выделения");
        } catch (IOException ignored) {
        }
    }

    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        modalResult = true;
        dialog.setVisible(false);
    }

    /**
     * Обработка закрытия без сохранения
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    /**
     * Показывает диалог цветового выделения
     *
     * @param positionComponent Компонент для позиционирования
     * @return true - выход с сохранением, false - без
     */
    public boolean show(Component positionComponent) {
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        Color[] tempColors = new Color[colors.length];
        System.arraycopy(colors, 0, tempColors, 0, colors.length);
        dialog.setVisible(true);
        if (!modalResult) {
            System.arraycopy(tempColors, 0, colors, 0, colors.length);
            recolorLabels();
        } else {
            storeColors();
        }
        return modalResult;
    }

    private void recolorLabels() {
        label1.setForeground(colors[0]);
        label2.setForeground(colors[1]);
        label3.setForeground(colors[2]);
        label4.setForeground(colors[3]);
    }

    /**
     * Цвет выделения закрытых договоров
     *
     * @return Цвет
     */
    public Color getClosedColor() {
        return colors[0];
    }

    /**
     * Цвет выделения открытых договоров
     *
     * @return Цвет
     */
    public Color getOpenedColor() {
        return colors[1];
    }

    /**
     * Цвет выделения авансов
     *
     * @return Цвет
     */
    public Color getPrepaidColor() {
        return colors[2];
    }

    /**
     * Цвет выделения расходов
     *
     * @return Цвет
     */
    public Color getSpendingColor() {
        return colors[3];
    }

    /**
     * Цвет выделения закрытых по зарплате договоров
     *
     * @return Цвет
     */
    public Color getClosedForSalaryColor() {
        return colors[4];
    }

}
