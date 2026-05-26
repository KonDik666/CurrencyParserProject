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
import java.util.Scanner;

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

            System.out.println("Интересует курс конкретной валюты? y/n ");

            Scanner in = new Scanner(System.in);
            String response = in.nextLine();
            if (response.equals("y")) {
                NodeList valuteList = document.getElementsByTagName("Valute");
                getCurrency(valuteList);
            }

        }
        catch( Exception e) {
            System.out.println("Произошла ошибка:");
            System.out.println(e.getMessage());
        }
    }

    public static void getCurrency(NodeList valuteList){  //в метод передаем NodeList с валютами
        System.out.println("Список доступных валют:");
        System.out.println("------------------------------");

        for(int i = 0; i < valuteList.getLength(); i++){ //вывод всех валют для удобства
            Node node = valuteList.item(i);

            Element valute = (Element) node;

            String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent();

            String name = valute.getElementsByTagName("Name").item(0).getTextContent();

            System.out.println(charCode + " - " + name);
        }

        System.out.println("------------------------------");
        System.out.println();

        Scanner in = new Scanner(System.in);

        System.out.print("Введите код валюты для поиска, например USD: ");
        String userCode = in.nextLine().trim().toUpperCase();

        boolean isFound = false;

        for (int i = 0; i < valuteList.getLength(); i++) {  //поиск валюты введенной пользователем
            Node node = valuteList.item(i);

            Element valute = (Element) node;

            String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent();

            if (charCode.equalsIgnoreCase(userCode)) {
                String numCode = valute.getElementsByTagName("NumCode").item(0).getTextContent();
                String nominal = valute.getElementsByTagName("Nominal").item(0).getTextContent();
                String name = valute.getElementsByTagName("Name").item(0).getTextContent();
                String value = valute.getElementsByTagName("Value").item(0).getTextContent();

                System.out.println();
                System.out.println("Валюта найдена:");
                System.out.println("------------------------------");
                System.out.println("Цифровой код: " + numCode);
                System.out.println("Буквенный код: " + charCode);
                System.out.println("Название: " + name);
                System.out.println("Номинал: " + nominal);
                System.out.println("Курс: " + value);
                System.out.println("------------------------------");

                //double rateForOneUnit = calculateRateForOneUnit(value, nominal);
                //double res = calculateRubles(10, rateForOneUnit);
                //System.out.println("10 " + charCode + " это: " + res + " руб.");

                isFound = true;
                break;
            }

        }

        if (!isFound) {
            System.out.println();
            System.out.println("Валюта с кодом " + userCode + " не найдена.");
            System.out.println("Проверьте правильность введенного кода.");
        }



    }

    public static double calculateRateForOneUnit(String value, String nominal) {   //метод для подсчета курса за 1 единицу валюты (для тех валют у которых курс более чем за 1 единицу, например, Иены)
        double valueNumber = Double.parseDouble(value.replace(",", "."));
        int nominalNumber = Integer.parseInt(nominal);

        return valueNumber / nominalNumber;
    }

    public static double calculateRubles(double amount, double rateForOneUnit) {  //Метод переводит сумму в выбранной валюте в рубли
        return amount * rateForOneUnit;
    }

    public static double calculateCurrencyAmount(double rubles, double rateForOneUnit) { //Метод переводит сумму в рублях в выбранную валюту
        if (rateForOneUnit <= 0) {
            return 0;
        }

        return rubles / rateForOneUnit;
    }


}
