package ru.lsv.finARM.ui;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.common.UserRoles;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.FinancialMonth;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.reports.FinancialResultsReport;
import ru.lsv.finARM.reports.NonFinishedOperationsReport;
import ru.lsv.finARM.reports.PayrollReport;
import ru.lsv.finARM.reports.PlannedMonthSpendingReport;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Основная форма
 */
public class MainForm implements ActionListener {
    private JPanel mainPanel;
    private JPanel selectedPeriodPanel;
    private JButton setPeriodBtn;
    private JTable finOpTable;
    private JPanel btnPanel;
    private JButton addOperationBtn;
    private JButton addPrepaymentBtn;
    private JButton addSpendingBtn;
    private JButton editBtn;
    private JButton delBtn;
    private JButton colorSetupBtn;
    private JScrollPane finOpTableSP;
    private JLabel periodLabel;

    private MainForm_ColorSetup colors;
    //
    private JMenuItem shortSalaryMI = null;
    private JMenuItem fullSalaryMI = null;
    //
    //boolean isDirector = false;
    UserRoles userRole;
    // Константы пунктов меню
    private static final String managersMIText = "Менеджеры";
    private static final String spendingTemplatesMIText = "Шаблон месячных расходов";
    private static final String monthSpendingMIText = "Планирумые расходы";
    private static final String closeMonthMIText = "Закрыть текущий месяц";
    private static final String openMonthMIText = "Открыть последний закрытый месяц";
    private static final String reports_plannedMonthSpendingMIText = "По планируемым расходам";
    private static final String closeMIText = "Завершить работу";
    private static final String aboutMIText = "О программе...";
    private static final String reports_salaryMIText = "Зарплатная ведомость";
    private static final String reports_fullSalaryMIText = "Полная зарплатная ведомость";
    private static final String reports_nonClosedOperationsMIText = "По незакрытым договорам";
    private static final String reports_financialResultsMIText = "Финансовые результаты";
    private static final String usersMIText = "Пользователи";
    private static final String changePswMIText = "Изменить пароль";

    /**
     * Параметры установленного временного фильтра
     *
     * @return Временной фильтр
     */
    public TimeFilterParams getTimeFilterParams() {
        return timeFilterParams;
    }

    /**
     * Параметры временного фильтра
     */
    private TimeFilterParams timeFilterParams = null;

    public MainForm() {
        //
        finOpTable.setModel(new FinancialOperationTableModel());
        finOpTable.setDefaultRenderer(String.class, new FinancialOperationRenderer(true));
        finOpTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        finOpTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doEdit();
                }
            }
        });
        //
        addPrepaymentBtn.addActionListener(this);
        addSpendingBtn.addActionListener(this);
        addOperationBtn.addActionListener(this);
        //
        editBtn.addActionListener(this);
        //
        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelete();
            }
        });
        colorSetupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (colors.show(mainPanel)) finOpTable.repaint();
            }
        });
        setPeriodBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm_TimeFilterParams params = new MainForm_TimeFilterParams(frame);
                TimeFilterParams tmp = params.doEdit(timeFilterParams, mainPanel);
                if (tmp != null) {
                    timeFilterParams = tmp;
                    loadData(null, false);
                }
            }
        });
    }

    /**
     * Удаление финансовой операции
     */
    private void doDelete() {
        if (finOpTable.getSelectedRow() != -1) {
            FinancialOperation fo = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations().get(finOpTable.getSelectedRow());
            String msg = "Вы уверены, что хотите удалить " + fo + "?";
            if (JOptionPane.showConfirmDialog(mainPanel, msg, "Удаление", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                Session sess = null;
                Transaction trx = null;
                try {
                    sess = HibernateUtils.openSession();
                    trx = sess.beginTransaction();
                    sess.delete(fo);
                    sess.flush();
                    trx.commit();
                    trx = null;
                    sess.close();
                    sess = null;
                    loadData(null, false);
                } catch (HibernateException ex) {
                    if (trx != null) trx.rollback();
                    if (sess != null) sess.close();
                }
            }
        }
    }

    /**
     * Редактирование финансовой операции
     */
    private void doEdit() {
        if (finOpTable.getSelectedRow() != -1) {
            FinancialOperation fo = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations().get(finOpTable.getSelectedRow());
            switch (fo.getKind()) {
                case 0: {
                    // Редактируем договор
                    FinancialOperationParam_Operation param = new FinancialOperationParam_Operation(frame);
                    fo = param.doEdit(fo, mainPanel, userRole, addOperationBtn.isEnabled());
                    if (fo != null) {
                        saveToDB(fo, 1);
                    }
                    break;
                }
                case 1: {
                    // Редактируем аванс
                    FinancialOperationParam_Prepaid param = new FinancialOperationParam_Prepaid(frame);
                    fo = param.doEdit(fo, mainPanel, addOperationBtn.isEnabled());
                    if (fo != null) {
                        fo.setCloseDate(fo.getOperationDate());
                        // Поехали сохранять
                        saveToDB(fo, 1);
                    }
                    break;
                }
                case 2: {
                    // Редактируем расход
                    FinancialOperationParam_Spending param = new FinancialOperationParam_Spending(frame);
                    fo = param.doEdit(fo, mainPanel, addOperationBtn.isEnabled());
                    if (fo != null) {
                        fo.setCloseDate(fo.getOperationDate());
                        // Поехали сохранять
                        saveToDB(fo, 1);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Возвращает frame для формы
     *
     * @return см.описание
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Frame для формы
     */
    private JFrame frame;

    /**
     * Построение фрейма
     */
    public void buildFrame() {
        frame = new JFrame("Финансовый АРМ");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Завершить работу с системой?", "Завершение работы", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        frame.setJMenuBar(buildMenu());
        //
        //frame.pack();
        frame.setBounds(10, 10, 1100, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        colors = new MainForm_ColorSetup(frame);

        // Устанавливаем временной промежуток для просмотра
        timeFilterParams = new TimeFilterParams(FinancialMonths.getInstance().getActiveMonth().getMonth(),
                FinancialMonths.getInstance().getActiveMonth().getYear());
        //
        loadData(null, false);
    }

    /**
     * Построение меню
     *
     * @return Сформированное меню
     */
    private JMenuBar buildMenu() {
        // Меню будем строить в зависимости от того, какая роль ща у нашего текущего пользователя
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            List<String> res = sess.createSQLQuery("SELECT role_name FROM information_schema.applicable_roles where grantee=current_user").list();
            if (res.size() != 1) {
                // Что-то странное. Какой-то текущий мутант - будем считать что это НЕ директор
                throw new HibernateException("Strange user - has no or more than one roles");
            }
            if (res.get(0).equals("armDirectors"))
                userRole = UserRoles.DIRECTOR;
            else if (res.get(0).equals("armUsers"))
                userRole = UserRoles.USER;
            else if (res.get(0).equals("armViewers"))
                userRole = UserRoles.VIEWER;
            else {
                // Что-то странное. Какой-то текущий мутант - будем считать что это НЕ директор
                throw new HibernateException("Strange user - has strange role");
            }
            sess.close();
            sess = null;
        } catch (HibernateException ex) {
            userRole = UserRoles.VIEWER;
            if (sess != null) sess.close();
        }
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem item;
        //if (isDirector) {
        // Это могут видеть только директора или пользователи. "Просмотрщики" не видят ничего
        if ((userRole == UserRoles.DIRECTOR) || (userRole == UserRoles.USER)) {
            menu = new JMenu("Справочники");
            item = new JMenuItem(managersMIText);
            item.addActionListener(this);
            menu.add(item);
            item = new JMenuItem(spendingTemplatesMIText);
            item.addActionListener(this);
            menu.add(item);
            //
            if (userRole == UserRoles.DIRECTOR) {
                menu.addSeparator();
                item = new JMenuItem(usersMIText);
                item.addActionListener(this);
                menu.add(item);
            }
            //
            menuBar.add(menu);
            //
            menu = new JMenu("Помесячное");
            item = new JMenuItem(monthSpendingMIText);
            item.addActionListener(this);
            menu.add(item);
            menu.addSeparator();
            item = new JMenuItem(closeMonthMIText);
            item.addActionListener(this);
            menu.add(item);
            item = new JMenuItem(openMonthMIText);
            item.addActionListener(this);
            menu.add(item);
            menuBar.add(menu);
            //}
            //
            //
            menu = new JMenu("Отчеты");
            shortSalaryMI = new JMenuItem(reports_salaryMIText);
            shortSalaryMI.addActionListener(this);
            menu.add(shortSalaryMI);
//        if (isDirector) {
            fullSalaryMI = new JMenuItem(reports_fullSalaryMIText);
            fullSalaryMI.addActionListener(this);
            menu.add(fullSalaryMI);
            menu.addSeparator();
            item = new JMenuItem(reports_plannedMonthSpendingMIText);
            item.addActionListener(this);
            menu.add(item);
            menu.addSeparator();
            item = new JMenuItem(reports_nonClosedOperationsMIText);
            item.addActionListener(this);
            menu.add(item);
            if (userRole == UserRoles.DIRECTOR) {
                menu.addSeparator();
                item = new JMenuItem(reports_financialResultsMIText);
                item.addActionListener(this);
                menu.add(item);
            }
            menuBar.add(menu);
        }
        //
        //
        menu = new JMenu("Дополнительное");
        item = new JMenuItem(closeMIText);
        item.addActionListener(this);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem(changePswMIText);
        //item = new JMenuItem(aboutMIText);
        item.addActionListener(this);
        menu.add(item);
        menuBar.add(menu);
        //
        return menuBar;
    }

    /**
     * см. @java.awt.event.ActionListener
     *
     * @param e см. @java.awt.event.ActionListener
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (managersMIText.equals(e.getActionCommand())) {
            ManagersCatalog mCatalogTemp = new ManagersCatalog(frame);
            if (mCatalogTemp.show(mainPanel)) {
                // Тут надо будет отрефрешить основной dataset
                loadData(null, false);
            }
        } else if (spendingTemplatesMIText.equals(e.getActionCommand())) {
            SpendingTemplatesCatalog spCatalogTemp = new SpendingTemplatesCatalog(frame);
            spCatalogTemp.showCatalog(mainPanel);
        } else if (monthSpendingMIText.equals(e.getActionCommand())) {
            MonthSpendingCatalog msc = new MonthSpendingCatalog(frame);
            msc.showCatalog(mainPanel);
        } else if (addPrepaymentBtn.getText().equals(e.getActionCommand())) {
            // Обрабатываем добавление аванса
            doAddPrepaid_Spending(1);
        } else if (addSpendingBtn.getText().equals(e.getActionCommand())) {
            doAddPrepaid_Spending(2);
        } else if (addOperationBtn.getText().equals(e.getActionCommand())) {
            doAddOperation();
        } else if (editBtn.getText().equals(e.getActionCommand())) {
            doEdit();
        } else if (closeMonthMIText.equals(e.getActionCommand())) {
            closeCurrentMonth();
        } else if (openMonthMIText.equals(e.getActionCommand())) {
            openPrevMonth();
        } else if (reports_plannedMonthSpendingMIText.equals(e.getActionCommand())) {
            PlannedMonthSpendingReport report = new PlannedMonthSpendingReport();
            try {
                report.makeReport(mainPanel, mainPanel, periodLabel.getText(), ((FinancialOperationTableModel) finOpTable.getModel()).getOperations(), timeFilterParams);
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "При формировании отчета произошла ошибка");
            }
        } else if (reports_salaryMIText.equals(e.getActionCommand())) {
            PayrollReport report = new PayrollReport();
            try {
                report.makeReport(mainPanel, mainPanel, periodLabel.getText(), ((FinancialOperationTableModel) finOpTable.getModel()).getOperations(), timeFilterParams, false);
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "При формировании отчета произошла ошибка");
            }
        } else if (reports_fullSalaryMIText.equals(e.getActionCommand())) {
            PayrollReport report = new PayrollReport();
            try {
                report.makeReport(mainPanel, mainPanel, periodLabel.getText(), ((FinancialOperationTableModel) finOpTable.getModel()).getOperations(), timeFilterParams, true);
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "При формировании отчета произошла ошибка");
            }
        } else if (reports_nonClosedOperationsMIText.equals(e.getActionCommand())) {
            NonFinishedOperationsReport report = new NonFinishedOperationsReport();
            try {
                report.makeReport(mainPanel, mainPanel, periodLabel.getText(), ((FinancialOperationTableModel) finOpTable.getModel()).getOperations());
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "При формировании отчета произошла ошибка");
            }
        } else if (reports_financialResultsMIText.equals(e.getActionCommand())) {
            FinancialResultsReport report = new FinancialResultsReport();
            try {
                report.makeReport(mainPanel, mainPanel, periodLabel.getText(), ((FinancialOperationTableModel) finOpTable.getModel()).getOperations(), timeFilterParams);
            } catch (Exception e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null, "При формировании отчета произошла ошибка");
            }
        } else if (closeMIText.equals(e.getActionCommand())) {
            if (JOptionPane.showConfirmDialog(null, "Завершить работу с системой?", "Завершение работы", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else if (changePswMIText.equals(e.getActionCommand())) {
            doPasswordChange();
        } else if (usersMIText.equals(e.getActionCommand())) {
            UsersCatalog catalog = new UsersCatalog("", frame);
            catalog.showCatalog(mainPanel);
        }
    }

    /**
     * Меняет пароль для текущего юзера
     */
    private void doPasswordChange() {
        ChangePassword cngPsw = new ChangePassword(frame);
        cngPsw.doChange(mainPanel);
    }

    /**
     * Открывает предыдущий месяц
     */
    private void openPrevMonth() {
        // Вначале проверим - а нет ли закрытых данных за текущий месяц?
        Session sess = null;
        try {
            sess = HibernateUtils.openSession();
            FinancialMonth fm = FinancialMonths.getInstance().getActiveMonth();
            List<FinancialOperation> fos = sess.createQuery("from FinancialOperation where closed=true AND closeMonth=? AND closeYear=?").
                    setInteger(0, fm.getMonth()).
                    setInteger(1, fm.getYear()).
                    list();
            sess.close();
            sess = null;
            if (fos.size() > 0) {
                String[] msgs = {"В текущем месяце уже присутствуют данные (занесен аванс, расход или закрыт договор).",
                        "Откройте договор, удалите авансы и расходы.",
                        "Открытие предыдущего месяца возможно только при отсутствии каких-либо данных в текущем месяце."};
                JOptionPane.showMessageDialog(mainPanel, msgs, "Открытие месяца", JOptionPane.ERROR_MESSAGE);
            } else {
                String[] msgs = {"Вы уверены, что хотите открыть предыдущий месяц?",
                        "Открытие месяца приведет к полному удалению планируемых расходов за текущий месяц!"};
                if (JOptionPane.showConfirmDialog(mainPanel, msgs, "Открытие месяца",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (FinancialMonths.getInstance().openMonth() == 0) {
                        timeFilterParams = new TimeFilterParams(FinancialMonths.getInstance().getActiveMonth().getMonth(),
                                FinancialMonths.getInstance().getActiveMonth().getYear());
                        loadData(null, false);
                        JOptionPane.showMessageDialog(mainPanel, "Месяц успешно открыт", "Открытие месяца", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainPanel, "При открытии месяца произошли ошибки", "Открытие месяца", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        } catch (HibernateException ex) {
            if (sess != null) sess.close();
        }
    }

    /**
     * Закрытие текущего месяца
     */
    private void closeCurrentMonth() {
        if (JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите закрыть текущий месяц (" + periodLabel.getText() + ")?", "Закрытие месяца",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (FinancialMonths.getInstance().closeMonth() == 0) {
                timeFilterParams = new TimeFilterParams(FinancialMonths.getInstance().getActiveMonth().getMonth(),
                        FinancialMonths.getInstance().getActiveMonth().getYear());
                loadData(null, false);
                JOptionPane.showMessageDialog(null, "Месяц закрыт успешно", "Закрытие месяца", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "При закрытии месяца произошли ошибки", "Закрытие месяца", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Добавление операции (договора)
     */
    private void doAddOperation() {
        FinancialOperation fo = new FinancialOperation();
        fo.setKind(0);
        FinancialOperationParam_Operation params = new FinancialOperationParam_Operation(frame);
        fo = params.doEdit(fo, mainPanel, userRole, true);
        if (fo != null) {
            // Сохраняем
            saveToDB(fo, 0);
            // А теперь его и выделим
            List<FinancialOperation> fos = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations();
            for (int i = 0; i < fos.size(); i++) {
                FinancialOperation foTmp = fos.get(i);
                if (foTmp.getFoId().equals(fo.getFoId())) {
                    finOpTable.getSelectionModel().setAnchorSelectionIndex(i);
                    break;
                }
            }
        }
    }

    /**
     * Добавляет аванс или расход
     *
     * @param what 1 - аванс, 2 - расход
     */
    private void doAddPrepaid_Spending(int what) {
        FinancialOperation fo = new FinancialOperation();
        fo.setKind(what);
        fo.setClosed(true);
        fo.setCloseMonth(FinancialMonths.getInstance().getActiveMonth().getMonth());
        fo.setCloseYear(FinancialMonths.getInstance().getActiveMonth().getYear());
        fo.setOperationSum(0.0);
        fo.setOperationDate(new Date(Calendar.getInstance().getTimeInMillis()));
        switch (what) {
            case 1: {
                FinancialOperationParam_Prepaid prepaidParam = new FinancialOperationParam_Prepaid(frame);
                fo = prepaidParam.doEdit(fo, mainPanel, true);
                break;
            }
            case 2: {
                FinancialOperationParam_Spending param = new FinancialOperationParam_Spending(frame);
                fo = param.doEdit(fo, mainPanel, true);
                break;
            }
            default:
                fo = null;
        }
        if (fo != null) {
            fo.setCloseDate(fo.getOperationDate());
            // Поехали сохранять
            saveToDB(fo, 0);
            // А теперь его и выделим
            List<FinancialOperation> fos = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations();
            for (int i = 0; i < fos.size(); i++) {
                FinancialOperation foTmp = fos.get(i);
                if (foTmp.getFoId().equals(fo.getFoId())) {
                    finOpTable.getSelectionModel().setAnchorSelectionIndex(i);
                    break;
                }
            }
        }
    }


    /**
     * Сохраняет операцию в базу
     *
     * @param fo        Операция для сохранения
     * @param operation 0 - добавление, 1 - изменение
     */
    private void saveToDB(FinancialOperation fo, int operation) {
        Session sess = null;
        Transaction trx = null;
        try {
            sess = HibernateUtils.openSession();
            trx = sess.beginTransaction();
            switch (operation) {
                case 0: {
                    sess.save(fo);
                }
                case 1: {
                    sess.update(fo);
                }
            }
            sess.flush();
            trx.commit();
            trx = null;
            switch (operation) {
                case 0:
                    loadData(sess, false);
                case 1:
                    loadData(null, true);
            }
        } catch (HibernateException ex) {
            if (trx != null) trx.rollback();
            if (sess != null) sess.close();
        }
    }

    /**
     * Загружает данные в модель
     *
     * @param session      Сессия. Если null - будет создана унутре
     * @param savePosition Сохранять или нет позицию выделения
     */
    private void loadData(Session session, boolean savePosition) {
        // До кучи - тут будем еще и обновлять информацию о периоде
        if ((timeFilterParams.getBeginDate() == null) &&
                (timeFilterParams.getSelectedMonth() == FinancialMonths.getInstance().getActiveMonth().getMonth()) &&
                (timeFilterParams.getSelectedYear() == FinancialMonths.getInstance().getActiveMonth().getYear())) {
            // Текущий месяц. Можем делать все, что приспичит
            periodLabel.setForeground(Color.GREEN.darker());
            doPeriodEnable(true);
        } else {
            periodLabel.setForeground(Color.RED.darker());
            doPeriodEnable(false);
        }
        periodLabel.setText("" + timeFilterParams);
        //
        int storedId = -1;
        if (finOpTable.getSelectedRow() != -1) {
            storedId = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations().get(finOpTable.getSelectedRow()).getFoId();
        }
        // Получаем список менеджеров
        Session sess = null;
        if (session != null) {
            sess = session;
        }
        try {
            if (session != null) sess = session;
            else sess = HibernateUtils.openSession();
            // Установим временной фильтр
            Query query;
            if (timeFilterParams.getBeginDate() == null) {
                query = sess.createQuery("from FinancialOperation where (closed=false)OR((closed=true) AND (closeYear=?) AND (closeMonth=?)) order by operationDate, foId")
                        .setInteger(0, timeFilterParams.getSelectedYear())
                        .setInteger(1, timeFilterParams.getSelectedMonth());
            } else {
                query = sess.createQuery("from FinancialOperation where (closed=false)OR((closed=true)AND(closeDate >= ?)AND(closeDate <= ?))OR((kind=0)AND(operationDate >= ?)AND(operationDate <= ?)) order by operationDate, foId")
                        .setDate(0, timeFilterParams.getBeginDate())
                        .setDate(1, timeFilterParams.getEndDate())
                        .setDate(2, timeFilterParams.getBeginDate())
                        .setDate(3, timeFilterParams.getEndDate());
            }
            java.util.List<FinancialOperation> operations = query.list();
            ((FinancialOperationTableModel) finOpTable.getModel()).setOperations(operations);
            ((FinancialOperationTableModel) finOpTable.getModel()).fireTableDataChanged();
            if ((savePosition) && (storedId != -1)) {
                // Восстановим выделение
                for (int i = 0; i < operations.size(); i++) {
                    if (operations.get(i).getFoId() == storedId) {
                        finOpTable.getSelectionModel().setSelectionInterval(i, i);
                        break;
                    }
                }
            }
        } finally {
            if ((session == null) && (sess != null)) sess.close();
        }

    }

    /**
     * Включает / выключает контролы в зависимости от выбранного периода и текущего пользователя!
     *
     * @param enable Включить / выключить
     */
    private void doPeriodEnable(boolean enable) {
        // Просмотрщик не может смотреть ничего!
        if (userRole == UserRoles.VIEWER) enable = false;
        addOperationBtn.setEnabled(enable);
        addPrepaymentBtn.setEnabled(enable);
        addSpendingBtn.setEnabled(enable);
        delBtn.setEnabled(enable);

        //editBtn.setEnabled(enable);
    }

    private class FinancialOperationTableModel extends AbstractTableModel {

        private List<FinancialOperation> operations = new ArrayList<FinancialOperation>();
        private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        private final int colCount = 7;

        /**
         * Получение данных из модели
         *
         * @return Данные
         */
        public List<FinancialOperation> getOperations() {
            return operations;
        }

        /**
         * Загрузка данных в модель
         *
         * @param operations Данные
         */
        public void setOperations(List<FinancialOperation> operations) {
            this.operations = operations;
        }

        /**
         * Returns the number of rows in the model.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return operations.size();
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
            if ((rowIndex < operations.size()) && (columnIndex < colCount)) {
                FinancialOperation op = operations.get(rowIndex);
                switch (columnIndex) {
                    case 0: {
                        switch (op.getKind()) {
                            case 0:
                                return op.getCustomer();
                            case 1:
                                return "Аванс";
                            case 2:
                                if (op.getPlannedSpending() != null) return "" + op.getPlannedSpending();
                                else return op.getNonPlannedSpending();
                        }
                    }
                    case 1:
                        return sdf.format(op.getOperationDate());
                    case 2:
                        return CommonUtils.formatCurrency(op.getOperationSum());
                    case 3:
                        if (op.getManager() != null) return "" + op.getManager();
                        else return "-";
                    case 4:
                        if (op.getOrderNum() != null) return op.getOrderNum();
                        else return "-";
                    case 5:
                        if (op.getKind() == 0) {
                            if (op.getPaymentType() == 0) return "нал.";
                            else return "безнал.";
                        } else {
                            return "-";
                        }
                    case 6:
                        if (op.getKind() == 0) {
                            return CommonUtils.formatCurrency(op.getCurrentProfit());
                        } else
                            return CommonUtils.formatCurrency(-op.getOperationSum());
                    default:
                        return "";
                }
            } else return null;
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
                    return "Заказчик / операция";
                case 1:
                    return "Дата";
                case 2:
                    return "Сумма";
                case 3:
                    return "Менеджер";
                case 4:
                    return "Номер счета";
                case 5:
                    return "Нал/безнал";
                case 6:
                    return "Прибыль";
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
            /*switch (columnIndex) {
                case 1:
                    return Date.class;
                default:
                    return String.class;
            }*/
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

    private class FinancialOperationRenderer extends JLabel
            implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public FinancialOperationRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object object,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            if ((row >= 0) && (row < ((FinancialOperationTableModel) finOpTable.getModel()).getOperations().size())) {
                FinancialOperation op = ((FinancialOperationTableModel) finOpTable.getModel()).getOperations().get(row);
                if (op != null) {
                    switch (op.getKind()) {
                        case 0: {
                            if (op.getClosed()) setForeground(colors.getClosedColor());
                            else if (op.getClosedForSalary()) setForeground(colors.getClosedForSalaryColor());
                            else setForeground(colors.getOpenedColor());
                            break;
                        }
                        case 1: {
                            setForeground(colors.getPrepaidColor());
                            break;
                        }
                        case 2: {
                            setForeground(colors.getSpendingColor());
                            break;
                        }
                    }
                    if (object.getClass().equals(String.class)) {
                        setText((String) object);
                    }
                }
            }
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                if (getForeground().equals(Color.BLACK)) setForeground(Color.WHITE);
                else setForeground(getForeground().brighter());
            } else {
                setBackground(table.getBackground());
            }
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        /*selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());*/
                        selectedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        /*unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());*/
                        unselectedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

    /**
     * Класс - параметры временного фильтра
     */
    public class TimeFilterParams {

        /**
         * Период, определенный двумя датами
         *
         * @param beginDate Дата начала
         * @param endDate   Дата окончания
         */
        public TimeFilterParams(Date beginDate, Date endDate) {
            this.selectedMonth = -1;
            this.selectedYear = -1;
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        /**
         * Период, определенный месяцем
         *
         * @param selectedMonth Месяц
         * @param selectedYear  Год
         */
        public TimeFilterParams(int selectedMonth, int selectedYear) {
            this.beginDate = null;
            this.endDate = null;
            this.selectedMonth = selectedMonth;
            this.selectedYear = selectedYear;
        }

        /**
         * Установка параметров временного периода
         *
         * @param beginDate Дата начала
         * @param endDate   Дата окончания
         */
        public void setPeriod(Date beginDate, Date endDate) {
            this.selectedMonth = -1;
            this.selectedYear = -1;
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        /**
         * Установка параметров месяца
         *
         * @param selectedMonth Месяц
         * @param selectedYear  Год
         */
        public void setMonth(int selectedMonth, int selectedYear) {
            this.beginDate = null;
            this.endDate = null;
            this.selectedMonth = selectedMonth;
            this.selectedYear = selectedYear;
        }

        /**
         * Дата начала (null - если выбран месяц)
         *
         * @return Дата начала
         */
        public Date getBeginDate() {
            return beginDate;
        }

        /**
         * Дата окончания (null - если выбран месяц)
         *
         * @return Дата окончания
         */
        public Date getEndDate() {
            return endDate;
        }

        /**
         * Выбранный месяц (-1 - если выбраны даты)
         *
         * @return Выбранный месяц
         */
        public int getSelectedMonth() {
            return selectedMonth;
        }

        /**
         * Выбранный год (-1 - если выбраны даты)
         *
         * @return Выбранный год
         */
        public int getSelectedYear() {
            return selectedYear;
        }

        private int selectedMonth;
        private int selectedYear;
        private Date beginDate;
        private Date endDate;


        /**
         * Returns a string representation of the object.
         *
         * @return a string representation of the object.
         */
        @Override
        public String toString() {
            if (beginDate == null) {
                return CommonUtils.getMonthNameByIndex(selectedMonth) + " " + selectedYear;
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                return sdf.format(beginDate) + " - " + sdf.format(endDate);
            }
        }


    }

}
