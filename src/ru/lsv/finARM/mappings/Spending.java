package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;
import java.util.Calendar;

/**
 * Расходы по операциям
 */
public class Spending {
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
            case 5 :
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

}
