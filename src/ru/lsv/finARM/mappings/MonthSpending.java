package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;

/**
 * Планируемые месячные затраты
 */
public class MonthSpending {

    public MonthSpending() {
        amount = 0.0;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    @Override
    public int hashCode() {
        return ((monthSpId == null) ? 0 : monthSpId.hashCode());
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            MonthSpending mp = (MonthSpending)obj;
            return ((monthSpId == null) ? (((MonthSpending) obj).getMonthSpId() == null) : monthSpId.equals(mp.getMonthSpId()));
        } else {
            return false;
        }
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getMonthSpId() {
        return monthSpId;
    }

    public void setMonthSpId(Integer monthSpId) {
        this.monthSpId = monthSpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Идентификатор

     */
    private Integer monthSpId;
    /**
     * Месяц
     */
    private Integer month;
    /**
     * Год
     */
    private Integer year;
    /**
     * Название
      */
    private String name;
    /**
     * Планируемая сумма
     */
    private Double amount;

    @Override
    public String toString() {
        //return CommonUtils.getMonthNameByIndex(month)+" "+year+" -  "+name+" : "+ CommonUtils.formatCurrency(amount);
        return name+" : "+ CommonUtils.formatCurrency(amount);
    }

    /**
     * Возвращает нужное значение по индексу для table model
     * @param columnIndex см.описание
     * @return Значение одного из полей в зависимости от columnIndex
     */
    public Object getValueByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0 : return name;
                case 1 : return CommonUtils.formatCurrency(amount);
                default: return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает класс поля в зависимости от индекса
     * @param columnIndex см.описание
     * @return Класс поля в зависимости от индекса
     */
    public static Class getValueClassByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0 : return String.class;
                case 1 : return String.class;
                default: return null;
            }
        } else {
            return null;        }

    }

    /**
     * Возвращает название поля в зависимости от индекса
     * @param columnIndex см.описание
     * @return Название одного из полей в зависимости от индекса
     */
    public static String getValueNameByIndex (int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0 : return "Наименование";
                case 1 : return "Сумма (руб.)";
                default: return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает количество полей, которые будут отображаться в table model
     * @return см.описание
     */
    public static int getValuesCount() {
        return 2;
    }

}
