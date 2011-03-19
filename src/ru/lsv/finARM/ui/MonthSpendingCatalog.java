package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.FinancialMonth;
import ru.lsv.finARM.mappings.MonthSpending;
import ru.lsv.finARM.mappings.SpendingTemplate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Список планируемых месячных трат
 */
public class MonthSpendingCatalog {
    private JPanel mainPanel;
    private JButton closeBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton delBtn;
    private JTable table;
    private JPanel periodPanel;
    private JComboBox monthsComboBox;
    private JButton moveFromTemplateBtn;

    //
    protected JDialog dialog;
    //
    private MonthSpendingParam monthSpendingParam;


    MonthSpendingCatalog(Frame owner) {
        dialog = new JDialog(owner, "Планируемые месячные расходы");
        dialog.setModal(true);
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        //
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEdit();
            }
        });
        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelete();
            }
        });
        //
        dialog.getContentPane().add(mainPanel);
        //
        table.setModel(new MonthSpendingTableModel());
        //
        monthsComboBox.setModel(FinancialMonths.getInstance().getComboBoxModel());
        monthsComboBox.setRenderer(FinancialMonths.getInstance().getComboBoxRenderer());
        //
        monthsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBtn.setEnabled(monthsComboBox.getSelectedIndex() == 0);
                editBtn.setEnabled(monthsComboBox.getSelectedIndex() == 0);
                delBtn.setEnabled(monthsComboBox.getSelectedIndex() == 0);
                moveFromTemplateBtn.setEnabled(monthsComboBox.getSelectedIndex() == 0);
                loadData(null, false);
            }
        });
        //
        monthSpendingParam = new MonthSpendingParam(dialog);
        moveFromTemplateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFromTemplate();
            }
        });
    }

    /**
     * Грохает ВСЕ текущие расходы и переносит данные из шаблона в текущий месяц
     */
    private void moveFromTemplate() {
        Session sess = null;
        Transaction trx = null;
        try {
            sess = HibernateUtils.openSession();
            // Вначале проверяем - а может быть в основном уже что-то есть занесенное за этот месяц?!
            FinancialMonth fm = (FinancialMonth) monthsComboBox.getSelectedItem();
            java.util.List res = sess.createQuery("select foId from FinancialOperation where kind=2 AND closeMonth=? AND closeYear=? AND plannedSpending != null").
                    setInteger(0, fm.getMonth()).
                    setInteger(1, fm.getYear()).list();
            if (res.size() > 0) {
                JOptionPane.showMessageDialog(mainPanel, "По ллановым расходам уже занесены расходы в основной журнал. Перенос невозможен",
                        "Перенос из шаблона", JOptionPane.ERROR_MESSAGE);
            } else {
                String[] msg = {"Вы уверены, что хотите перенести данные из шаблона в текущий месяц?",
                        "Все планируемые расходы за текущий месяц при этом будут удалены!"};
                if (JOptionPane.showConfirmDialog(mainPanel, msg, "Перенос из шаблона", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    trx = sess.beginTransaction();
                    // Грохаем все за текущий месяц
                    sess.createQuery("delete from MonthSpending where month=? and year=?").
                            setInteger(0, fm.getMonth()).
                            setInteger(1, fm.getYear()).executeUpdate();
                    // Получаем все из шаблона
                    java.util.List<SpendingTemplate> spTemplates = sess.createQuery("from SpendingTemplate").list();
                    for (SpendingTemplate sp : spTemplates) {
                        MonthSpending monthSp = new MonthSpending();
                        monthSp.setMonth(fm.getMonth());
                        monthSp.setYear(fm.getYear());
                        monthSp.setName(sp.getSpendName());
                        monthSp.setAmount(sp.getSpendAmount());
                        sess.save(monthSp);
                    }
                    sess.flush();
                    trx.commit();
                    loadData(null, false);
                }
            }
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
        }
    }

    /**
     * Вызывается при удалении
     */
    private void doDelete() {
        java.util.List<MonthSpending> spends = ((MonthSpendingTableModel) table.getModel()).getMonthSpending();
        if ((table.getSelectedRow() > -1) && (table.getSelectedRow() < spends.size())) {
            MonthSpending spTemplate = spends.get(table.getSelectedRow());
            Session sess = null;
            Transaction trx = null;
            try {
                sess = HibernateUtils.openSession();
                // Проверяем - а может на это уже что-то заносили в основную базу?
                java.util.List res = sess.createQuery("select foId from FinancialOperation where plannedSpending.monthSpId=?").setInteger(0, spTemplate.getMonthSpId()).list();
                if (res.size() != 0) {
                    // Значит удалять ничего НЕЛЬЗЯ!
                    JOptionPane.showMessageDialog(mainPanel, "На данный расход уже заведены расходы. Удаление невозможно",
                            "Удаление", JOptionPane.ERROR_MESSAGE);
                    sess.close();
                    sess = null;
                    return;
                }
                if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить планируемый месячный расход \"" + spTemplate + "\"?",
                        "Удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    trx = sess.beginTransaction();
                    sess.delete(spTemplate);
                    sess.flush();
                    trx.commit();
                    trx = null;
                    table.getSelectionModel().clearSelection();
                    loadData(sess, false);
                    sess.close();
                    sess = null;
                }
            } catch (HibernateException ex) {
                if (trx != null) trx.rollback();
                if (sess != null) sess.close();
                JOptionPane.showMessageDialog(mainPanel, "Ошибка удаление планируемого расхода", "Удаление", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Вызывается при редактировании
     */
    private void doEdit() {
        java.util.List<MonthSpending> spends = ((MonthSpendingTableModel) table.getModel()).getMonthSpending();
        if ((table.getSelectedRow() > -1) && (table.getSelectedRow() < spends.size())) {
            MonthSpending spTemplate = spends.get(table.getSelectedRow());
            // Мимикримуем по SpendingTemplate
            spTemplate = monthSpendingParam.doEdit(spTemplate, mainPanel);
            if (spTemplate != null) {
                saveSpendingTemplateToDB(spTemplate, "Ошибка сохранения планируемого месячного расхода", 1);
            }
        }
    }

    /**
     * Сохраняет или апдейтит в базу
     *
     * @param spTemplate Что сохранять
     * @param errMsg     Сообщение об ошибке, которое будет выдано
     * @param operation  0 - добавление, 1 - редактирование
     */
    private void saveSpendingTemplateToDB(MonthSpending spTemplate, String errMsg, int operation) {
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
            loadData(sess, true);
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            JOptionPane.showMessageDialog(mainPanel, errMsg, errTitle, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Вызывается при добавлении
     */
    private void doAdd() {
        MonthSpending spTemplate = new MonthSpending();
        spTemplate.setYear(FinancialMonths.getInstance().getActiveMonth().getYear());
        spTemplate.setMonth(FinancialMonths.getInstance().getActiveMonth().getMonth());
        spTemplate = monthSpendingParam.doEdit(spTemplate, mainPanel);
        if (spTemplate != null) {
            // Тут надо сохранять
            saveSpendingTemplateToDB(spTemplate, "Ошибка добавления планируемого месячного расхода", 0);
        }
    }

    /**
     * Вызывается при закрытии формы
     */
    private void doClose() {
        dialog.setVisible(false);
    }

    /**
     * Отображение формы
     *
     * @param component Относительно чего размещать
     */
    public void showCatalog(Component component) {
        loadData(null, false);
        dialog.pack();
        dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);
    }

    /**
     * Загружает данные в table model
     *
     * @param session      Сессия для загрузки. Если null - будет создана унутре
     * @param savePosition Сохранять или нет выделение
     */
    private void loadData(Session session, boolean savePosition) {
        int savedStId = -1;
        java.util.List<MonthSpending> spends = ((MonthSpendingTableModel) table.getModel()).getMonthSpending();
        if ((table.getSelectedRow() != -1) && (table.getSelectedRow() < spends.size())) {
            savedStId = spends.get(table.getSelectedRow()).getMonthSpId();
        }
        Session sess = null;
        try {
            if (session == null) sess = HibernateUtils.openSession();
            else sess = session;
            FinancialMonth fm = (FinancialMonth) monthsComboBox.getSelectedItem();
            spends = sess.createQuery("from MonthSpending where month=? AND year=? order by amount").
                    setInteger(0, fm.getMonth()).setInteger(1, fm.getYear()).list();
            ((MonthSpendingTableModel) table.getModel()).setMonthSpendingList(spends);
            ((MonthSpendingTableModel) table.getModel()).fireTableDataChanged();
            if ((savePosition) && (savedStId != -1)) {
                // Востанновим выделение
                for (int i = 0; i < spends.size(); i++) {
                    if (spends.get(i).getMonthSpId() == savedStId) {
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
     * Table model
     */
    private class MonthSpendingTableModel extends AbstractTableModel {

        private final int colCount = MonthSpending.getValuesCount();

        public java.util.List<MonthSpending> getMonthSpending() {
            return monthSpending;
        }

        private java.util.List<MonthSpending> monthSpending = new ArrayList<MonthSpending>();

        public void setMonthSpendingList(java.util.List<MonthSpending> list) {
            monthSpending = list;
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
            return monthSpending.size();
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
            if ((rowIndex < monthSpending.size()) && (columnIndex < colCount)) {
                return monthSpending.get(rowIndex).getValueByIndex(columnIndex);
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
                return MonthSpending.getValueNameByIndex(column);
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
                return MonthSpending.getValueClassByIndex(columnIndex);
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
