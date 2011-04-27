package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.Spending;
import ru.lsv.finARM.ui.MainForm;
import ru.lsv.finARM.ui.ReportViewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Отчет по незакрытым договорам
 */
public class NonFinishedOperationsReport {
    /**
     * Формирование отчета
     *
     * @param frameComp    см. @ru.lsv.finARM.ui.ReportViewer
     * @param locationComp см. @ru.lsv.finARM.ui.ReportViewer
     * @param period       За какой период он формируется
     * @param operations   Список финансовых операций за этот период
     * @throws java.io.IOException В случае ошибок ввода-вывода при формировании и сохранении XML
     * @throws javax.xml.parsers.ParserConfigurationException
     *                             см. @javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
     * @throws javax.xml.transform.TransformerException
     *                             см. @javax.xml.transform.Transformer
     * @throws org.hibernate.HibernateException
     *                             В случае происхождения чего-нито при работе с Hibernate
     */
    public void makeReport(Component frameComp,
                           Component locationComp,
                           String period, java.util.List<FinancialOperation> operations) throws ParserConfigurationException, IOException, TransformerException, HibernateException {
        // Формируем XML с отчетом...
        File file = new File("NonFinished.toReport");
        DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document doc = impl.createDocument(null, null, null);

        Element root = doc.createElement("operations");
        root.setAttribute("period", period);
        doc.appendChild(root);
        //
        Session sess = HibernateUtils.openSession();
        // Поехали по операциям
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        for (FinancialOperation op : operations) {
            if ((op.getKind() == 0)&&(!op.getClosed())) {
                op = (FinancialOperation) sess.get(FinancialOperation.class, op.getFoId());
                Element e1 = doc.createElement("operation");
                e1.setAttribute("customer", op.getCustomer());
                e1.setAttribute("order", op.getOrderNum());
                e1.setAttribute("date", sdf.format(op.getOperationDate()));
                e1.setAttribute("sum", CommonUtils.formatDouble(op.getOperationSum()));
                if (op.getPaymentType() == 0) {
                    e1.setAttribute("txtSum", CommonUtils.formatCurrency(op.getOperationSum())+"(нал.)");
                } else {
                    e1.setAttribute("txtSum", CommonUtils.formatCurrency(op.getOperationSum())+"(безнал.)");                    
                }
                double total = 0;
                for (Spending spend : op.getSpendings()) {
                    total = total + spend.getPaymentSum();
                }
                e1.setAttribute("spend", CommonUtils.formatDouble(total));
                double rest = op.getOperationSum() - total;
                if (rest < 0) rest = 0;
                e1.setAttribute("rest", CommonUtils.formatDouble(rest));
                e1.setAttribute("manager", ""+op.getManager());
                root.appendChild(e1);
            }
        }
        sess.close();
        //
        // Сохраняем
        CommonUtils.saveXML(file, doc);
        //
        // Показываем
        ReportViewer.showPreview(frameComp, locationComp, "NonFinished", "NonFinished.toReport");
    }

}
