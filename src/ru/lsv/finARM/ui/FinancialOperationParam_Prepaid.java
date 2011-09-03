package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.Manager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.List;

/**
 * Парамеры финансовой операции - аванс
 */
public class FinancialOperationParam_Prepaid {

    private JDialog dialog;

    private boolean modalResult = false;
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JComboBox managerComboBox;
    private JFormattedTextField amountEdit;
    private JDateChooser dateEdit;

    public FinancialOperationParam_Prepaid(Frame owner) {
        dialog = new JDialog(owner, "Параметры аванса");
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
        // Заполняем список менеджеров
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            List<Manager> managers = sess.createQuery("from Manager where (dismissed=false) order by FIO").list();
            managerComboBox.setModel(new DefaultComboBoxModel(managers.toArray()));
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
    }

    /**
     * Обработка закрытия с сохранением
     */
    private void doNormalClose() {
        if (!saveBtn.isEnabled()) return;
        // Делаем дополнительные проверки
        if (managerComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Не выбран менеджер", "Параметры аванса", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((amountEdit.getValue() == null)||((Double)amountEdit.getValue()) == 0) {
            JOptionPane.showMessageDialog(null, "Сумма аванса не может быть нулевой", "Параметры аванса", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата аванса", "Параметры аванса", JOptionPane.ERROR_MESSAGE);
            return;
        }
        modalResult = true;
        dialog.setVisible(false);
    }

    /**
     * Обработа закрытия без сохранения
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    /**
     * Редактирование аванса
     * @param fo Запись о авансе
     * @param positionComponent Компонента, относительно которой все будет позиционироваться
     * @param allowSave Разрешено ли сохранять текущую запись
     * @return Измененная запись о авансе
     */
    public FinancialOperation doEdit(FinancialOperation fo, Component positionComponent, boolean allowSave) {
        managerComboBox.setSelectedItem(fo.getManager());
        amountEdit.setValue(fo.getOperationSum());
        dateEdit.setDate(fo.getOperationDate());
        if (!allowSave) {
            CommonUtils.disableComponents(dialog);
            cancelBtn.setEnabled(true);
        }
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            fo.setManager((Manager)managerComboBox.getSelectedItem());
            fo.setOperationSum((Double)amountEdit.getValue());
            fo.setOperationDate(new Date(dateEdit.getDate().getTime()));
            return fo;
        } else {
            return null;
        }
    }

}
