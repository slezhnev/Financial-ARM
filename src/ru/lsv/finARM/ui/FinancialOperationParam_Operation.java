package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.Manager;
import ru.lsv.finARM.mappings.Spending;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Параметры договора
 */
public class FinancialOperationParam_Operation {
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JComboBox customerComboBox;
    private JTextField orderNumEdit;
    private JDateChooser dateEdit;
    private JComboBox paymentTypeComboBox;
    private JComboBox managerComboBox;
    private JTable spendingTable;
    private JButton addSpendingBtn;
    private JButton editSpendingBtn;
    private JButton delSpengingBtn;
    private JFormattedTextField operationSumEdit;
    private JButton closeBtn;
    private JPanel panel1;
    private JLabel currentProfitLabel;

    private JDialog dialog;

    private boolean modalResult = false;

    private boolean isClosed = false;
    private Date closeDate = null;
    private Double currentProfit;

    public FinancialOperationParam_Operation(Frame owner) {
        dialog = new JDialog(owner, "Параметры договора");
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
        //
        spendingTable.setModel(new SpendingTableModel());
        //
        addSpendingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAddSpending();
            }
        });
        editSpendingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEditSpending();
            }
        });
        delSpengingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelSpending();
            }
        });
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCloseEnabling(!isClosed);
            }
        });
    }

    /**
     * Удаление расхода
     */
    private void doDelSpending() {
        if (spendingTable.getSelectedRow() > -1) {
            Spending spend = (Spending) ((SpendingTableModel) spendingTable.getModel()).getSpendings().toArray()[spendingTable.getSelectedRow()];
            if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить " + spend + "?", "Удаление расхода", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                ((SpendingTableModel) spendingTable.getModel()).getSpendings().remove(spend);
                ((SpendingTableModel) spendingTable.getModel()).fireTableDataChanged();
                rebuildCurrentProfit();
            }
        }
    }

    /**
     * Редактирование расхода
     */
    private void doEditSpending() {
        if (spendingTable.getSelectedRow() > -1) {
            Spending spend = (Spending) ((SpendingTableModel) spendingTable.getModel()).getSpendings().toArray()[spendingTable.getSelectedRow()];
            FinancialOperationParam_OperationSpending param = new FinancialOperationParam_OperationSpending(dialog);
            // Готовим список. Чтобы избежать проверки на совпадение "само с собой" - удаляем текущий элемент из списка
            // Да и проверять нам надо только на полное совпадение с остальными элементами
            HashSet<Spending> tmpSpendings = new HashSet<Spending>(((SpendingTableModel) spendingTable.getModel()).getSpendings());
            tmpSpendings.remove(spend);
            spend = param.doEdit(spend, tmpSpendings, (Double) operationSumEdit.getValue(), mainPanel);
            if (spend != null) {
                ArrayList<Spending> spendings = new ArrayList<Spending>(((SpendingTableModel) spendingTable.getModel()).getSpendings());
                spendings.set(spendingTable.getSelectedRow(), spend);
                ((SpendingTableModel) spendingTable.getModel()).setSpendings(new HashSet<Spending>(spendings));
                ((SpendingTableModel) spendingTable.getModel()).fireTableRowsUpdated(spendingTable.getSelectedRow(), spendingTable.getSelectedRow());
                rebuildCurrentProfit();
            }
        }
    }

    /**
     * Добавление расхода
     */
    private void doAddSpending() {
        Spending spend = new Spending();
        FinancialOperationParam_OperationSpending param = new FinancialOperationParam_OperationSpending(dialog);
        spend = param.doEdit(spend, ((SpendingTableModel) spendingTable.getModel()).getSpendings(), (Double) operationSumEdit.getValue(),
                mainPanel);
        if (spend != null) {
            ((SpendingTableModel) spendingTable.getModel()).getSpendings().add(spend);
            ((SpendingTableModel) spendingTable.getModel()).fireTableDataChanged();
            rebuildCurrentProfit();
        }
    }

    /**
     * Обрабатывает зыкрытие / открытие
     *
     * @param closed Чего конкретно делать-то надо
     */
    private void doCloseEnabling(Boolean closed) {
        if (closed) {
            if (!isClosed) {
                if (JOptionPane.showConfirmDialog(mainPanel, new String[] {"Вы уверены, что хотите закрыть договор?",
                        "После закрытия редактирование договора будет невозможно без его повторного открытия!"},
                        "Закрытие договора", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;                    
                }
                // Значит - зыкрываем
                Calendar cal = Calendar.getInstance();
                closeDate = new Date(cal.getTimeInMillis());
                isClosed = closed;
            }
            closeBtn.setText("Открыть договор. Дата закрытия - " + new SimpleDateFormat("dd.MM.yyyy").format(closeDate));
            closeBtn.setToolTipText("Открыть закрытый договор");
            java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/refresh_square16_h.png");
            if (img != null) {
                closeBtn.setIcon(new ImageIcon(img));
            }
        } else {
            if (isClosed) {
                if (JOptionPane.showConfirmDialog(mainPanel, new String[] {"Вы уверены, что хотите открыть договор?",
                        "Это может повлиять на уже сформированные и напечатанные отчеты!"},
                        "Открытие договора", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                } 
                isClosed = closed;
            }
            closeBtn.setText("Закрыть договор");
            closeBtn.setToolTipText("Закрыть договор");
            java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/post_square16_h.png");
            if (img != null) {
                closeBtn.setIcon(new ImageIcon(img));
            }
        }
        customerComboBox.setEnabled(!closed);
        orderNumEdit.setEnabled(!closed);
        dateEdit.setEnabled(!closed);
        paymentTypeComboBox.setEnabled(!closed);
        managerComboBox.setEnabled(!closed);
        spendingTable.setEnabled(!closed);
        addSpendingBtn.setEnabled(!closed);
        editSpendingBtn.setEnabled(!closed);
        delSpengingBtn.setEnabled(!closed);
        operationSumEdit.setEnabled(!closed);
    }

    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        // Проверяем всякую фигню...
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата договора", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((customerComboBox.getSelectedItem() == null) || ((String) customerComboBox.getSelectedItem()).trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не выбран заказчик", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        /*if (((Double) operationSumEdit.getValue()) < 0) {
            JOptionPane.showMessageDialog(null, "Сумма договора не может быть меньше 0.", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }*/
        if (orderNumEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не выбран номер счета", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (managerComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Не выбран менеджер", "Параметры договора", JOptionPane.ERROR_MESSAGE);
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

    /**
     * Редактирование информации о договоре
     *
     * @param fo                Договор
     * @param positionComponent Относительно чего позиционироваться
     * @return Скорректированный расход
     */
    public FinancialOperation doEdit(FinancialOperation fo, Component positionComponent) {
        // Вначале - загрузим fo полностью...
        // До кучи - проинициализируем все остальное, связанное с сессией
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            // Грузим fo полностью - если есть id
            if (fo.getFoId() != null)
                fo = (FinancialOperation) sess.get(FinancialOperation.class, fo.getFoId());
            // Грузим список заказчиков
            java.util.List<String> customers = sess.createQuery("select DISTINCT customer from FinancialOperation order by customer").list();
            customerComboBox.setModel(new DefaultComboBoxModel(customers.toArray()));
            customerComboBox.setSelectedItem(fo.getCustomer());
            //
            orderNumEdit.setText(fo.getOrderNum());
            operationSumEdit.setValue(fo.getOperationSum());
            dateEdit.setDate(fo.getOperationDate());
            paymentTypeComboBox.setSelectedIndex(fo.getPaymentType());
            //
            java.util.List<Manager> managers = sess.createQuery("from Manager order by FIO").list();
            managerComboBox.setModel(new DefaultComboBoxModel(managers.toArray()));
            managerComboBox.setSelectedItem(fo.getManager());
            //
            isClosed = fo.getClosed();
            closeDate = fo.getCloseDate();
            doCloseEnabling(fo.getClosed());
            //
            HashSet<Spending> tempSp = new HashSet<Spending>(fo.getSpendings());
            ((SpendingTableModel) spendingTable.getModel()).setSpendings(tempSp);
            //
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
            // Выходим - что-то сломалось
            return null;
        }
        rebuildCurrentProfit();
        //
        dialog.pack();
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            // Сохраняем
            fo.setCustomer((String) customerComboBox.getSelectedItem());
            fo.setOrderNum(orderNumEdit.getText());
            fo.setOperationSum((Double) operationSumEdit.getValue());
            fo.setOperationDate(new Date(dateEdit.getDate().getTime()));
            fo.setPaymentType(paymentTypeComboBox.getSelectedIndex());
            fo.setManager((Manager) managerComboBox.getSelectedItem());
            fo.setClosed(isClosed);
            if (fo.getClosed()) {
                fo.setCloseDate(closeDate);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(fo.getCloseDate().getTime());
                fo.setCloseMonth(cal.get(Calendar.MONTH));
                fo.setCloseYear(cal.get(Calendar.YEAR));
            } else {
                fo.setCloseDate(null);
                fo.setCloseMonth(null);
                fo.setCloseYear(null);
            }
            fo.setSpendings(((SpendingTableModel) spendingTable.getModel()).getSpendings());
            fo.setCurrentProfit(currentProfit);
            return fo;
        } else {
            return null;
        }
    }

    /**
     * Пересчитывает и отображает текущую прибыль по договору
     */
    private void rebuildCurrentProfit() {
        currentProfit = (Double)operationSumEdit.getValue();
        for (Spending sp : ((SpendingTableModel)spendingTable.getModel()).getSpendings()) {
            currentProfit = currentProfit - sp.getPaymentSum();
        }
        currentProfitLabel.setText(CommonUtils.formatCurrency(currentProfit));
    }


    /**
     * table model для трат по договору
     */
    private class SpendingTableModel extends AbstractTableModel {

        /**
         * Расходы
         */
        Set<Spending> spendings = new HashSet<Spending>();

        /**
         * Число полей
         */
        final int colCount = (new Spending()).getFieldCount();

        /**
         * Список расходов для договора
         *
         * @return Расходы
         */
        public Set<Spending> getSpendings() {
            return spendings;
        }

        /**
         * Установить расходы для договора
         *
         * @param spendings Расходы
         */
        public void setSpendings(Set<Spending> spendings) {
            this.spendings = spendings;
        }

        /**
         * Returns the number of rows in the model.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return spendings.size();
        }

        /**
         * Returns the number of columns in the model.
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
            if ((columnIndex < colCount) && (rowIndex < spendings.size())) {
                return ((Spending) spendings.toArray()[rowIndex]).getFieldById(columnIndex);
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
            return (new Spending()).getFieldNameById(column);
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (new Spending()).getFieldClassById(columnIndex);
        }
    }

}
