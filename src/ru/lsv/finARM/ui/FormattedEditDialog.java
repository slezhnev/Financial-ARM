package ru.lsv.finARM.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FormattedEditDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JFormattedTextField valueEdit;

    public FormattedEditDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static double doEnterValue(String title, double val, Component locationComponent) {
        FormattedEditDialog dialog = new FormattedEditDialog();
        dialog.setTitle(title);
        dialog.valueEdit.setValue(val);
        dialog.pack();
        dialog.setLocationRelativeTo(locationComponent);
        dialog.setVisible(true);
        return (Double) dialog.valueEdit.getValue();
    }

}
