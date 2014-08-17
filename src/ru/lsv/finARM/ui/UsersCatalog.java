package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Список пользователей
 */
public class UsersCatalog extends CatalogTemplate {

    private String currentUser = null;
    private List<String> databases = null;

    /**
     * Создает форму
     *
     * @param title Не используется
     * @param owner Owner
     */
    UsersCatalog(String title, Frame owner) {
        super("Пользователи", owner);
        addBtn.setToolTipText("Добавить пользователя");
        editBtn.setToolTipText("Параметры пользователя");
        delBtn.setToolTipText("Удалить пользователя");
        //
        table.setModel(new UsersTableModel());
    }

    /**
     * Вызывается при закрытии формы
     */
    @Override
    protected void doClose() {
        dialog.setVisible(false);
    }

    /**
     * Вызывается при удалении
     */
    @Override
    protected void doDelete() {
        if (table.getSelectedRow() != -1) {
            UserStorage user = ((UsersTableModel) table.getModel()).getUsers().get(table.getSelectedRow());
            if (user.userName.equals(currentUser)) {
                String[] msg = {"Удаление пользователя, под которым вы вошли в систему, невозможно.",
                        "Зайдите в систему под другим именем пользователя для удаления этого пользователя."};
                JOptionPane.showMessageDialog(mainPanel, msg, "Удаление пользователя", JOptionPane.WARNING_MESSAGE);
            } else {
                if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить пользователя '" + user.userName + "'?",
                        "Удаление пользователя", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Session sess = null;
                    Transaction trx = null;
                    try {
                        sess = HibernateUtils.openSession();
                        trx = sess.beginTransaction();
                        sess.createSQLQuery("DROP ROLE " + user.userName).executeUpdate();
                        //
                        sess.flush();
                        trx.commit();
                        trx = null;
                        sess.close();
                        sess = null;
                        ((UsersTableModel) table.getModel()).getUsers().remove(user);
                        ((UsersTableModel) table.getModel()).fireTableDataChanged();
                    } catch (HibernateException ex) {
                        if (trx != null) trx.rollback();
                        if (sess != null) sess.close();
                        JOptionPane.showMessageDialog(mainPanel, "При удалении пользователя произошла ошибка. Обратитесь к своему системуному администратору",
                                "Удаление пользователя", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Вызывается при редактировании
     */
    @Override
    protected void doEdit() {
        if (table.getSelectedRow() != -1) {
            UserStorage user = ((UsersTableModel) table.getModel()).getUsers().get(table.getSelectedRow());
            if (user.userName.equals(currentUser)) {
                String[] msg = {"Редактирование параметров пользователя, под которым вы вошли в систему, невозможно.",
                        "Зайдите в систему под другим именем пользователя для редактирования этого пользователя."};
                JOptionPane.showMessageDialog(mainPanel, msg, "Параметры пользователя", JOptionPane.WARNING_MESSAGE);
            } else {
                new UserParam(dialog).doEdit(user, mainPanel, databases);
            }
        }
    }

    /**
     * Вызывается при добавлении
     */
    @Override
    protected void doAdd() {
        UserParam up = new UserParam(dialog);
        UserStorage user = up.doEdit(null, mainPanel, databases);
        if (user != null) {
            ((UsersTableModel) table.getModel()).getUsers().add(user);
            ((UsersTableModel) table.getModel()).fireTableDataChanged();
        }
    }

    /**
     * Загружает данные в table model
     * При загрузке должно сохраняться выделение в model
     *
     * @param session Сессия для загрузки. Если null - будет создана унутре
     */
    @Override
    protected void loadData(Session session) {
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            currentUser = (String) sess.createSQLQuery("select current_user").uniqueResult();
            // Получаем список БД
            databases = new ArrayList<String>();
            List dbs = sess.createSQLQuery("SELECT datname FROM pg_database WHERE datistemplate = false").list();
            for (Object db : dbs) {
                if (!"postgres".equals(db))
                    databases.add((String) db);
            }
            //
            List users = sess.createSQLQuery("select f.rolname, d.groname from pg_roles f " +
                    "left join pg_group d " +
                    "on f.oid = ANY(d.grolist) " +
                    "where f.rolcanlogin=true and f.rolsuper=false " +
                    "order by f.rolname").list();
            ArrayList<UserStorage> allUsers = new ArrayList<UserStorage>();
            List<String> accessRoles = null;
            String processingUser = null;
            String processingUserRole = null;
            for (Object user : users) {
                Object[] currUser = (Object[]) user;
                if (!((String) currUser[0]).equals(processingUser)) {
                    // Новый пользователь
                    if (processingUser != null) {
                        allUsers.add(new UserStorage(processingUser, processingUserRole, accessRoles));
                    }
                    processingUser = (String) currUser[0];
                    processingUserRole = null;
                    accessRoles = new ArrayList<String>();
                }
                if ("armDirectors".equals(currUser[1]) || "armUsers".equals(currUser[1]) || "armViewers".equals
                        (currUser[1])) {
                    processingUserRole = (String) currUser[1];
                } else {
                    // Проверяем - может это роль доступа к БД?
                    for (String db : databases) {
                        if (((String) currUser[1]).toLowerCase().startsWith(db.toLowerCase())) {
                            accessRoles.add(db);
                        }
                    }
                }
            }
            if (processingUser != null) {
                allUsers.add(new UserStorage(processingUser, processingUserRole, accessRoles));
            }
            ((UsersTableModel) table.getModel()).setUsers(allUsers);
            ((UsersTableModel) table.getModel()).fireTableDataChanged();
            //
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
    }

    /**
     * Storage для пользователя
     */
    public static class UserStorage {
        public UserStorage(String userName, String userRole, List<String> accessRoles) {
            if (userName == null) this.userName = "";
            else this.userName = userName;
            if (userRole == null) this.userRole = "";
            else this.userRole = userRole;
            if (accessRoles == null) {
                this.accessRoles = new ArrayList<String>();
            } else {
                this.accessRoles = accessRoles;
            }
        }

        String userName;

        String userRole;

        List<String> accessRoles;
    }

    private class UsersTableModel extends AbstractTableModel {

        private java.util.List<UserStorage> users = new ArrayList<UserStorage>();

        /**
         * Returns the number of rows in the model.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return users.size();
        }

        /**
         * Returns the number of columns in the model.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         *
         * @param rowIndex    the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if ((rowIndex < users.size()) && (columnIndex < 2)) {
                if (columnIndex == 0) return users.get(rowIndex).userName;
                else if (columnIndex == 1) {
                    if (users.get(rowIndex).userRole.equals("armDirectors")) {
                        return "Директора";
                    } else if (users.get(rowIndex).userRole.equals("armUsers")) {
                        return "Пользователи";
                    } else if (users.get(rowIndex).userRole.equals("armViewers")) {
                        return "Только просмотр";
                    } else {
                        return "ОШИБКА";
                    }
                } else {
                    StringBuffer tmp = new StringBuffer();
                    for (String role : users.get(rowIndex).accessRoles) {
                        if (tmp.length() != 0) {
                            tmp.append(" / ");
                        }
                        tmp.append(role);
                    }
                    return tmp.toString();
                }
            } else
                return null;
        }

        /**
         * Returns a default name for the column using spreadsheet conventions:
         * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
         * returns an empty string.
         *
         * @param column the column being queried
         * @return a string containing the default name of <code>column</code>
         */
        @Override
        public String getColumnName(int column) {
            if (column == 0) return "Имя пользователя";
            else if (column == 1) return "Группа";
            else return "Доступ";
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public java.util.List<UserStorage> getUsers() {
            return users;
        }

        public void setUsers(List<UserStorage> users) {
            this.users = users;
        }
    }

}
