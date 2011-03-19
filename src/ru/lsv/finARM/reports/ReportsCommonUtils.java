package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.mappings.ManagerPerMonth;

/**
 * Класс, содержающий общие методы для отчетов
 */
public class ReportsCommonUtils {
    
    /**
     * Получение параметров менеджера за определенный месяц
     * @param sess Сессия
     * @param managerId id менеджера
     * @param month Месяц
     * @param year Год
     * @return Найденного менеджера
     * @throws org.hibernate.HibernateException В случае падения запроса
     */
    public static ManagerPerMonth getManager(Session sess, int managerId, int month, int year) throws HibernateException {
        return (ManagerPerMonth) sess.createQuery("from ManagerPerMonth where managerId=? AND month=? AND year=?").
                setInteger(0, managerId).
                setInteger(1, month).
                setInteger(2, year).
                uniqueResult();
    }


}
