package ru.lsv.finARM.ui;

import com.toedter.calendar.JDateChooser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.common.UserRoles;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Параметры договора
 */
public class FinancialOperationParam_Operation {
    private JPanel mainPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JComboBox customerComboBox;
    private JTextField orderNumEdit;
    private JDateChooser dateEdit;
    private JComboBox paymentTypeComboBox;
    private JComboBox managerComboBox;
    private JTable spendingTable;
    private JButton addSpendingBtn;
    private JButton editSpendingBtn;
    private JButton delSpengingBtn;
    private JFormattedTextField operationSumEdit;
    private JButton closeBtn;
    private JPanel panel1;
    private JLabel currentProfitLabel;
    private JFormattedTextField salarySum;
    private JLabel currentSalaryProfitLabel;
    private JFormattedTextField managerPercentEdit;
    private JButton recalcProfitBtn;
    private JButton closeForSalaryBtn;
    private JLabel currentSalaryProfitLabel_Text;
    private JTable incomingsTable;
    private JButton recalcSumBtn;
    private JButton addIncBtn;
    private JButton editIncBtn;
    private JButton delIncBtn;

    private JDialog dialog;

    private boolean modalResult = false;

    private boolean isClosed = false;
    private Date closeDate = null;
    private boolean isClosedForSalary = false;
    private Date closeForSalaryDate = null;
    private Double currentProfit;
    private Double currentSalaryProfit;
    //private boolean isDirector = false;
    private UserRoles userRole;

    public FinancialOperationParam_Operation(Frame owner) {
        dialog = new JDialog(owner, "Параметры договора");
        dialog.setModal(true);
        dialog.getContentPane().add(mainPanel);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doOnClosing();
            }
        });
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doOnClosing();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doNormalClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        //
        //
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOnClosing();
            }
        });
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обрабатываем нормально закрытие
                doNormalClose();
            }
        });
        //
        spendingTable.setModel(new SpendingTableModel());
        //
        addSpendingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAddSpending();
            }
        });
        editSpendingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEditSpending();
            }
        });
        delSpengingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelSpending();
            }
        });
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCloseEnabling(!isClosed);
            }
        });
        operationSumEdit.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (salarySum.isEnabled())
                    salarySum.setValue(operationSumEdit.getValue());
            }
        });
        managerComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSetManagerPercent();
            }
        });
        paymentTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSetManagerPercent();
            }
        });
        recalcProfitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Обновим траты - с учетом того, что у нас может зарплатных сумм и не быть
                for (Spending sp : ((SpendingTableModel) spendingTable.getModel()).getSpendings()) {
                    if (sp.getPaymentSalarySum() == null)
                        sp.setPaymentSalarySum(sp.getPaymentSum());
                }
                rebuildCurrentProfit();
            }
        });
        closeForSalaryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCloseForSalaryEnabling(!isClosedForSalary);
            }
        });
        //
        incomingsTable.setModel(new IncomingTableModel());
        recalcSumBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recalcOperationSum();
            }
        });
        addIncBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAddIncoming();
            }
        });
        editIncBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doEditIncoming();
            }
        });
        delIncBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelIncoming();
            }
        });
    }

    /**
     * Удаление поступления
     */
    private void doDelIncoming() {
        if (incomingsTable.getSelectedRow() > -1) {
            Incoming inc = (Incoming) ((IncomingTableModel) incomingsTable.getModel()).getIncomings().toArray()[incomingsTable.getSelectedRow()];
            if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить поступление?", "Удаление поступления", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                ((IncomingTableModel) incomingsTable.getModel()).getIncomings().remove(inc);
                ((IncomingTableModel) incomingsTable.getModel()).fireTableDataChanged();
                recalcOperationSum();
            }
        }
    }

    /**
     * Изменение поступления
     */
    private void doEditIncoming() {
        if (incomingsTable.getSelectedRow() > -1) {
            Incoming inc = (Incoming) ((IncomingTableModel) incomingsTable.getModel()).getIncomings().toArray()[incomingsTable.getSelectedRow()];
            FinancialOperationParam_OperationIncoming fooi = new FinancialOperationParam_OperationIncoming(dialog);
            inc = fooi.doEdit(inc, mainPanel);
            if (inc != null) {
                ArrayList<Incoming> incs = new ArrayList<Incoming>(((IncomingTableModel) incomingsTable.getModel()).getIncomings());
                incs.set(incomingsTable.getSelectedRow(), inc);
                ((IncomingTableModel) incomingsTable.getModel()).setIncomings(new HashSet<Incoming>(incs));
                ((IncomingTableModel) incomingsTable.getModel()).fireTableRowsUpdated(incomingsTable.getSelectedRow(), incomingsTable.getSelectedRow());
                recalcOperationSum();
            }
        }
    }

    /**
     * Добавление поступления
     */
    private void doAddIncoming() {
        Incoming inc = new Incoming();
        FinancialOperationParam_OperationIncoming fooi = new FinancialOperationParam_OperationIncoming(dialog);
        inc = fooi.doEdit(inc, mainPanel);
        if (inc != null) {
            ((IncomingTableModel) incomingsTable.getModel()).getIncomings().add(inc);
            ((IncomingTableModel) incomingsTable.getModel()).fireTableDataChanged();
            recalcOperationSum();
        }
    }

    /**
     * Пересчет суммы договора по поступлениям
     */
    private void recalcOperationSum() {
        if (!isClosed) {
            // Что-то делаем только в том случае, если договор не закрыт
            if (((IncomingTableModel) incomingsTable.getModel()).getIncomings().size() == 0) {
                operationSumEdit.setEnabled(true);
            } else {
                operationSumEdit.setEnabled(false);
                Double sum = 0.0;
                for (Incoming inc : ((IncomingTableModel) incomingsTable.getModel()).getIncomings()) {
                    sum = sum + inc.getIncomingSum();
                }
                operationSumEdit.setValue(sum);
                if (!isClosedForSalary)
                    salarySum.setValue(sum);
            }
            rebuildCurrentProfit();
        }
    }


    /**
     * Устанавливает процент менеджера при изменении менеджера или вида оплаты
     */
    private void doSetManagerPercent() {
        if (managerComboBox.getSelectedItem() != null) {
            if (paymentTypeComboBox.getSelectedIndex() == 0)
                managerPercentEdit.setValue(((Manager) managerComboBox.getSelectedItem()).getCashPercent());
            else
                managerPercentEdit.setValue(((Manager) managerComboBox.getSelectedItem()).getNonCashPercent());
        }
    }

    /**
     * Удаление расхода
     */
    private void doDelSpending() {
        if (spendingTable.getSelectedRow() > -1) {
            Spending spend = (Spending) ((SpendingTableModel) spendingTable.getModel()).getSpendings().toArray()[spendingTable.getSelectedRow()];
            if (JOptionPane.showConfirmDialog(mainPanel, "Вы уверены, что хотите удалить " + spend + "?", "Удаление расхода", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                ((SpendingTableModel) spendingTable.getModel()).getSpendings().remove(spend);
                ((SpendingTableModel) spendingTable.getModel()).fireTableDataChanged();
                rebuildCurrentProfit();
            }
        }
    }

    /**
     * Редактирование расхода
     */
    private void doEditSpending() {
        if (spendingTable.getSelectedRow() > -1) {
            Spending spend = (Spending) ((SpendingTableModel) spendingTable.getModel()).getSpendings().toArray()[spendingTable.getSelectedRow()];
            FinancialOperationParam_OperationSpending param = new FinancialOperationParam_OperationSpending(dialog);
            // Готовим список. Чтобы избежать проверки на совпадение "само с собой" - удаляем текущий элемент из списка
            // Да и проверять нам надо только на полное совпадение с остальными элементами
            HashSet<Spending> tmpSpendings = new HashSet<Spending>(((SpendingTableModel) spendingTable.getModel()).getSpendings());
            tmpSpendings.remove(spend);
            spend = param.doEdit(spend, tmpSpendings, (Double) operationSumEdit.getValue(), isClosedForSalary, mainPanel);
            if (spend != null) {
                ArrayList<Spending> spendings = new ArrayList<Spending>(((SpendingTableModel) spendingTable.getModel()).getSpendings());
                spendings.set(spendingTable.getSelectedRow(), spend);
                ((SpendingTableModel) spendingTable.getModel()).setSpendings(new HashSet<Spending>(spendings));
                ((SpendingTableModel) spendingTable.getModel()).fireTableRowsUpdated(spendingTable.getSelectedRow(), spendingTable.getSelectedRow());
                rebuildCurrentProfit();
            }
        }
    }

    /**
     * Добавление расхода
     */
    private void doAddSpending() {
        Spending spend = new Spending();
        FinancialOperationParam_OperationSpending param = new FinancialOperationParam_OperationSpending(dialog);
        spend = param.doEdit(spend, ((SpendingTableModel) spendingTable.getModel()).getSpendings(), (Double) operationSumEdit.getValue(),
                isClosedForSalary, mainPanel);
        if (spend != null) {
            ((SpendingTableModel) spendingTable.getModel()).getSpendings().add(spend);
            ((SpendingTableModel) spendingTable.getModel()).fireTableDataChanged();
            rebuildCurrentProfit();
        }
    }

    /**
     * Обрабатывает зыкрытие / открытие
     *
     * @param closed Чего конкретно делать-то надо
     */
    private void doCloseEnabling(Boolean closed) {
        if (closed) {
            if (!isClosed) {
                if (JOptionPane.showConfirmDialog(mainPanel, new String[]{"Вы уверены, что хотите закрыть договор?",
                        "После закрытия редактирование договора будет невозможно без его повторного открытия!"},
                        "Закрытие договора", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                // Значит - зыкрываем
                Calendar cal = Calendar.getInstance();
                FinancialMonth fm = FinancialMonths.getInstance().getActiveMonth();
                if (cal.get(Calendar.MONTH) != fm.getMonth()) {
                    cal.set(Calendar.YEAR, fm.getYear());
                    cal.set(Calendar.MONTH, fm.getMonth());
                    cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
                }
                closeDate = new Date(cal.getTimeInMillis());
                isClosed = closed;
            }
            closeBtn.setText("Открыть договор. Дата закрытия - " + new SimpleDateFormat("dd.MM.yyyy").format(closeDate));
            closeBtn.setToolTipText("Открыть закрытый договор");
            java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/refresh_square16_h.png");
            if (img != null) {
                closeBtn.setIcon(new ImageIcon(img));
            }
            if (userRole != UserRoles.DIRECTOR) closeBtn.setEnabled(false);
        } else {
            if (isClosed) {
                if (JOptionPane.showConfirmDialog(mainPanel, new String[]{"Вы уверены, что хотите открыть договор?",
                        "Это может повлиять на уже сформированные и напечатанные отчеты!"},
                        "Открытие договора", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
                isClosed = closed;
            }
            closeBtn.setText("Закрыть договор");
            closeBtn.setToolTipText("Закрыть договор");
            java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/post_square16_h.png");
            if (img != null) {
                closeBtn.setIcon(new ImageIcon(img));
            }
        }
        customerComboBox.setEnabled(!closed);
        orderNumEdit.setEnabled(!closed);
        dateEdit.setEnabled(!closed);
        paymentTypeComboBox.setEnabled(!closed);
        managerComboBox.setEnabled(!closed);
        spendingTable.setEnabled(!closed);
        addSpendingBtn.setEnabled(!closed);
        editSpendingBtn.setEnabled(!closed);
        delSpengingBtn.setEnabled(!closed);
        operationSumEdit.setEnabled(!closed);
        salarySum.setEnabled(!closed);
        managerPercentEdit.setEnabled(!closed);
        recalcProfitBtn.setEnabled(!closed);
        //
        incomingsTable.setEnabled(!closed);
        addIncBtn.setEnabled(!closed);
        editIncBtn.setEnabled(!closed);
        delIncBtn.setEnabled(!closed);
        recalcSumBtn.setEnabled(!closed);
        //
        if (closeForSalaryBtn.isEnabled()) closeForSalaryBtn.setEnabled(!closed);
    }


    /**
     * Обрабатывает зыкрытие / открытие для зарплаты
     *
     * @param closed Чего конкретно делать-то надо
     */
    private void doCloseForSalaryEnabling(Boolean closed) {
        if (!isClosed) {
            // Что- тут мы будем делать только в случае, если у нас договор не закрыт
            closeForSalaryBtn.setEnabled(userRole == UserRoles.DIRECTOR);
            if (closed) {
                if (!isClosedForSalary) {
                    if (JOptionPane.showConfirmDialog(mainPanel, new String[]{"Вы уверены, что хотите закрыть договор по зарплате?",
                            "После закрытия будут недоступны к редактированию зарплатная сумма, менеджер, зарплатный процент, вид оплаты,",
                            "не будет пересчитываться текущая зарплатная прибыль !"},
                            "Закрытие договора по зарплате", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    // Значит - зыкрываем
                    Calendar cal = Calendar.getInstance();
                    FinancialMonth fm = FinancialMonths.getInstance().getActiveMonth();
                    if (cal.get(Calendar.MONTH) != fm.getMonth()) {
                        cal.set(Calendar.YEAR, fm.getYear());
                        cal.set(Calendar.MONTH, fm.getMonth());
                        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
                    }
                    closeForSalaryDate = new Date(cal.getTimeInMillis());
                    isClosedForSalary = closed;
                }
                closeForSalaryBtn.setText("Открыть договор по зарплате. Дата закрытия - " + new SimpleDateFormat("dd.MM.yyyy").format(closeForSalaryDate));
                closeForSalaryBtn.setToolTipText("Открыть закрытый по зарплате договор");
                java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/refresh_square16_h.png");
                if (img != null) {
                    closeForSalaryBtn.setIcon(new ImageIcon(img));
                }
            } else {
                if (isClosedForSalary) {
                    if (JOptionPane.showConfirmDialog(mainPanel, new String[]{"Вы уверены, что хотите открыть договор по зарплате?",
                            "Это может повлиять на уже сформированные и напечатанные отчеты!", " ",
                            "ВНИМАНИЕ - если в закрытый по зарплате договор вносились расходы - не забудьте скорректировать в них зарплатные суммы!", " "},
                            "Открытие договора по зарплате", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    isClosedForSalary = closed;
                }
                closeForSalaryBtn.setText("Закрыть договор по зарплате");
                closeForSalaryBtn.setToolTipText("Закрыть договор по зарплате");
                java.net.URL img = getClass().getResource("ru/lsv/finARM/resources/post_square16_h.png");
                if (img != null) {
                    closeForSalaryBtn.setIcon(new ImageIcon(img));
                }
            }
            paymentTypeComboBox.setEnabled(!closed);
            managerComboBox.setEnabled(!closed);
            salarySum.setEnabled(!closed);
            managerPercentEdit.setEnabled(!closed);
            if (closed) {
                currentSalaryProfitLabel.setForeground(Color.ORANGE.darker());
            } else {
                currentSalaryProfitLabel.setForeground(Color.BLACK);
            }
            currentSalaryProfitLabel_Text.setForeground(currentSalaryProfitLabel.getForeground());
        }
    }


    /**
     * Обработка нормального закрытия
     */
    private void doNormalClose() {
        // Если кнопка сохранения выключена - то просто ничего не делаем
        if (!saveBtn.isEnabled()) return;
        // Проверяем всякую фигню...
        if (dateEdit.getDate() == null) {
            JOptionPane.showMessageDialog(null, "Не указана дата договора", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((customerComboBox.getSelectedItem() == null) || ((String) customerComboBox.getSelectedItem()).trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не выбран заказчик", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        /*if (((Double) operationSumEdit.getValue()) < 0) {
            JOptionPane.showMessageDialog(null, "Сумма договора не может быть меньше 0.", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }*/
        if (orderNumEdit.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(null, "Не выбран номер счета", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (managerComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Не выбран менеджер", "Параметры договора", JOptionPane.ERROR_MESSAGE);
            return;
        }
        //
        modalResult = true;
        dialog.setVisible(false);
    }

    /**
     * Обработка закрытия без сохранения
     */
    private void doOnClosing() {
        modalResult = false;
        dialog.setVisible(false);
    }

    /**
     * Редактирование информации о договоре
     *
     * @param fo                Договор
     * @param positionComponent Относительно чего позиционироваться
     * @param userRole          Роль текущего пользователя в системе
     * @param allowSave         Разрешать ли сохранять текущее редактирование
     * @return Скорректированный расход
     */
    public FinancialOperation doEdit(FinancialOperation fo, Component positionComponent, UserRoles userRole, boolean allowSave) {
        //this.isDirector = isDirector;
        this.userRole = userRole;
        // Вначале - загрузим fo полностью...
        // До кучи - проинициализируем все остальное, связанное с сессией
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            // Грузим fo полностью - если есть id
            if (fo.getFoId() != null)
                fo = (FinancialOperation) sess.get(FinancialOperation.class, fo.getFoId());
            // Грузим список заказчиков
            java.util.List<String> customers = sess.createQuery("select DISTINCT customer from FinancialOperation order by customer").list();
            customerComboBox.setModel(new DefaultComboBoxModel(customers.toArray()));
            customerComboBox.setSelectedItem(fo.getCustomer());
            //
            orderNumEdit.setText(fo.getOrderNum());
            operationSumEdit.setValue(fo.getOperationSum());
            //
            recalcOperationSum();
            //
            if (fo.getSalarySum() != null)
                salarySum.setValue(fo.getSalarySum());
            else
                salarySum.setValue(fo.getOperationSum());
            dateEdit.setDate(fo.getOperationDate());
            paymentTypeComboBox.setSelectedIndex(fo.getPaymentType());
            //
            java.util.List<Manager> managers = sess.createQuery("from Manager order by FIO").list();
            managerComboBox.setModel(new DefaultComboBoxModel(managers.toArray()));
            managerComboBox.setSelectedItem(fo.getManager());
            doSetManagerPercent();
            if (fo.getManagerPercent() != null) {
                managerPercentEdit.setValue(fo.getManagerPercent());
            }
            //
            currentSalaryProfit = fo.getCurrentSalaryProfit();
            //
            isClosed = fo.getClosed();
            closeDate = fo.getCloseDate();
            doCloseEnabling(fo.getClosed());
            isClosedForSalary = fo.getClosedForSalary();
            closeForSalaryDate = fo.getCloseForSalaryDate();
            doCloseForSalaryEnabling(fo.getClosedForSalary());
            //
            HashSet<Spending> tempSp = new HashSet<Spending>(fo.getSpendings());
            ((SpendingTableModel) spendingTable.getModel()).setSpendings(tempSp);
            //
            HashSet<Incoming> incs = new HashSet<Incoming>(fo.getIncomings());
            ((IncomingTableModel) incomingsTable.getModel()).setIncomings(incs);
            //
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
            // Выходим - что-то сломалось
            return null;
        }
        //
        dialog.pack();
        //
        if (allowSave) {
            recalcOperationSum();
            rebuildCurrentProfit();
        } else {
            // Выключаем ВСЕ НАФИГ!
            CommonUtils.disableComponents(dialog);
            cancelBtn.setEnabled(true);
        }
        //
        dialog.setLocationRelativeTo(positionComponent);
        dialog.setVisible(true);
        if (modalResult) {
            // Сохраняем
            fo.setCustomer((String) customerComboBox.getSelectedItem());
            fo.setOrderNum(orderNumEdit.getText());
            fo.setOperationSum((Double) operationSumEdit.getValue());
            fo.setOperationDate(new Date(dateEdit.getDate().getTime()));
            fo.setPaymentType(paymentTypeComboBox.getSelectedIndex());
            fo.setManager((Manager) managerComboBox.getSelectedItem());
            fo.setClosed(isClosed);
            if (fo.getClosed()) {
                fo.setCloseDate(closeDate);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(fo.getCloseDate().getTime());
                fo.setCloseMonth(cal.get(Calendar.MONTH));
                fo.setCloseYear(cal.get(Calendar.YEAR));
            } else {
                fo.setCloseDate(null);
                fo.setCloseMonth(null);
                fo.setCloseYear(null);
            }
            fo.setClosedForSalary(isClosedForSalary);
            if (fo.getClosedForSalary()) {
                fo.setCloseForSalaryDate(closeForSalaryDate);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(fo.getCloseForSalaryDate().getTime());
                fo.setCloseForSalaryMonth(cal.get(Calendar.MONTH));
                fo.setCloseForSalaryYear(cal.get(Calendar.YEAR));
            } else {
                fo.setCloseForSalaryDate(null);
                fo.setCloseForSalaryMonth(null);
                fo.setCloseForSalaryYear(null);
            }
            fo.setSpendings(((SpendingTableModel) spendingTable.getModel()).getSpendings());
            fo.setIncomings(((IncomingTableModel) incomingsTable.getModel()).getIncomings());
            fo.setCurrentProfit(currentProfit);
            fo.setSalarySum((Double) salarySum.getValue());
            fo.setCurrentSalaryProfit(currentSalaryProfit);
            fo.setManagerPercent((Double) managerPercentEdit.getValue());
            return fo;
        } else {
            return null;
        }
    }

    /**
     * Пересчитывает и отображает текущую прибыль по договору
     */
    private void rebuildCurrentProfit() {
        currentProfit = (Double) operationSumEdit.getValue();
        currentSalaryProfit = (Double) salarySum.getValue();
        for (Spending sp : ((SpendingTableModel) spendingTable.getModel()).getSpendings()) {
            currentProfit = currentProfit - sp.getPaymentSum();
            if (sp.getPaymentSalarySum() != null)
                currentSalaryProfit = currentSalaryProfit - sp.getPaymentSalarySum();
        }
        currentProfitLabel.setText(CommonUtils.formatCurrency(currentProfit));
        currentSalaryProfitLabel.setText(CommonUtils.formatCurrency(currentSalaryProfit));
    }


    /**
     * table model для трат по договору
     */
    private class SpendingTableModel extends AbstractTableModel {

        /**
         * Расходы
         */
        Set<Spending> spendings = new HashSet<Spending>();

        /**
         * Число полей
         */
        final int colCount = (new Spending()).getFieldCount();

        /**
         * Список расходов для договора
         *
         * @return Расходы
         */
        public Set<Spending> getSpendings() {
            return spendings;
        }

        /**
         * Установить расходы для договора
         *
         * @param spendings Расходы
         */
        public void setSpendings(Set<Spending> spendings) {
            this.spendings = spendings;
        }

        /**
         * Returns the number of rows in the model.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return spendings.size();
        }

        /**
         * Returns the number of columns in the model.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return colCount;
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         *
         * @param rowIndex    the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if ((columnIndex < colCount) && (rowIndex < spendings.size())) {
                return ((Spending) spendings.toArray()[rowIndex]).getFieldById(columnIndex);
            } else
                return null;
        }

        /**
         * Returns a default name for the column using spreadsheet conventions:
         * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
         * returns an empty string.
         *
         * @param column the column being queried
         * @return a string containing the default name of <code>column</code>
         */
        @Override
        public String getColumnName(int column) {
            return (new Spending()).getFieldNameById(column);
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return (new Spending()).getFieldClassById(columnIndex);
        }
    }

    private class IncomingTableModel extends AbstractTableModel {

        public Set<Incoming> getIncomings() {
            return incomings;
        }

        public void setIncomings(Set<Incoming> incomings) {
            this.incomings = incomings;
            fireTableDataChanged();
        }

        Set<Incoming> incomings = new HashSet<Incoming>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        /**
         * Returns the number of rows in the model.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return incomings.size();
        }

        /**
         * Returns the number of columns in the model.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         *
         * @param rowIndex    the row whose value is to be queried
         * @param columnIndex the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if ((incomings.size() > rowIndex) && (columnIndex < 3)) {
                Incoming inc = (Incoming) incomings.toArray()[rowIndex];
                switch (columnIndex) {
                    case 0:
                        return sdf.format(inc.getIncomingDate());
                    case 1:
                        return CommonUtils.formatCurrency(inc.getIncomingSum());
                    case 2:
                        return inc.getIncomingComment();
                    default:
                        return null;
                }
            } else
                return null;
        }

        /**
         * Returns a default name for the column using spreadsheet conventions:
         * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
         * returns an empty string.
         *
         * @param column the column being queried
         * @return a string containing the default name of <code>column</code>
         */
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Дата";
                case 1:
                    return "Сумма";
                case 2:
                    return "Комментарий";
                default:
                    return null;
            }
        }

        /**
         * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
         *
         * @param columnIndex the column being queried
         * @return the Object.class
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        /**
         * Returns false.  This is the default implementation for all cells.
         *
         * @param rowIndex    the row being queried
         * @param columnIndex the column being queried
         * @return false
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

}
