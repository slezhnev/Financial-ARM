package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.Manager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;

/**
 * Редактирование параметров менеджера
 */
public class ManagerParam {

    JDialog dialog;
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JTextField FIOEdit;
    private JDateChooser incomeDateEdit;
    private JFormattedTextField subsidyEdit;
    private JFormattedTextField retensionEdit;
    private JFormattedTextField cashPercentEdit;
    private JFormattedTextField nonCashPercentEdit;
    private JCheckBox dismissedCB;
    private JDateChooser dismissDateEdit;
    private JFormattedTextField salaryEdit;
    private JRadioButton managerRB;
    private JRadioButton directorRB;
    //

    //
    private Integer managerId;
    //
    boolean modalResult;

    ManagerParam(Dialog owner) {
        dialog = new JDialog(owner, "Параметры менеджера");
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
        managerRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPercentageEnable();
            }
        });
        directorRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doPercentageEnable();
            }
        });
    }

    /**
     * Выставляет проценты и включает / выключает редактирование процентов...
     */
    private void doPercentageEnable() {
        if (directorRB.isSelected()) {
            cashPercentEdit.setValue(100.0);
            nonCashPercentEdit.setValue(100.0);
        }
        cashPercentEdit.setEnabled(!directorRB.isSelected());
        nonCashPercentEdit.setEnabled(!directorRB.isSelected());
    }

    /**
     * Обработка закрытия по кнопке "Сохранить" или Enter'у
     */
    private void doNormalClose() {
        // Проверяем - а сохранять-то мы вообще можем? Может там фигня какая навведена?
        if (incomeDateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Должна быть задана дата приема на работу", "Параметры менеджера", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (FIOEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Должна быть задана ФИО", "Параметры менеджера", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (((Double) cashPercentEdit.getValue() < 0) || ((Double) nonCashPercentEdit.getValue() < 0)) {
            JOptionPane.showMessageDialog(null, "Процент не может быть отрицательным", "Параметры менеджера", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Прежде чем что-то там сохранять - надо проверить, а может быть уже есть менеджер с такой ФИО?
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            //
            Criteria crit = sess.createCriteria(Manager.class);
            crit.add(Restrictions.eq("FIO", FIOEdit.getText()));
            if (managerId != null) crit.add(Restrictions.ne("managerId", managerId));
            if (crit.uniqueResult() != null) {
                // Значит есть уже такой манагер
                JOptionPane.showMessageDialog(null, "Менеджер с таким ФИО уже существует. Измените ФИО для сохранения.", "Параметры менеджера", JOptionPane.WARNING_MESSAGE);
                sess.close();
                return;
            }
            //
            sess.close();
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
        modalResult = true;
        dialog.setVisible(false);
    }

    /**
     * Обработка закрытия формы
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    /**
     * Редактирование параметроа менеджера
     *
     * @param manager           Менеджер для редактирования
     * @param positionComponent Относительно чего делать setPositionRelativeTo
     * @return Измененный manager или null, если редактирование было отменено
     */
    public Manager doEdit(Manager manager, Component positionComponent) {
        //
        FIOEdit.setText(manager.getFIO());
        incomeDateEdit.setDate(manager.getIncomeDate());
        subsidyEdit.setValue(manager.getSubsidy());
        retensionEdit.setValue(manager.getRetention());
        cashPercentEdit.setValue(manager.getCashPercent());
        nonCashPercentEdit.setValue(manager.getNonCashPercent());
        if (manager.getCashPercent() == 100) {
            directorRB.setSelected(true);
        } else {
            managerRB.setSelected(true);
        }
        doPercentageEnable();
        dismissedCB.setSelected(manager.isDismissed());
        dismissDateEdit.setDate(manager.getDismissDate());
        if (manager.getSalary() == null) salaryEdit.setValue(0.0);
        else salaryEdit.setValue(manager.getSalary());
        //
        managerId = manager.getManagerId();
        //
        modalResult = false;
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            manager.setFIO(FIOEdit.getText());
            manager.setIncomeDate(new Date(incomeDateEdit.getDate().getTime()));
            manager.setSubsidy((Double) subsidyEdit.getValue());
            manager.setRetention((Double) retensionEdit.getValue());
            manager.setCashPercent((Double) cashPercentEdit.getValue());
            manager.setNonCashPercent((Double) nonCashPercentEdit.getValue());
            manager.setDismissed(dismissedCB.isSelected());
            manager.setDismissDate(dismissDateEdit.getDate() == null ? null : new Date(dismissDateEdit.getDate().getTime()));
            manager.setSalary((Double) salaryEdit.getValue());
            return manager;
        } else {
            return null;
        }
    }

}
