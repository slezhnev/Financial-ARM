package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Расходы по операциям
 */
public class Spending implements Comparable, Cloneable {

    public Spending() {
        paymentSum = 0.0;
        paymentType = 0;
        paymentDate = new Date(Calendar.getInstance().getTimeInMillis());
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
        return ((payerTo == null ? 0 : payerTo.hashCode()) +
                (orderNum == null ? 0 : orderNum.hashCode()) +
                paymentSum.hashCode() +
                paymentDate.toString().hashCode() + paymentType.hashCode()) / 5;
    }

    /**
     * Indicates whether some other object is "equal to" this one.     *
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
            Spending sp = (Spending) obj;
            return (payerTo == null ? (sp.getPayerTo() == null) : payerTo.equals(sp.getPayerTo())) &&
                    (orderNum == null ? (sp.getOrderNum() == null) : orderNum.equals(sp.getOrderNum())) &&
                    (paymentDate.toString().equals(sp.getPaymentDate().toString())) &&
                    (paymentSum.equals(sp.getPaymentSum())) &&
                    (paymentType.equals(sp.getPaymentType()));
        } else return false;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    public Object clone()  {
        Spending sp = new Spending();    
        sp.finSpId = this.finSpId;
        sp.payerTo = this.payerTo;
        sp.orderNum = this.orderNum;
        sp.paymentSum = this.paymentSum;
        sp.paymentSalarySum = this.paymentSalarySum;
        sp.paymentType = this.paymentType;
        sp.paymentDate = this.paymentDate;
        sp.comment = this.comment;
        return sp;
    }

    public String whatChanged(Spending old) {
        StringBuffer res = new StringBuffer("изменено:");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if (!old.payerTo.equals(this.payerTo)) {
            res.append("\nпоставщик с ").append(old.payerTo).append(" на ").append(this.payerTo);
        }
        if (!old.orderNum.equals(this.orderNum)) {
            res.append("\nномер счета с ").append(old.orderNum).append(" на ").append(this.orderNum);
        }
        if (!old.paymentSum.equals(this.paymentSum)) {
            res.append("\nсумма с ").append(CommonUtils.formatCurrency(old.paymentSum)).append(" на ").append(this.paymentSum);
        }
        if ((old.paymentSalarySum != null)&&(!old.paymentSalarySum.equals(this.paymentSalarySum))) {
            res.append("\nзарплатная сумма с ").append(CommonUtils.formatCurrency(old.paymentSalarySum)).append(" на ").append(CommonUtils.formatCurrency(this.paymentSalarySum));
        }
        if (!old.paymentType.equals(this.paymentType)) {
            res.append("\nвид платежа с ");
            if (old.paymentType == 0) res.append("безнал.");
            else res.append("нал.");
            res.append(" на ");
            if (this.paymentType == 0) res.append("безнал.");
            else res.append("нал.");
        }
        if ((old.paymentDate != null)&&(!old.paymentDate.equals(this.paymentDate))) {
            res.append("\nдата с ").append(sdf.format(old.paymentDate)).append(" на ").append(sdf.format(this.paymentDate));
        }
        return res.toString();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return payerTo + " " + orderNum + " " + CommonUtils.formatCurrency(paymentSum);
    }

    public Integer getFinSpId() {
        return finSpId;
    }

    public void setFinSpId(Integer finSpId) {
        this.finSpId = finSpId;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getPayerTo() {
        return payerTo;
    }

    public void setPayerTo(String payerTo) {
        this.payerTo = payerTo;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Double getPaymentSum() {
        return paymentSum;
    }

    public void setPaymentSum(Double paymentSum) {
        this.paymentSum = paymentSum;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getPaymentSalarySum() {
        return paymentSalarySum;
    }

    public void setPaymentSalarySum(Double paymentSalarySum) {
        this.paymentSalarySum = paymentSalarySum;
    }

    /**
     * Возвращает число полей
     *
     * @return Число полей
     */
    public int getFieldCount() {
        return 6;
    }

    /**
     * Возвращает значение поля по id. 0 - payerTo
     *
     * @param id id
     * @return значение поля
     */
    public Object getFieldById(int id) {
        switch (id) {
            case 0:
                return payerTo;
            case 1:
                return orderNum;
            case 2:
                return paymentSum;
            case 3:
                return (paymentType == 0 ? "нал." : "безнал.");
            case 4:
                return paymentDate;
            case 5:
                return comment;
            default:
                return null;
        }
    }

    /**
     * Возвращает класс поля по id. 0 - payerTo
     *
     * @param id id
     * @return Класс
     */
    public Class getFieldClassById(int id) {
        switch (id) {
            case 2:
                return Double.class;
            case 4:
                return Date.class;
            default:
                return String.class;
        }
    }

    /**
     * Возвращает имя поля по id
     *
     * @param id id
     * @return Имя поля
     */
    public String getFieldNameById(int id) {
        switch (id) {
            case 0:
                return "Поставщик";
            case 1:
                return "Номер счета";
            case 2:
                return "Сумма";
            case 3:
                return "Вид опл.";
            case 4:
                return "Дата";
            case 5:
                return "Комментарий";
            default:
                return null;
        }
    }

    /**
     * Код
     */
    private Integer finSpId;
    /**
     * Наименование поставщика - кому платили
     */
    private String payerTo;
    /**
     * Номер счета для оплаты
     */
    private String orderNum;
    /**
     * Сумма
     */
    private Double paymentSum;
    /**
     * Сумма для расчета зарплаты
     */
    private Double paymentSalarySum;
    /**
     * Вид оплаты. 0 - наличными, 1 - безналичным
     */
    private Integer paymentType;
    /**
     * Дата платежа
     */
    private Date paymentDate;
    /**
     * Комментарий
     */
    private String comment;

    /**
     * см Comparable
     * @param o см Comparable
     * @return см Comparable
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof Spending) {
            Spending os = (Spending) o;
            int val = paymentDate.compareTo(os.getPaymentDate());
            if (val == 0) {
                // Сравнивать будем по кодам. Если коды одинаковые - то и записи совпадают
                if ((finSpId == null) && (os.getFinSpId() == null)) {
                    // Тут они свежедобавленные чтоль? Значит будем сравнивать по хэшу
                    return new Integer(hashCode()).compareTo(os.hashCode());
                } else
                    return finSpId.compareTo(os.getFinSpId());
            } else return val;
        } else return 0;
    }
}
