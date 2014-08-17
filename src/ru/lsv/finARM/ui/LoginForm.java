package ru.lsv.finARM.ui;

import ru.lsv.finARM.common.HibernateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Простая login form
 */
public class LoginForm {
    private JPanel mainPanel;
    private JTextField userNameEdit;
    private JPasswordField pswEdit;
    private JButton enterBtn;
    private final JFrame frame = new JFrame("Имя пользователя и пароль");

    public LoginForm() {
        enterBtn.addActionListener(new ActionListener() {
                                       @Override
                                       public void actionPerformed(ActionEvent e) {
                                           // Загружаем адрес сервера...
                                           try {
                                               String connection_properties = System.getProperty("connection");
                                               if (connection_properties == null) {
                                                   connection_properties = "connection.properties";
                                               }
                                               Properties props = new Properties();
                                               props.load(new FileReader(connection_properties));
                                               String db = "finARM";
                                               if (!props.containsKey("server.address")) {
                                                   throw new IOException("Invalid configuration file!");
                                               }
                                               if (props.containsKey("db")) {
                                                   db = props.getProperty("db");
                                                   System.out.println("Working with base " + db);
                                               }
                                               HibernateUtils.doSessionFactoryConfiguration(userNameEdit.getText(),
                                                       new String(pswEdit.getPassword()), props.getProperty("server.address"), db);
                                               props.put("last.connected", userNameEdit.getText());
                                               props.store(new FileWriter("connection.properties"), "Connection properties");
                                           } catch (ExceptionInInitializerError ex) {
                                               JOptionPane.showMessageDialog(null, "Неверное имя пользователя/пароль или проблемы с доступностью SQL-сервера");
                                               return;
                                           } catch (IOException e1) {
                                               JOptionPane.showMessageDialog(null, "Не удается получить адрес сервера");
                                               return;
                                           }

                                           // Прячем и будем показывать какую-нито другую форму
                                           MainForm mainForm = new MainForm();
                                           mainForm.buildFrame();
                                           frame.setVisible(false);
                                           mainForm.getFrame().

                                                   setVisible(true);
                                       }
                                   }

        );
    }

    public JFrame buildFrame() throws IOException {
        //
        Properties props = new Properties();
        try {
            props.load(new FileReader("connection.properties"));
            if (!props.containsKey("server.address")) {
                throw new IOException("Invalid configuration file!");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Отсутствует адрес сервера");
            throw new IOException(e.getMessage());
        }
        if (props.containsKey("last.connected")) {
            userNameEdit.setText((String) props.get("last.connected"));
        }
        //
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        //frame.setBounds(10, 10, 100, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enterBtn.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pswEdit.requestFocusInWindow();
        return frame;
    }

}
