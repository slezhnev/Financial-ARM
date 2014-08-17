package ru.lsv.finARM.ui;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.Manager;
import ru.lsv.finARM.mappings.SpendingTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Редактирование параметров шаблона трат
 */
public class SpendingTemplateParam {

    private JDialog dialog;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JPanel mainPanel;
    private JTextField spendNameEdit;
    private JFormattedTextField spendAmountEdit;

    Integer spId;
    boolean modalResult = false;

    public SpendingTemplateParam(JDialog owner) {
        dialog = new JDialog(owner, "Параметры шаблона расхода");
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
     * Обработка закрытия по "Сохранить" или Enter'у
     */
    private void doNormalClose() {
        // Проверяем - а сохранять-то мы вообще можем? Может там фигня какая навведена?
        if (spendNameEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Должно быть задано наименование расхода", "Параметры шаблона расхода", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Прежде чем что-то там сохранять - надо проверить, а может быть уже есть менеджер с такой ФИО?
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            //
            Criteria crit = sess.createCriteria(SpendingTemplate.class);
            crit.add(Restrictions.eq("spendName", spendNameEdit.getText()));
            if (spId != null) crit.add(Restrictions.ne("stId", spId));
            if (crit.uniqueResult() != null) {
                // Значит есть уже такой манагер
                JOptionPane.showMessageDialog(null, "Шаблон расхода с таким наименованием уже существует. Измените наименование расхода", "Параметры шаблона расхода", JOptionPane.WARNING_MESSAGE);
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
     * Обработка закрытия редактирования
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    public SpendingTemplate doEdit(SpendingTemplate spTemplate, Component positionComponent) {
        spId = spTemplate.getStId();
        spendNameEdit.setText(spTemplate.getSpendName());
        spendAmountEdit.setValue(spTemplate.getSpendAmount());
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            spTemplate.setSpendName(spendNameEdit.getText());
            spTemplate.setSpendAmount((Double) spendAmountEdit.getValue());
            return spTemplate;
        } else {
            return null;
        }
    }

}
