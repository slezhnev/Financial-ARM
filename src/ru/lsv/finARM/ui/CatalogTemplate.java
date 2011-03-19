package ru.lsv.finARM.ui;

import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Темплейт для формы редактирования справочника
 */
public abstract class CatalogTemplate {
    protected JButton closeBtn;
    protected JTable table;
    protected JButton addBtn;
    protected JButton editBtn;
    protected JButton delBtn;
    protected JPanel mainPanel;

    //
    protected JDialog dialog;
    //

    CatalogTemplate(String title, Frame owner) {
        dialog = new JDialog(owner, title);
        dialog.setModal(true);
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        //
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEdit();
            }
        });
        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelete();
            }
        });
        //
        dialog.getContentPane().add(mainPanel);
        //
    }

    /**
     * Вызывается при закрытии формы
     */
    protected abstract void doClose();

    /**
     * Вызывается при удалении
     */
    protected abstract void doDelete();

    /**
     * Вызывается при редактировании
     */
    protected abstract void doEdit();

    /**
     * Вызывается при добавлении
     */
    protected abstract void doAdd();

    /**
     * Отображение формы
     *
     * @param component Относительно чего размещать
     */
    public void showCatalog(Component component) {
        loadData(null);
        dialog.pack();
        dialog.setLocationRelativeTo(component);
        dialog.setVisible(true);
    }

    /**
     * Загружает данные в table model
     * При загрузке должно сохраняться выделение в model
     *
     * @param session Сессия для загрузки. Если null - будет создана унутре
     */

    protected abstract void loadData(Session session);


}
