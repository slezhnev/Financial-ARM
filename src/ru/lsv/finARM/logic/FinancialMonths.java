package ru.lsv.finARM.logic;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialMonth;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.MonthSpending;
import ru.lsv.finARM.mappings.SpendingTemplate;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Класс работы с финансовыми месяцами
 */
public class FinancialMonths {

    private static FinancialMonths singleton = null;

    /**
     * Синглтон получения экземпляра
     *
     * @return Экземпляр объекта
     */
    public static FinancialMonths getInstance() {
        if (singleton == null) {
            singleton = new FinancialMonths();
        }
        return singleton;
    }

    private List<FinancialMonth> months = null;

    private FinancialMonths() {
        // Формируем список месяцев
        doFillMonths();
    }

    /**
     * Формирует список месяцев
     */
    private void doFillMonths() {
        Session sess = null;
        Transaction trx = null;
        try {
            sess = HibernateUtils.openSession();
            months = sess.createQuery("from FinancialMonth order by year desc, month desc").list();
            // Если еще не закрыт ни один месяц - то сохраним текущий как открытый
            if (months.size() == 0) {
                FinancialMonth fm = new FinancialMonth();
                trx = sess.beginTransaction();
                sess.save(fm);
                sess.flush();
                trx.commit();
                trx = null;
                months = sess.createQuery("from FinancialMonth order by year desc, month desc").list();
            }
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
        }
    }

    /**
     * Получение списка месяцев
     *
     * @return Список месяцев
     */
    public List<FinancialMonth> getMonths() {
        return months;
    }

    /**
     * Обновляет список месяцев
     */
    public void refreshMonths() {
        doFillMonths();
    }

    /**
     * Выполняет операцию закрытия последнего открытого месяца
     *
     * @return 0 - если месяц закрылся, остальное - коды ошибок
     */
    public int closeMonth() {
        //throw new NotImplementedException("closeMonth not implemented");
        // Закрываем текущий месяц...
        FinancialMonth fMonth = getActiveMonth();
        Session sess = null;
        Transaction trx = null;
        try {
            sess = HibernateUtils.openSession();
            trx = sess.beginTransaction();
            // Закрываем текущий
            fMonth.setClosed(true);
            sess.update(fMonth);
            // Создаем новый
            if (fMonth.getMonth() == 11) {
                fMonth = new FinancialMonth(0, fMonth.getYear() + 1);
            } else {
                fMonth = new FinancialMonth(fMonth.getMonth() + 1, fMonth.getYear());
            }
            sess.save(fMonth);
            // Переносим из шаблона в текущий месяц
            List<SpendingTemplate> spTemplates = sess.createQuery("from SpendingTemplate").list();
            for (SpendingTemplate sp : spTemplates) {
                MonthSpending monthSp = new MonthSpending();
                monthSp.setMonth(fMonth.getMonth());
                monthSp.setYear(fMonth.getYear());
                monthSp.setName(sp.getSpendName());
                monthSp.setAmount(sp.getSpendAmount());
                sess.save(monthSp);
            }
            sess.flush();
            trx.commit();
            trx = null;
            sess.close();
            sess = null;
            refreshMonths();
            return 0;
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            return -1;
        }
    }

    /**
     * Открывает последний закрытый месяц
     *
     * @return 0 - если удалось открыть, 1 - есть данные за текущий месяц, -1 - что-то вааще упало
     */
    public int openMonth() {
        //throw new NotImplementedException("openMonth not implemented");
        // Первое - проверяем, а можем ли мы открыть этот месяц?
        // Проверка - наличие в основной базе закрытого элемента по этому месяцу
        Session sess = null;
        Transaction trx = null;
        try {
            sess = HibernateUtils.openSession();
            FinancialMonth fm = FinancialMonths.getInstance().getActiveMonth();
            List<FinancialOperation> fos = sess.createQuery("from FinancialOperation where closed=true AND closeMonth=? AND closeYear=?").
                    setInteger(0, fm.getMonth()).
                    setInteger(1, fm.getYear()).
                    list();
            if (fos.size() > 0) {
                sess.close();
                return 1;
            } else {
                // Грохаем все по этому месяцу из месячных расходов
                trx = sess.beginTransaction();
                sess.createQuery("delete from MonthSpending where month=? and year=?").
                        setInteger(0, fm.getMonth()).
                        setInteger(1, fm.getYear()).
                        executeUpdate();
                // Грохаем текущий месяц
                sess.delete(fm);
                // Получаем предыдущий
                Query query = sess.createQuery("from FinancialMonth where month=? and year=?");
                if (fm.getMonth() == 0) {
                    query = query.setInteger(0, 11).setInteger(1, fm.getYear() - 1);
                } else {
                    query = query.setInteger(0, fm.getMonth() - 1).setInteger(1, fm.getYear());
                }
                fm = (FinancialMonth) query.uniqueResult();
                fm.setClosed(false);
                sess.update(fm);
                sess.flush();
                trx.commit();
                trx = null;
                sess.close();
                sess = null;
                refreshMonths();
                return 0;
            }
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
            return -1;
        }
    }

    /**
     * Возвращает текущий открытый месяц
     *
     * @return Открытый месяц
     */
    public FinancialMonth getActiveMonth() {
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            FinancialMonth fMonth = (FinancialMonth) sess.createQuery("from FinancialMonth where closed=?").setBoolean(0, false).uniqueResult();
            sess.close();
            sess = null;
            return fMonth;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
            return null;
        }
    }

    /**
     * Метод, пробующий обработать ситуацию, когда есть НЕСКОЛЬКО активных месяцев
     */
    public void doFixMultiplyActiveMonths() {
        throw new NotImplementedException("doFixMultiplyActiveMonths not implemented");

    }

    /**
     * Создает модель для отображения списка месяцев
     *
     * @return модель
     */
    public ComboBoxModel getComboBoxModel() {
        return new FinancialMonthsComboBoxModel();
    }

    /**
     * Возвращает рендерер для цветового выделения месяцев
     *
     * @return Рендерер
     */
    public ListCellRenderer getComboBoxRenderer() {
        return new FinancialMonthsComboBoxRenderer();
    }

    /**
     * Combo box model для отображения списка месяцев
     */
    public class FinancialMonthsComboBoxModel extends DefaultComboBoxModel {

        public FinancialMonthsComboBoxModel() {
            super(months.toArray());
            if (getSize() > 0) {
                setSelectedItem(months.get(0));
            }
        }

    }

    /**
     * Combo box renderer для цветового выделения месяцев
     */
    public class FinancialMonthsComboBoxRenderer extends JLabel implements ListCellRenderer {

        public FinancialMonthsComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        /**
         * Return a component that has been configured to display the specified
         * value. That component's <code>paint</code> method is then called to
         * "render" the cell.  If it is necessary to compute the dimensions
         * of a list because the list cells do not have a fixed size, this method
         * is called to generate a component on which <code>getPreferredSize</code>
         * can be invoked.
         *
         * @param list         The JList we're painting.
         * @param value        The value returned by list.getModel().getElementAt(index).
         * @param index        The cells index.
         * @param isSelected   True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component whose paint() method will render the specified value.
         * @see javax.swing.JList
         * @see javax.swing.ListSelectionModel
         * @see javax.swing.ListModel
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                setText("" + value);
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    if (((FinancialMonth) value).getClosed()) {
                        setForeground(Color.RED.darker());
                    } else {
                        setForeground(Color.GREEN.darker());
                    }
                }
            } else {
                setText("");
            }
            return this;
        }
    }

}
