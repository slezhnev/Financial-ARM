package ru.lsv.finARM.ui;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import net.sf.jasperreports.view.JRViewer;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Форма просмотра XML-отчетов
 */
public class ReportViewer extends JDialog {
    private JRViewer JRViewer1;
    private JPanel panel1;

    private String reportName;
    private String xmlName;

    public static void showPreview(Component frameComp,
                                   Component locationComp,
                                   String reportName,
                                   String xmlName) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        ReportViewer rp = new ReportViewer(frame, locationComp, reportName, xmlName);
        rp.fitPage();
        rp.setVisible(true);
    }

    /**
     * Создание просмотршика отчетов
     *
     * @param frame Где создавать форму
     * @param locationComp Относитель чего позиционироваться
     * @param reportName Название отчета (БЕЗ раширения!)
     * @param xmlName Имя XML документа с данными
     */
    public ReportViewer(Frame frame,
                        Component locationComp,
                        String reportName,
                        String xmlName) {
        super(frame, "Просмотр отчета", true);
        //
        this.reportName = reportName;
        this.xmlName = xmlName;
        getContentPane().add(panel1);
        setBounds(0, 0, 1100, 900);
        setLocationRelativeTo(locationComp);
    }

    private void createUIComponents() {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            Document document = JRXmlUtils.parse(JRLoader.getLocationInputStream("./"+xmlName));
            params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
            params.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, "dd/mm/yyyy");
            params.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, "#,##0.#");
            params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.getDefault());
            params.put(JRParameter.REPORT_LOCALE, Locale.getDefault());
            params.put("SUBREPORT_DIR", "./reports/");

            JasperPrint print = JasperFillManager.fillReport("./reports/"+reportName+".jasper", params);

            JRViewer1 = new JRViewer(print);
            //
            panel1 = new JPanel(new BorderLayout());
            panel1.add(JRViewer1);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    public void fitPage() {
        JRViewer1.setFitPageZoomRatio();
    }
}
