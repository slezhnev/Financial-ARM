package ru.lsv.finARM.mappings;

import java.util.Comparator;

/**
 * Компаратор для поступлений
 * User: Сергей
 * Date: 01.10.2011
 * Time: 14:38:31
 */
public class IncomingComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof Incoming) {
            return ((Incoming) o1).compareTo(o2);
        } else
            return 0;
    }
}
