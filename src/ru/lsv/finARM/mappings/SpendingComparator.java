package ru.lsv.finARM.mappings;

import java.sql.Date;
import java.util.Comparator;

/**
 * Компаратор для трат
 * User: Сергей
 * Date: 29.09.2011
 * Time: 22:02:58
 */
public class SpendingComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Spending) {
            return ((Spending) o1).compareTo(o2);
        } else
            return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SpendingComparator);
    }
}
