package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

/**
 * Шаблон месячных расходов
 */
public class SpendingTemplate {

    /**
     * Идентификатор
     */
    private Integer stId;

    public SpendingTemplate() {
        spendAmount = 0.0;
    }

    /**
     * Получение суммы траты
     *
     * @return см.описание
     */
    public Double getSpendAmount() {
        return spendAmount;
    }

    /**
     * Установка суммы траты
     *
     * @param spendAmount см.описание
     */
    public void setSpendAmount(Double spendAmount) {
        this.spendAmount = spendAmount;
    }

    /**
     * Получение наименования траты
     *
     * @return см.описание
     */
    public String getSpendName() {
        return spendName;
    }

    /**
     * Установка наименования траты
     *
     * @param spendName см.описание
     */
    public void setSpendName(String spendName) {
        this.spendName = spendName;
    }

    /**
     * Получение идентификатора
     *
     * @return см.описание
     */
    public Integer getStId() {
        return stId;
    }

    /**
     * Установка идентификатора
     *
     * @param stId см.описание
     */
    public void setStId(Integer stId) {
        this.stId = stId;
    }

    /**
     * Наименование траты
     */

    private String spendName;
    /**
     * Сумма траты
     */
    private Double spendAmount;


    /**
     * см. @java.land.String
     *
     * @return Тектовое представление
     */
    @Override
    public String toString() {
        return spendName + " " + CommonUtils.formatCurrency(spendAmount);
    }

    /**
     * Возвращает нужное значение по индексу для table model
     *
     * @param columnIndex см.описание
     * @return Значение одного из полей в зависимости от columnIndex
     */
    public Object getValueByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0:
                    return spendName;
                case 1:
                    return spendAmount;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает класс поля в зависимости от индекса
     *
     * @param columnIndex см.описание
     * @return Класс поля в зависимости от индекса
     */
    public static Class getValueClassByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Double.class;
                default:
                    return null;
            }
        } else {
            return null;
        }

    }

    /**
     * Возвращает название поля в зависимости от индекса
     *
     * @param columnIndex см.описание
     * @return Название одного из полей в зависимости от индекса
     */
    public static String getValueNameByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0:
                    return "Наименование платежа";
                case 1:
                    return "Сумма";
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает количество полей, которые будут отображаться в table model
     *
     * @return см.описание
     */
    public static int getValuesCount() {
        return 2;
    }

}
