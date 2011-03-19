package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Форма коррекции пароля текущего пользователя
 */
public class ChangePassword {
    private JPanel mainPanel;
    private JButton okBtn;
    private JButton cancelBtn;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JDialog dialog;

    public ChangePassword(Frame owner) {
        dialog = new JDialog(owner, "Изменение пароля");
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
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обрабатываем нормально закрытие
                doNormalClose();
            }
        });

    }

    /**
     * Закрытие с сохранением
     */
    private void doNormalClose() {
        if ((new String(passwordField1.getPassword()).trim().length() == 0) ||
                (new String(passwordField2.getPassword()).trim().length() == 0)) {
            JOptionPane.showMessageDialog(mainPanel, "Пароль не задан", "Изменение пароля", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!new String(passwordField1.getPassword()).equals(new String(passwordField2.getPassword()))) {
            JOptionPane.showMessageDialog(mainPanel, "Введенные пароли не совпадают!", "Изменение пароля", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] msg = {"Вы уверены, что хотите изменить пароль?", "После изменения пароля работа с системой будет автоматически завершена."};
        if (JOptionPane.showConfirmDialog(mainPanel, msg, "Изменение пароля", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Вот тут уже будем изменять
            Session sess = null;
            Transaction trx = null;
            try {
                sess = HibernateUtils.openSession();
                String currentUser = (String) sess.createSQLQuery("select current_user").uniqueResult();
                trx = sess.beginTransaction();
                sess.createSQLQuery("ALTER ROLE "+currentUser+" PASSWORD '"+new String(passwordField1.getPassword())+"'").executeUpdate();
                sess.flush();
                trx.commit();
                trx = null;
                //
                sess.close();
                sess = null;
                System.exit(0);
            } catch (HibernateException ex) {
                if (trx != null) trx.rollback();
                if (sess != null) sess.close();
                JOptionPane.showMessageDialog(mainPanel, "При изменении пароля произошла ошибка. Обратитесь к своему системному администратору",
                        "Изменение пароля", JOptionPane.ERROR_MESSAGE);
                dialog.setVisible(false);
            }
        }
    }

    /**
     * Закрытие по отмене
     */
    private void doOnClosing() {
        dialog.setVisible(false);
    }

    public void doChange(Component positionComponent) {
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
    }

}
