package ru.lsv.finARM.mappings;

/**
 * Сохраненные данные менеджера за финансовый месяц
 */
public class ManagerPerMonth extends Manager {

    public ManagerPerMonth() {
        super();
        month = -1;
        year = -1;
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
        if (obj.getClass().equals(this.getClass())) {
            if (super.equals(obj)) {
                ManagerPerMonth mng = (ManagerPerMonth) obj;
                return (this.month.equals(mng.getMonth()) && this.year.equals(mng.getYear()));
            } else
                return false;
        } else
            return false;
    }

    /**
     * Hash code calculation
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        return (super.hashCode() + month + year);
    }

    /**
     * см. @java.land.String
     *
     * @return Тектовое представление
     */
    @Override
    public String toString() {
        return super.toString() + " - " + month + "." + year;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        if (o == null) return 1;
        return toString().compareTo(o.toString());
    }

    /**
     * Создание объекта с инициализацией
     *
     * @param manager Менеджер
     * @param month   Месяц и год
     */
    public ManagerPerMonth(Manager manager, FinancialMonth month) {
        super(manager);
        this.month = month.getMonth();
        this.year = month.getYear();
    }

    /**
     * Месяц
     */
    private Integer month;
    /**
     * Год
     */
    private Integer year;
    /**
     * Идентификатор
     */
    private Integer mngPerMonthId;

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getMngPerMonthId() {
        return mngPerMonthId;
    }

    public void setMngPerMonthId(Integer mngPerMonthId) {
        this.mngPerMonthId = mngPerMonthId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
