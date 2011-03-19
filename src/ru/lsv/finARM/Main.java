package ru.lsv.finARM;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import ru.lsv.finARM.ui.LoginForm;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {
    
    public static void main(final String[] args) throws Exception {
        /*final Session session = HibernateUtils.openSession();
        try {
            System.out.println("querying all the managed entities...");
            final Map metadataMap = session.getSessionFactory().getAllClassMetadata();
            for (Object key : metadataMap.keySet()) {
                final ClassMetadata classMetadata = (ClassMetadata) metadataMap.get(key);
                final String entityName = classMetadata.getEntityName();
                final Query query = session.createQuery("from " + entityName);
                System.out.println("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    System.out.println("  " + o);
                }
            }
        }
        finally {
            session.close();
        }*/
        System.setProperty("file.encoding", "UTF-8");        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowUI();
            }
        });
    }

    private static void createAndShowUI() {
        configureUI();
        LoginForm loginForm = new LoginForm();
        try {
            loginForm.buildFrame();
        } catch (IOException e) {
            // Просто выходим. Что-то тут стряслось
            System.exit(-1);
        }
    }

    private static void configureUI() {
        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setDefaultIconSize(new Dimension(18, 18));

        String lafName =
            LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lafName);
        } catch (Exception e) {
            System.err.println("Can't set look & feel:" + e);
        }
        //
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.yesButtonText", "Да");
    }

}
