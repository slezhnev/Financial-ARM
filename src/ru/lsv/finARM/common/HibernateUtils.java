package ru.lsv.finARM.common;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.lsv.finARM.mappings.Manager;

import java.util.List;

public class HibernateUtils {

    /**
     * Session factory
     */
    private static SessionFactory sessFactory = null;

    /**
     * Создает и настраивает фактори коннектов
     *
     * @param userName      Имя пользователя
     * @param userPsw       Пароль
     * @param serverAddress Адрес сервера
     * @throws ExceptionInInitializerError Если экземпляр уже создан и сделана попытка повторной инициализации
     */
    public static void doSessionFactoryConfiguration(String userName, String userPsw, String serverAddress) throws ExceptionInInitializerError {
        if (sessFactory != null) {
            throw new ExceptionInInitializerError("Session factory already created");
        } else {
            try {
                Configuration conf = new Configuration().
                        configure("ru/lsv/finARM/resources/hibernate.cfg.xml");
                conf.setProperty("hibernate.connection.username", userName);
                conf.setProperty("hibernate.connection.password", userPsw);
                conf.setProperty("hibernate.connection.url", "jdbc:postgresql://" + serverAddress + "/finARM");
                sessFactory = conf.buildSessionFactory();
                // Что-то как-то при проблемах с именем пользователя никто exception создавать не хочет
                // Обойдем тестовым запросом - тогда оно точно бабахнется
                Session sess = sessFactory.openSession();
                List<Manager> tmpMng = sess.createQuery("from Manager").list();
                sess.close();
            }
            catch (Throwable ex) {
                if (sessFactory != null) sessFactory.close();
                sessFactory = null;
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

    /**
     * Открывает сессию
     *
     * @return Открытая сессия
     */
    public static Session openSession() {
        Session sess = sessFactory.openSession();
        // Дополнительная инициализация
        sess.setFlushMode(FlushMode.COMMIT);
        sess.setCacheMode(CacheMode.NORMAL);
        //
        return sess;
    }

}
