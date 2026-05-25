package CurrencyParserPackage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;

public class CurrencyParser {
    static void main(String[] args) {
        try {
            String url = "https://www.cbr.ru/scripts/XML_daily.asp"; //путь к xml центробанка

            String fileName = "currencies.xml"; //наименование файла куда будут сохранены данные

            System.out.println("Получение XML с сайта ЦБ РФ...");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(url);
            document.getDocumentElement().normalize();

            System.out.println("XML успешно получен.");

            System.out.println(document.getDocumentElement().getNodeName());
            //System.out.println(document.getElementsByTagName("Name").item(10).getTextContent());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

            System.out.println("Список валют сохранен в файле: " + fileName);

        }
        catch( Exception e) {
            System.out.println("Произошла ошибка:");
            System.out.println(e.getMessage());
        }
    }
}
