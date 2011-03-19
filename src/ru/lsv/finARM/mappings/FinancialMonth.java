package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Расчетный месяц. С отметкой "закрытия"
 */
public class FinancialMonth {

    //private String[] monthNames;// = {"январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрю"};


    /**
     * Создаем месяц с заданными параметрами
     * @param month Месяц
     * @param year Год
     */
    public FinancialMonth(int month, int year) {
        this.month = month;
        this.year = year;
        closed = false;
    }

    /**
     * По умолчанию создается текущий месяц
     */
    public FinancialMonth() {
        //
        //monthNames = new DateFormatSymbols(new Locale("RU","ru")).getMonths();
        //
        Calendar now = Calendar.getInstance();
        month = now.get(Calendar.MONTH);
        year = now.get(Calendar.YEAR);
        closed = false;
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
        if (obj == null) return false;
        if (getClass().equals(obj.getClass())) {
            FinancialMonth fm = (FinancialMonth)obj;
            return ((month == null ? fm.getMonth() == null : month.equals(fm.getMonth()))&&
                    (year == null ? fm.getYear() == null : year.equals(fm.getYear())));
        } else {
            return false;
        }
    }

    Integer fmId;
    Integer month;

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Integer getFmId() {
        return fmId;
    }

    public void setFmId(Integer fmId) {
        this.fmId = fmId;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if ((month >= 0) && (month < 12)) {
            return CommonUtils.getMonthNameByIndex(month) + " " + year;
        } else {
            return "" + month + " " + year;
        }
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    Integer year;
    Boolean closed;

}
