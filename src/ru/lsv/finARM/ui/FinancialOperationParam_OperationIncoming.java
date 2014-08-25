package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import ru.lsv.finARM.common.UserRoles;
import ru.lsv.finARM.mappings.Incoming;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;

/**
 * Параметр прихода
 */
public class FinancialOperationParam_OperationIncoming {

    private JDialog dialog;

    private boolean modalResult = false;
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JFormattedTextField sumEdit;
    private JTextField commentEdit;
    private JDateChooser dateEdit;

    public FinancialOperationParam_OperationIncoming(JDialog owner) {
        dialog = new JDialog(owner, "Параметры поступления по договору");
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

    }

    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        //
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата", "Параметры поступления по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (((Double) sumEdit.getValue()) < 0) {
            JOptionPane.showMessageDialog(null, "Сумма поступления не может быть отрицательной", "Параметры поступления по договору", JOptionPane.ERROR_MESSAGE);
            return;
        }
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


    public Incoming doEdit(Incoming inc, Component positionComponent, UserRoles userRole) {
        //
        dateEdit.setDate(inc.getIncomingDate());
        sumEdit.setValue(inc.getIncomingSum());
        commentEdit.setText(inc.getIncomingComment());
        //
        dateEdit.setEnabled(userRole == UserRoles.DIRECTOR);
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            return makeIncoming(inc);
        } else {
            return null;
        }

    }

    /**
     * Создает объект из формы
     *
     * @param inc Из чего создавать
     * @return Объект
     */
    private Incoming makeIncoming(Incoming inc) {
        inc.setIncomingDate(new Date(dateEdit.getDate().getTime()));
        inc.setIncomingSum((Double) sumEdit.getValue());
        inc.setIncomingComment(commentEdit.getText());
        return inc;
    }

}
