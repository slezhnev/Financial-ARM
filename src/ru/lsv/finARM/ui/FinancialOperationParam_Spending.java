package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.MonthSpending;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

/**
 * Финансования операция - расходы
 */
public class FinancialOperationParam_Spending {
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JFormattedTextField amountEdit;
    private JDateChooser dateEdit;
    private JRadioButton plannedSpendingRB;
    private JRadioButton nonPlannedSpendingRB;
    private JComboBox plannedSpendingComboBox;
    private JTextField nonPlannedSpendinEdit;
    private JComboBox paymentTypeComboBox;

    private JDialog dialog;

    private boolean modalResult = false;


    public FinancialOperationParam_Spending(Frame owner) {
        dialog = new JDialog(owner, "Параметры расхода");
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
        // Заполняем список расходов
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            java.util.List<MonthSpending> spendings = sess.createQuery("from MonthSpending where (month=? AND year=?) order by name")
                    .setInteger(0, FinancialMonths.getInstance().getActiveMonth().getMonth())
                    .setInteger(1, FinancialMonths.getInstance().getActiveMonth().getYear())
                    .list();
            plannedSpendingComboBox.setModel(new DefaultComboBoxModel(spendings.toArray()));
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
        plannedSpendingRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSpendingsEnable();
            }
        });
        nonPlannedSpendingRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSpendingsEnable();
            }
        });
    }

    /**
     * Обработка закрытия с сохранением
     */
    private void doNormalClose() {
        if (!saveBtn.isEnabled()) return;
        //
        if (plannedSpendingRB.isSelected() && (plannedSpendingComboBox.getSelectedItem() == null)) {
            JOptionPane.showMessageDialog(null, "Не выбран расход", "Параметры расхода", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nonPlannedSpendingRB.isSelected() && (nonPlannedSpendinEdit.getText().trim().length() == 0)) {
            JOptionPane.showMessageDialog(null, "Не введено наименование расхода", "Параметры расхода", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((amountEdit.getValue() == null)||((Double)amountEdit.getValue()) == 0) {
            JOptionPane.showMessageDialog(null, "Сумма расхода не может быть нулевой", "Параметры расхода", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата расхода", "Параметры расхода", JOptionPane.ERROR_MESSAGE);
            return;
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
     * Редактирование информации о расходе
     *
     * @param fo                Расход
     * @param positionComponent Относительно чего позиционироваться
     * @param allowSave Разрешать ли сохранять текущую запись
     * @return Скорректированный расход
     */
    public FinancialOperation doEdit(FinancialOperation fo, Component positionComponent, boolean allowSave) {
        if (fo.getPlannedSpending() != null) {
            plannedSpendingRB.setSelected(true);
            plannedSpendingComboBox.setSelectedItem(fo.getPlannedSpending());
        } else {
            nonPlannedSpendingRB.setSelected(true);            
            nonPlannedSpendinEdit.setText(fo.getNonPlannedSpending());
        }
        amountEdit.setValue(fo.getOperationSum());
        dateEdit.setDate(fo.getOperationDate());
        paymentTypeComboBox.setSelectedIndex(fo.getPaymentType());
        doSpendingsEnable();
        if (!allowSave) {
            CommonUtils.disableComponents(dialog);
            cancelBtn.setEnabled(true);
        }
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            if (plannedSpendingRB.isSelected()) {
                fo.setPlannedSpending((MonthSpending) plannedSpendingComboBox.getSelectedItem());
                fo.setNonPlannedSpending(null);
            } else {
                fo.setPlannedSpending(null);
                fo.setNonPlannedSpending(nonPlannedSpendinEdit.getText());
            }
            fo.setOperationSum((Double) amountEdit.getValue());
            fo.setOperationDate(new java.sql.Date(dateEdit.getDate().getTime()));
            fo.setPaymentType(paymentTypeComboBox.getSelectedIndex());
            return fo;
        } else {
            return null;
        }
    }


    /**
     * Включение / выключение контролов
     */
    private void doSpendingsEnable() {
        plannedSpendingComboBox.setEnabled(plannedSpendingRB.isSelected());
        nonPlannedSpendinEdit.setEnabled(nonPlannedSpendingRB.isSelected());
    }
}
