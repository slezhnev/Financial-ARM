package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

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
    private JTable accessTable;

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
     * @param databases         Список баз
     * @return Информация о пользователе или null, если выход без сохранения
     */
    public UsersCatalog.UserStorage doEdit(UsersCatalog.UserStorage user, Component positionComponent,
                                           java.util.List<String> databases) {
        java.util.List<CheckModel.AccessToDB> accessToBases = new ArrayList<CheckModel.AccessToDB>();
        //
        if (user == null) {
            userNameEdit.setText("");
            userGroupCombo.setSelectedIndex(0);
            for (String db : databases) {
                accessToBases.add(new CheckModel.AccessToDB(db, false));
            }
        } else {
            userNameEdit.setText(user.userName);
            userNameEdit.setEnabled(false);
            accessTable.setEnabled(false);
            for (String db : databases) {
                accessToBases.add(new CheckModel.AccessToDB(db, user.accessRoles.contains(db)));
            }
            if (user.userRole.equals("armDirectors")) {
                userGroupCombo.setSelectedIndex(1);
                accessToBases.clear();
                for (String db : databases) {
                    accessToBases.add(new CheckModel.AccessToDB(db, true));
                }
            } else if (user.userRole.equals("armUsers")) {
                userGroupCombo.setSelectedIndex(0);
            } else {
                userGroupCombo.setSelectedIndex(2);
            }
            userGroupCombo.setEnabled(false);
        }
        //
        accessTable.setModel(new CheckModel(accessToBases));
        //
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
                    sess.createSQLQuery("CREATE ROLE " + userNameEdit.getText() + " LOGIN CREATEROLE PASSWORD '" + new String(passwordField1.getPassword()) + "'").executeUpdate();
                    java.util.List<String> userAccess = new ArrayList<String>();
                    if (userGroupCombo.getSelectedIndex() == 1) {
                        sess.createSQLQuery("GRANT \"armDirectors\" TO " + userNameEdit.getText()).executeUpdate();
                        // Тут достут до БД открывать не надо - он открывается при создании БД
                    } else {
                        if (userGroupCombo.getSelectedIndex() == 0) {
                            sess.createSQLQuery("GRANT \"armUsers\" TO " + userNameEdit.getText()).executeUpdate();
                        } else {
                            sess.createSQLQuery("GRANT \"armViewers\" TO " + userNameEdit.getText()).executeUpdate();
                        }
                        //  А теперь еще откроем доступ до нужной БД!
                        for (CheckModel.AccessToDB access : accessToBases) {
                            if (access.allowAccess) {
                                sess.createSQLQuery("GRANT \"" + access.db + "_users\" TO " + userNameEdit.getText())
                                        .executeUpdate();
                                userAccess.add(access.db);
                            }
                        }
                    }
                    //
                    sess.flush();
                    trx.commit();
                    trx = null;
                    sess.close();
                    sess = null;
                    if (userGroupCombo.getSelectedIndex() == 1) {
                        return new UsersCatalog.UserStorage(userNameEdit.getText(), "armDirectors", userAccess);
                    } else if (userGroupCombo.getSelectedIndex() == 0) {
                        return new UsersCatalog.UserStorage(userNameEdit.getText(), "armUsers", userAccess);
                    } else {
                        return new UsersCatalog.UserStorage(userNameEdit.getText(), "armViewers", userAccess);
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

    /**
     * From http://stackoverflow.com/questions/21358240/how-to-implement-checkbox-list-java
     */
    private static class CheckModel extends AbstractTableModel {

        /**
         * Вспомогательный класс - сторадж для прав доступа и для отображения
         */
        private static class AccessToDB {

            String db;
            Boolean allowAccess;

            private AccessToDB(String db, Boolean allowAccess) {
                this.db = db;
                this.allowAccess = allowAccess;
            }
        }


        java.util.List<AccessToDB> rowList;

        public CheckModel(java.util.List<AccessToDB> rowList) {
            this.rowList = rowList;
        }

        @Override
        public int getRowCount() {
            return rowList.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int col) {
            return "";
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return rowList.get(row).allowAccess;
            } else {
                return rowList.get(row).db;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            boolean b = (Boolean) aValue;
            rowList.get(row).allowAccess = (Boolean) aValue;
            fireTableRowsUpdated(row, row);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 0) return Boolean.class;
            else return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col == 0);
        }
    }

}
