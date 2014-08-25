package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Поступления по договору
 */
public class Incoming implements Comparable, Cloneable {

    public Incoming() {
        incomingDate = new Date(new java.util.Date().getTime());
        incomingSum = 0.0;
        incomingComment = "";
    }

    @Override
    public int hashCode() {
        return incomingDate.hashCode() + incomingSum.hashCode() + incomingComment.hashCode();
    }

    @Override
    public Object clone()  {
        Incoming inc = new Incoming();
        inc.incomingId = this.incomingId;
        inc.incomingDate = this.incomingDate;
        inc.incomingSum = this.incomingSum;
        inc.incomingComment = this.incomingComment;
        return inc;
    }

    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        if (incomingDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            res.append(" Дата -").append(sdf.format(incomingDate));
        }
        if (incomingSum != null) {
            res.append(" Сумма -").append(CommonUtils.formatCurrency(incomingSum));
        }
        if (incomingComment != null) res.append(" Примечание - \'").append(incomingComment).append("\'");
        return res.toString();
    }

    public String whatChanged(Incoming old) {
        StringBuffer res = new StringBuffer("изменено:");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if ((old.incomingDate != null)&&(!old.incomingDate.equals(this.incomingDate))) {
            res.append("\nдата с ").append(sdf.format(old.incomingDate)).append(" на ").append(this.incomingDate);            
        }
        if (!old.incomingSum.equals(this.incomingSum)) {
            res.append("\nсумма с ").append(CommonUtils.formatCurrency(old.incomingSum)).append(" на ").append(this.incomingSum);            
        }
        if ((old.incomingComment == null)&&(this.incomingComment != null)) {
            res.append("\nвведен комментарий ").append(this.incomingComment);
        } else {
            res.append("\nомментарий с ").append(old.incomingComment).append(" на ").append(this.incomingComment);
        }
        return res.toString();
    }

    private Integer incomingId;
    private Date incomingDate;
    private Double incomingSum;
    private String incomingComment;

    public Integer getIncomingId() {
        return incomingId;
    }

    public void setIncomingId(Integer incomingId) {
        this.incomingId = incomingId;
    }

    public Date getIncomingDate() {
        return incomingDate;
    }

    public void setIncomingDate(Date incomingDate) {
        this.incomingDate = incomingDate;
    }

    public Double getIncomingSum() {
        return incomingSum;
    }

    public void setIncomingSum(Double incomingSum) {
        this.incomingSum = incomingSum;
    }

    public String getIncomingComment() {
        return incomingComment;
    }

    public void setIncomingComment(String incomingComment) {
        this.incomingComment = incomingComment;
    }

    /**
     * см Comparable
     * @param o см Comparable
     * @return см Comparable
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof Incoming) {
            Incoming os = (Incoming) o;
            int val = incomingDate.compareTo(os.getIncomingDate());
            if (val == 0) {
                // Сравнивать будем по кодам. Если коды одинаковые - то и записи совпадают
                if ((incomingId == null) && (os.getIncomingId() == null)) {
                    // Тут они свежедобавленные чтоль? Значит будем сравнивать по хэшу
                    return new Integer(hashCode()).compareTo(os.hashCode());
                } else
                    return incomingId.compareTo(os.getIncomingId());
            } else return val;
        } else return -1;
    }
}
