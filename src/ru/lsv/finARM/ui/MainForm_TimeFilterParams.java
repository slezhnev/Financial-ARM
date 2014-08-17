package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.FinancialMonth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.Calendar;

/**
 * Настройка параметров временного фильтра
 */
public class MainForm_TimeFilterParams {
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JRadioButton monthRadioButton;
    private JRadioButton periodRadioButton;
    private JComboBox monthComboBox;
    private JDateChooser dateEdit1;
    private JDateChooser dateEdit2;
    private JDialog dialog;

    private boolean modalResult = false;

    public MainForm_TimeFilterParams(Frame owner) {
        dialog = new JDialog(owner, "Параметры просмотра");
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
        //
        monthComboBox.setModel(FinancialMonths.getInstance().getComboBoxModel());
        monthComboBox.setRenderer(FinancialMonths.getInstance().getComboBoxRenderer());
        //
        monthRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEnableDisable();
            }
        });
        periodRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEnableDisable();
            }
        });
    }

    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        if (monthRadioButton.isSelected()) {
            if (monthComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(mainPanel, "Не выбран месяц", "Параметры временного выделения",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (periodRadioButton.isSelected()) {
            if ((dateEdit1.getDate() == null) || (dateEdit2.getDate() == null)) {
                JOptionPane.showMessageDialog(mainPanel, "Не указана одна из дат периода", "Параметры временного выделения",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date date1 = new Date(dateEdit1.getDate().getTime());
            Date date2 = new Date(dateEdit2.getDate().getTime());
            if (date1.compareTo(date2) > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Дата начала периода находится после даты окончания периода", "Параметры временного выделения",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
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
     * Редактирование параметров временного фильтра
     *
     * @param params            Параметры для редактирования
     * @param positionComponent Компонента позиционирования
     * @return Временной фильтр или null, если выход был без сохранения
     */
    public MainForm.TimeFilterParams doEdit(MainForm.TimeFilterParams params, Component positionComponent) {
        if (params.getBeginDate() == null) {
            monthRadioButton.setSelected(true);
            FinancialMonth month = new FinancialMonth(params.getSelectedMonth(), params.getSelectedYear());
            monthComboBox.setSelectedItem(month);
            Calendar cal = Calendar.getInstance();
            Date date = new Date(cal.getTimeInMillis());
            dateEdit1.setDate(date);
            dateEdit2.setDate(date);
        } else {
            periodRadioButton.setSelected(true);
            monthComboBox.setSelectedItem(FinancialMonths.getInstance().getActiveMonth());
            dateEdit1.setDate(params.getBeginDate());
            dateEdit2.setDate(params.getEndDate());
        }
        doEnableDisable();
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            if (monthRadioButton.isSelected()) {
                FinancialMonth fm = (FinancialMonth) monthComboBox.getSelectedItem();
                params.setMonth(fm.getMonth(), fm.getYear());
            } else {
                //params.setPeriod((Date)dateEdit1.getValue(), (Date)dateEdit2.getValue());
                params.setPeriod(new Date(dateEdit1.getDate().getTime()), new Date(dateEdit2.getDate().getTime()));
            }
            return params;
        } else {
            return null;
        }
    }

    /**
     * Включает / выключает компоненты
     */
    private void doEnableDisable() {
        monthComboBox.setEnabled(monthRadioButton.isSelected());
        dateEdit1.setEnabled(periodRadioButton.isSelected());
        dateEdit2.setEnabled(periodRadioButton.isSelected());
    }

}
