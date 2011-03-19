package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Параметры пользователя
 */
public class UserParam {

    private JDialog dialog;

    private boolean modalResult = false;

    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JTextField userNameEdit;
    private JComboBox userGroupCombo;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;

    public UserParam(JDialog owner) {
        dialog = new JDialog(owner, "Параметры пользователя");
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
     * Нормальное закрытие
     */
    private void doNormalClose() {
        if (userNameEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(mainPanel, "Не задано имя пользователя", "Параметры пользователя", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("user".equals(userNameEdit.getText())) {
            JOptionPane.showMessageDialog(mainPanel, "Пользователь не может иметь имя 'user'.", "Параметры пользователя", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((new String(passwordField1.getPassword()).trim().length() == 0) ||
                (new String(passwordField2.getPassword()).trim().length() == 0)) {
            JOptionPane.showMessageDialog(mainPanel, "Не задан пароль", "Параметры пользователя", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!new String(passwordField1.getPassword()).equals(new String(passwordField2.getPassword()))) {
            JOptionPane.showMessageDialog(mainPanel, "Заданные пароли не совпадают", "Параметры пользователя", JOptionPane.ERROR_MESSAGE);
            return;
        }
        modalResult = true;
        dialog.setVisible(false);
    }

    /**
     * Закрытие по отмене
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    /**
     * Редактирование пользователя
     *
     * @param user              Пользователь для редактирования или null - если пользователь добавляется
     * @param positionComponent Компонента для позиционирования
     * @return Информация о пользователе или null, если выход без сохранения
     */
    public UsersCatalog.UserStorage doEdit(UsersCatalog.UserStorage user, Component positionComponent) {
        if (user == null) {
            userNameEdit.setText("");
            userGroupCombo.setSelectedIndex(0);
        } else {
            userNameEdit.setText(user.userName);
            userNameEdit.setEnabled(false);
            if (user.userRole.equals("armDirectors")) {
                userGroupCombo.setSelectedIndex(1);
            } else {
                userGroupCombo.setSelectedIndex(0);
            }
            userGroupCombo.setEnabled(false);
        }
        passwordField1.setText("");
        passwordField2.setText("");
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            Session sess = null;
            Transaction trx = null;
            try {
                sess = HibernateUtils.openSession();
                if (user == null) {
                    // Добавляем пользователя
                    trx = sess.beginTransaction();
                    if (userGroupCombo.getSelectedIndex() == 1) {
                        sess.createSQLQuery("CREATE ROLE " + userNameEdit.getText() + " LOGIN CREATEROLE PASSWORD '" + new String(passwordField1.getPassword()) + "'").executeUpdate();
                        sess.createSQLQuery("GRANT \"armDirectors\" TO "+ userNameEdit.getText()).executeUpdate();
                    } else {
                        sess.createSQLQuery("CREATE ROLE " + userNameEdit.getText() + " LOGIN PASSWORD '" + new String(passwordField1.getPassword()) + "'").executeUpdate();
                        sess.createSQLQuery("GRANT \"armUsers\" TO "+ userNameEdit.getText()).executeUpdate();
                    }
                    //
                    sess.flush();
                    trx.commit();
                    trx = null;
                    sess.close();
                    sess = null;
                    if (userGroupCombo.getSelectedIndex() == 1) {
                        return new UsersCatalog.UserStorage(userNameEdit.getText(), "armDirectors");
                    } else {
                        return new UsersCatalog.UserStorage(userNameEdit.getText(), "armUsers");                        
                    }
                } else {
                    // Меняем у пользователя пароль
                    trx = sess.beginTransaction();
                    sess.createSQLQuery("ALTER ROLE " + userNameEdit.getText() + " PASSWORD '" + new String(passwordField1.getPassword()) + "'").executeUpdate();
                    sess.flush();
                    trx.commit();
                    trx = null;
                    sess.close();
                    sess = null;
                    return user;
                }
            } catch (HibernateException ex) {
                if (trx != null) trx.rollback();
                if (sess != null) sess.close();
                JOptionPane.showMessageDialog(mainPanel, "При сохранении пользователя произошла ошибка. Обратитесь к своему системуному администратору",
                        "Сохранение пользователя", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            dialog.setVisible(false);
            return null;
        }
    }

}
