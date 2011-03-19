package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.Manager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Список менеджеров
 */
public class ManagersCatalog extends CatalogTemplate {

    //
    ManagerParam managerParam;
    //
    /**
     * Признак того, что мы в процессе отображения формы чота меняли
     */
    private boolean isModifyed = false;


    ManagersCatalog(Frame owner) {
        super("Менеджеры", owner);
        table.setModel(new ManagersTableModel());
        //
        managerParam = new ManagerParam(dialog);
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
        java.util.List<Manager> managers = ((ManagersTableModel) table.getModel()).getManagers();
        if ((table.getSelectedRow() != -1) && (table.getSelectedRow() < managers.size())) {
            Manager manager = managers.get(table.getSelectedRow());
            Session sess = null;
            Transaction trx = null;
            try {
                sess = HibernateUtils.openSession();
                // Проверяем - а не числится ли за ним что-то?
                java.util.List res = sess.createQuery("select foId from FinancialOperation where manager.managerId=?").setInteger(0, manager.getManagerId()).list();
                if (res.size() != 0) {
                    // Значит удалять ничего НЕЛЬЗЯ!
                    JOptionPane.showMessageDialog(mainPanel, "Данный менеджер уже имеет выданные авансы или договоры. Удаление невозможно",
                            "Удаление менеджера", JOptionPane.ERROR_MESSAGE);
                    sess.close();
                    sess = null;
                    return;
                }
                if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить менеджера " + manager, "Удаление менеджера",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    trx = sess.beginTransaction();
                    sess.delete(manager);
                    sess.flush();
                    trx.commit();
                    trx = null;
                    isModifyed = true;
                    table.getSelectionModel().clearSelection();
                    loadData(sess);
                    sess.close();
                    sess = null;
                }
            } catch (HibernateException ignore) {
                if (trx != null) trx.rollback();
                if (sess != null) sess.close();
                JOptionPane.showMessageDialog(mainPanel, "Ошибка при удалении менеджера", "Удаление менеджера", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Вызывается при редактировании
     */
    @Override
    protected void doEdit() {
        java.util.List<Manager> managers = ((ManagersTableModel) table.getModel()).getManagers();
        if ((table.getSelectedRow() != -1) && (table.getSelectedRow() < managers.size())) {
            Manager manager = managers.get(table.getSelectedRow());
            manager = managerParam.doEdit(manager, mainPanel);
            if (manager != null) {
                saveManagerToDB(manager, "Ошибка сохранения менеджера", 1);
            }
        }
    }

    /**
     * Вызывается при добавлении
     */
    @Override
    protected void doAdd() {
        Manager manager = new Manager();
        manager = managerParam.doEdit(manager, mainPanel);
        if (manager != null) {
            // Значит его тут надо сохранять
            saveManagerToDB(manager, "Ошибка добавления менеджера", 0);
        }
    }

    /**
     * Отображение формы
     *
     * @param component Относительно чего размещать
     * @return true - если что-то редактировалось, false - если нет
     */
    public boolean show(Component component) {
        isModifyed = false;
        super.showCatalog(component);    //To change body of overridden methods use File | Settings | File Templates.
        return isModifyed;
    }

    /**
     * Загружает данные в table model
     * При загрузке должно сохраняться выделение в model
     *
     * @param session Сессия для загрузки. Если null - будет создана унутре
     */
    @Override
    protected void loadData(Session session) {
        int storedManagerId = -1;
        if (table.getSelectedRow() != -1) {
            storedManagerId = ((ManagersTableModel) table.getModel()).getManagers().get(table.getSelectedRow()).getManagerId();
        }
        // Получаем список менеджеров
        Session sess = null;
        if (session != null) {
            sess = session;
        }
        try {
            if (session != null) sess = session;
            else sess = HibernateUtils.openSession();
            java.util.List<Manager> managers = sess.createQuery("from Manager order by dismissed desc, FIO").list();
            ((ManagersTableModel) table.getModel()).setManagersList(managers);
            if (storedManagerId != -1) {
                // Восстановим выделение
                for (int i = 0; i < managers.size(); i++) {
                    if (managers.get(i).getManagerId() == storedManagerId) {
                        table.getSelectionModel().setSelectionInterval(i, i);
                        break;
                    }
                }
            }
        } finally {
            if ((session == null) && (sess != null)) sess.close();
        }
    }

    /**
     * Сохранение менеджера в базу
     *
     * @param manager   Менеджер для сохранения
     * @param errMsg    Сообщение об ошибке, которое будет выдано при ошибке сохранения
     * @param operation Тип операции: 0 - добавление, 1 - обновление
     */
    private void saveManagerToDB(Manager manager, String errMsg, int operation) {
        if (manager == null) return;
        Session sess = null;
        Transaction trx = null;
        String errTitle = "Добавление менеджера";
        try {
            sess = HibernateUtils.openSession();
            trx = sess.beginTransaction();
            switch (operation) {
                case 0: {
                    sess.save(manager);
                    break;
                }
                case 1: {
                    sess.update(manager);
                    errTitle = "Изменение параметров менеджера";
                    break;
                }
            }
            sess.flush();
            trx.commit();
            trx = null;
            isModifyed = true;
            // Перезагружаем данные в модель
            loadData(sess);
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            JOptionPane.showMessageDialog(mainPanel, errMsg, errTitle, JOptionPane.ERROR_MESSAGE);
        }
    }


    private class ManagersTableModel extends AbstractTableModel {

        private final int colCount = Manager.getValuesCount();

        public java.util.List<Manager> getManagers() {
            return managers;
        }

        private java.util.List<Manager> managers = new ArrayList<Manager>();

        public void setManagersList(java.util.List<Manager> list) {
            managers = list;
            fireTableDataChanged();
        }

        /**
         * Returns the number of rows in the model. A
         * <code>JTable</code> uses this method to determine how many rows it
         * should display.  This method should be quick, as it
         * is called frequently during rendering.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return managers.size();
        }

        /**
         * Returns the number of columns in the model. A
         * <code>JTable</code> uses this method to determine how many columns it
         * should create and display by default.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return colCount;
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
            if ((rowIndex < managers.size()) && (columnIndex < colCount)) {
                return managers.get(rowIndex).getValueByIndex(columnIndex);
            } else {
                return null;
            }
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
            if (column < colCount) {
                return Manager.getValueNameByIndex(column);
            } else {
                return null;
            }
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex < colCount) {
                return Manager.getValueClassByIndex(columnIndex);
            } else {
                return null;
            }
        }

        /**
         * Returns false.  This is the default implementation for all cells.
         *
         * @param rowIndex    the row being queried
         * @param columnIndex the column being queried
         * @return false
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }
}
