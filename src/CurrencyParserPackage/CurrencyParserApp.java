package CurrencyParserPackage;

import org.w3c.dom.Document;

import javax.swing.SwingUtilities;

public class CurrencyParserApp {

    public static void main(String[] args) {

        String url = "https://www.cbr.ru/scripts/XML_daily.asp";

        Document doc = CurrencyParser.parseUrl(url);

        if (doc == null) {
            System.out.println("XML-документ не был получен. Программа завершена.");
            return;
        }

        SwingUtilities.invokeLater(() -> CurrencyParserGUI.createInterface(doc));
    }
}