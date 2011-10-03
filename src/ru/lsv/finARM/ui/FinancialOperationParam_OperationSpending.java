package ru.lsv.finARM.ui;

import com.jidesoft.swing.AutoCompletion;
import com.toedter.calendar.JDateChooser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.common.UserRoles;
import ru.lsv.finARM.mappings.Spending;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.util.Set;

/**
 * Параметры расхода по договору
 */
public class FinancialOperationParam_OperationSpending {
    private JButton saveBtn;
    private JButton cancelBtn;
    private JPanel mainPanel;
    private JComboBox payerToComboBox;
    private JTextField orderNumEdit;
    private JFormattedTextField sumEdit;
    private JComboBox paymentTypeComboBox;
    private JDateChooser dateEdit;
    private JTextField commentEdit;
    private JFormattedTextField salarySumEdit;

    private JDialog dialog;

    private boolean modalResult = false;

    Set<Spending> spendings = null;
    Double totalSum = null;

    public FinancialOperationParam_OperationSpending(JDialog owner) {
        dialog = new JDialog(owner, "Параметры расхода по договору");
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

        sumEdit.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (salarySumEdit.isEnabled())
                    salarySumEdit.setValue(sumEdit.getValue());
            }
        });
        //
        new AutoCompletion(payerToComboBox);
        //
    }

    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((payerToComboBox.getSelectedItem() == null) || ((String) payerToComboBox.getSelectedItem()).trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не выбран поставщик", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (orderNumEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не указан номер счета", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
        /*if (((Double) sumEdit.getValue()) <= 0) {
            JOptionPane.showMessageDialog(null, "Неверно задана сумма", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }*/
        // Проверяем - а не пытаемся ли мы засунуть дубля?!
        Spending spend = reload(new Spending());
        if (spendings.contains(spend)) {
            JOptionPane.showMessageDialog(null, "Вы пытаетесь сохранить расход, полностью повторяющий уже существующий", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Проверим еще - а не превысили ли мы сумму договора?
        // Отключаем нафиг по запросу. Мне-то чего...
        /*double sum = 0;
        for (Spending tmpSp : spendings) {
            sum = sum + tmpSp.getPaymentSum();
        }
        if ((sum + spend.getPaymentSum()) > totalSum) {
            JOptionPane.showMessageDialog(null, "Общая сумма расходов превышает сумму договора", "Параметры расхода по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }*/
        //
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
     * Редактирование расхода для договора
     *
     * @param spend             Расход
     * @param spendings         Список расходов в договоре. Надо для провеки введения дублей
     * @param sum               Общая сумма договора
     * @param positionComponent Компонента для позиционирования
     * @param userRole
     * @return Скорректированный расход
     */
    public Spending doEdit(Spending spend, Set<Spending> spendings, Double sum, boolean isClosedForSalary, Component positionComponent, UserRoles userRole) {
        // Сохраняем и удаляем нафиг
        this.spendings = spendings;
        totalSum = sum;
        // Вначале - загрузим список поставщиков
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            //
            java.util.List<String> payers = sess.createQuery("select DISTINCT payerTo from Spending order by payerTo").list();
            payerToComboBox.setModel(new DefaultComboBoxModel(payers.toArray()));
            payerToComboBox.setSelectedItem(spend.getPayerTo());
            //
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
        orderNumEdit.setText(spend.getOrderNum());
        sumEdit.setValue(spend.getPaymentSum());
        if (spend.getPaymentSalarySum() != null) {
            salarySumEdit.setValue(spend.getPaymentSalarySum());
        }
        if (isClosedForSalary) salarySumEdit.setEnabled(false);
        paymentTypeComboBox.setSelectedIndex(spend.getPaymentType());
        dateEdit.setDate(spend.getPaymentDate());
        commentEdit.setText(spend.getComment());
        //
        dateEdit.setEnabled(userRole == UserRoles.DIRECTOR);
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            spend = reload(spend);
            return spend;
        } else {
            return null;
        }
    }

    /**
     * Обновляет информацию и расходе из данных формы
     *
     * @param spend Исходные данные
     * @return Измененные расход
     */
    private Spending reload(Spending spend) {
        spend.setPayerTo((String) payerToComboBox.getSelectedItem());
        spend.setOrderNum(orderNumEdit.getText());
        spend.setPaymentSum((Double) sumEdit.getValue());
        spend.setPaymentSalarySum((Double) salarySumEdit.getValue());
        spend.setPaymentType(paymentTypeComboBox.getSelectedIndex());
        spend.setPaymentDate(new Date(dateEdit.getDate().getTime()));
        spend.setComment(commentEdit.getText());
        return spend;
    }

}
