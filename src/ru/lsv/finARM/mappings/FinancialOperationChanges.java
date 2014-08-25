package ru.lsv.finARM.mappings;

import java.sql.Date;
import java.util.Calendar;

/**
 * История изменения финансовых операций
 * User: Сергей
 * Date: 02.10.2011
 * Time: 21:43:05
 */
public class FinancialOperationChanges {
    private String whomChanged;

    public String getWhomChanged() {
        return whomChanged;
    }

    public void setWhomChanged(String whomChanged) {
        this.whomChanged = whomChanged;
    }

    public FinancialOperationChanges(FinancialOperation _old, FinancialOperation changed, String whomChanged) {
        if (changed != null) this.foId = changed.getFoId();
        else this.foId = _old.getFoId();
        this.whomChanged = whomChanged;
        changeDate = new Date(Calendar.getInstance().getTimeInMillis());
        if ((_old == null)&&(changed != null)) {
            whatChanged = "Добавлен " + changed;
        } else if (changed == null) {
            whatChanged = "Удален " + _old;
        } else {
            // А тут все сильно сложнее
            whatChanged = "Изменен " + changed + "\n" + changed.whatChanged(_old);
        }
    }

    public FinancialOperationChanges(Incoming _old, Incoming changed, FinancialOperation fo, String whomChanged) {
        this.foId = fo.getFoId();
        this.whomChanged = whomChanged;
        whatChanged = "Договор " + fo + "\n";
        changeDate = new Date(Calendar.getInstance().getTimeInMillis());
        if ((_old == null)&&(changed != null)) {
            whatChanged = "Добавлен приход " + changed;
        } else if (changed == null) {
            whatChanged = "Удален приход " + _old;
        } else {
            // А тут все сильно сложнее
            whatChanged = "Изменен приход " + changed + "\n" + changed.whatChanged(_old);
        }
    }

    public FinancialOperationChanges(Spending _old, Spending changed, FinancialOperation fo, String whomChanged) {
        this.foId = fo.getFoId();
        this.whomChanged = whomChanged;
        whatChanged = "Договор " + fo + "\n";
        changeDate = new Date(Calendar.getInstance().getTimeInMillis());
        if ((_old == null)&&(changed != null)) {
            whatChanged = "Добавлен расход " + changed;
        } else if (changed == null) {
            whatChanged = "Удален расход " + _old;
        } else {
            // А тут все сильно сложнее
            whatChanged = "Изменен расход " + changed + "\n" + changed.whatChanged(_old);
        }
    }


    private Integer changeId;
    private Integer foId;
    private Date changeDate;
    private String whatChanged;

    public FinancialOperationChanges() {
    }

    public String getWhatChanged() {
        return whatChanged;
    }

    public void setWhatChanged(String whatChanged) {
        this.whatChanged = whatChanged;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public Integer getFoId() {
        return foId;
    }

    public void setFoId(Integer foId) {
        this.foId = foId;
    }

    public Integer getChangeId() {
        return changeId;
    }

    public void setChangeId(Integer changeId) {
        this.changeId = changeId;
    }
}
