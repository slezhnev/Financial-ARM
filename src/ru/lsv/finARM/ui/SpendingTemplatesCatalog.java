package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.SpendingTemplate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Шаблон месячных трат
 */
public class SpendingTemplatesCatalog extends CatalogTemplate {

    private SpendingTemplateParam spendingTemplateParam;

    SpendingTemplatesCatalog(Frame owner) {
        super("Шаблон месячных трат", owner);
        table.setModel(new SpendingTableModel());
        //
        addBtn.setToolTipText("Добавить шаблон планируемого расхода");
        editBtn.setToolTipText("Изменить шаблон планируемого расхода");
        delBtn.setToolTipText("Удалить шаблон планируемого расхода");
        //
        spendingTemplateParam = new SpendingTemplateParam(dialog);
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
        java.util.List<SpendingTemplate> spends = ((SpendingTableModel) table.getModel()).getTemplates();
        if ((table.getSelectedRow() > -1) && (table.getSelectedRow() < spends.size())) {
            SpendingTemplate spTemplate = spends.get(table.getSelectedRow());
            if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить элемент шаблона \"" + spTemplate + "\"?",
                    "Удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Session sess = null;
                Transaction trx = null;
                try {
                    sess = HibernateUtils.openSession();
                    trx = sess.beginTransaction();
                    sess.delete(spTemplate);
                    sess.flush();
                    trx.commit();
                    trx = null;
                    table.getSelectionModel().clearSelection();
                    loadData(sess);
                    sess.close();
                    sess = null;
                } catch (HibernateException ex) {
                    if (trx != null) trx.rollback();
                    if (sess != null) sess.close();
                    JOptionPane.showMessageDialog(mainPanel, "Ошибка удаление элемента шаблона", "Удаление", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Вызывается при редактировании
     */
    @Override
    protected void doEdit() {
        java.util.List<SpendingTemplate> spends = ((SpendingTableModel) table.getModel()).getTemplates();
        if ((table.getSelectedRow() > -1) && (table.getSelectedRow() < spends.size())) {
            SpendingTemplate spTemplate = spends.get(table.getSelectedRow());
            spTemplate = spendingTemplateParam.doEdit(spTemplate, mainPanel);
            if (spTemplate != null) {
                saveSpendingTemplateToDB(spTemplate, "Ошибка сохранения элемента шаблона", 1);
            }
        }
    }

    /**
     * Вызывается при добавлении
     */
    @Override
    protected void doAdd() {
        SpendingTemplate spTemplate = new SpendingTemplate();
        spTemplate = spendingTemplateParam.doEdit(spTemplate, mainPanel);
        if (spTemplate != null) {
            // Тут надо сохранять
            saveSpendingTemplateToDB(spTemplate, "Ошибка добавления элемента шаблона", 0);
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
        int savedStId = -1;
        java.util.List<SpendingTemplate> spends = ((SpendingTableModel) table.getModel()).getTemplates();
        if ((table.getSelectedRow() != -1) && (table.getSelectedRow() < spends.size())) {
            savedStId = spends.get(table.getSelectedRow()).getStId();
        }
        Session sess = null;
        try {
            if (session == null) sess = HibernateUtils.openSession();
            else sess = session;
            spends = sess.createQuery("from SpendingTemplate order by spendAmount").list();
            ((SpendingTableModel) table.getModel()).setTemplates(spends);
            ((SpendingTableModel) table.getModel()).fireTableDataChanged();
            if (savedStId != -1) {
                // Востанновим выделение
                for (int i = 0; i < spends.size(); i++) {
                    if (spends.get(i).getStId() == savedStId) {
                        table.getSelectionModel().setSelectionInterval(i, i);
                        break;
                    }
                }
            }
        } finally {
            if ((sess != null) && (session == null)) sess.close();
        }
    }


    /**
     * Сохраняет или апдейтит в базу
     *
     * @param spTemplate Что сохранять
     * @param errMsg     Сообщение об ошибке, которое будет выдано
     * @param operation  0 - добавление, 1 - редактирование
     */
    private void saveSpendingTemplateToDB(SpendingTemplate spTemplate, String errMsg, int operation) {
        Session sess = null;
        Transaction trx = null;
        String errTitle = "Добавление";
        try {
            sess = HibernateUtils.openSession();
            trx = sess.beginTransaction();
            switch (operation) {
                case 0: {
                    sess.save(spTemplate);
                    break;
                }
                case 1: {
                    sess.update(spTemplate);
                    errTitle = "Изменение";
                }
            }
            sess.flush();
            trx.commit();
            trx = null;
            loadData(sess);
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            JOptionPane.showMessageDialog(mainPanel, errMsg, errTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Модель для JTable
     */
    private class SpendingTableModel extends AbstractTableModel {

        public java.util.List<SpendingTemplate> getTemplates() {
            return templates;
        }

        public void setTemplates(java.util.List<SpendingTemplate> templates) {
            this.templates = templates;
        }

        java.util.List<SpendingTemplate> templates = new ArrayList<SpendingTemplate>();

        /**
         * Returns the number of rows in the model. A
         * <code>JTable</code> uses this method to determine how many rows it
         * should display.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return templates.size();
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
            return SpendingTemplate.getValuesCount();
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
            if ((rowIndex < templates.size()) && (columnIndex < SpendingTemplate.getValuesCount())) {
                return templates.get(rowIndex).getValueByIndex(columnIndex);
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
            if (column < SpendingTemplate.getValuesCount()) {
                return SpendingTemplate.getValueNameByIndex(column);
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
            if (columnIndex < SpendingTemplate.getValuesCount()) {
                return SpendingTemplate.getValueClassByIndex(columnIndex);
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
