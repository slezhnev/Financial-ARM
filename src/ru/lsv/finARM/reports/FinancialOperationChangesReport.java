package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.FinancialOperationChanges;
import ru.lsv.finARM.ui.ReportViewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Отчет по изменениям в финансовых операциях
 * User: Сергей
 * Date: 03.10.2011
 * Time: 15:15:47
 */
public class FinancialOperationChangesReport {

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
                           String period,
                           java.util.List<FinancialOperation> operations) throws ParserConfigurationException, IOException, TransformerException, HibernateException {
        // Формируем XML с отчетом...
        File file = new File("Changes.toReport");
        DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document doc = impl.createDocument(null, null, null);

        Element root = doc.createElement("changes");
        root.setAttribute("period", period);
        doc.appendChild(root);
        //
        Session sess = HibernateUtils.openSession();
        // Поехали по операциям
        ArrayList<Integer> foIds = new ArrayList<Integer>();
        for (FinancialOperation op : operations) {
            foIds.add(op.getFoId());
        }
        java.util.List<FinancialOperationChanges> changes = sess.createCriteria(FinancialOperationChanges.class)
                .add(Restrictions.in("foId", foIds) )
                .addOrder(Order.asc("foId"))
                .addOrder(Order.asc("changeDate"))
                .list();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        for (FinancialOperationChanges change : changes) {
            Element e1 = doc.createElement("change");
            e1.setAttribute("foId", "" + change.getFoId());
            e1.setAttribute("changeDate", sdf.format(change.getChangeDate()));
            e1.setAttribute("whomChanged", change.getWhomChanged());
            e1.setTextContent(change.getWhatChanged());
            root.appendChild(e1);
        }
        sess.close();
        //
        // Сохраняем
        CommonUtils.saveXML(file, doc);
        //
        // Показываем
        ReportViewer.showPreview(frameComp, locationComp, "Changes", "Changes.toReport");
    }

}
