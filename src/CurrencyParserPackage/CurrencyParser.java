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
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrencyParser {


    public static Document parseUrl(String url) {  //Метод для пасринга урл центробанка и запись результатов в document
        try{
            System.out.println("Получение XML с сайта ЦБ РФ...");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(url);
            document.getDocumentElement().normalize();

            System.out.println("XML успешно получен.");
            System.out.println("Корневой элемент: " + document.getDocumentElement().getNodeName());

            return document;
        }
        catch (Exception e){
            System.out.println("Произошла ошибка при получении XML:");
            System.out.println(e.getMessage());
        }
        return null;
    }


//метод для создания XML файла на основе выбранных валют
    public static void createXml(ArrayList<Element> selectedCurrencies) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document newDocument = builder.newDocument();

            Element root = newDocument.createElement("SelectedCurrencies");
            newDocument.appendChild(root);

            for (int i = 0; i < selectedCurrencies.size(); i++) {
                Element oldValute = selectedCurrencies.get(i);

                Node copiedValute = newDocument.importNode(oldValute, true);

                root.appendChild(copiedValute);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(newDocument);
            StreamResult result = new StreamResult(new File("currencies.xml"));

            transformer.transform(source, result);

            System.out.println("Выбранные валюты сохранены в XML-файл: currencies.xml");

        } catch (Exception e) {
            System.out.println("Произошла ошибка при сохранении XML:");
            System.out.println(e.getMessage());
        }

    }

    //метод для получения списка всех валют
    public static ArrayList<Element> getAllCurrencies(Document document) {
        ArrayList<Element> currencies = new ArrayList<>();

        NodeList valuteList = document.getElementsByTagName("Valute");

        for (int i = 0; i < valuteList.getLength(); i++) {
            Node node = valuteList.item(i);
            Element valute = (Element) node;

            currencies.add(valute);
        }

        return currencies;
    }

    //вспомогательный метод чтобы не писать всегда valute.getElementsByTagName("CharCode").item(0).getTextContent()
    public static String getTagValue(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }


    //метод для создания эксель файла на основе выбранных валют
    public static void createExcel(ArrayList<Element> selectedCurrencies){
        try {
            String excelFileName = "rates.xlsx";

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Курсы валют");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Дата");
            header.createCell(1).setCellValue("Цифровой код");
            header.createCell(2).setCellValue("Буквенный код");
            header.createCell(3).setCellValue("Название");
            header.createCell(4).setCellValue("Номинал");
            header.createCell(5).setCellValue("Курс за номинал");
            header.createCell(6).setCellValue("Курс за 1 единицу валюты");

            int rowNumber = 1;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String currentDate = LocalDateTime.now().format(formatter);

            for (int i = 0; i < selectedCurrencies.size(); i++) {

                Element valute = selectedCurrencies.get(i);

                String numCode = valute.getElementsByTagName("NumCode").item(0).getTextContent();
                String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent();
                String nominal = valute.getElementsByTagName("Nominal").item(0).getTextContent();
                String name = valute.getElementsByTagName("Name").item(0).getTextContent();
                String value = valute.getElementsByTagName("Value").item(0).getTextContent();

                double valueNumber = Double.parseDouble(value.replace(",", "."));
                double rateForOneUnit = calculateRateForOneUnit(value, nominal);

                Row row = sheet.createRow(rowNumber);

                row.createCell(0).setCellValue(currentDate);
                row.createCell(1).setCellValue(numCode);
                row.createCell(2).setCellValue(charCode);
                row.createCell(3).setCellValue(name);
                row.createCell(4).setCellValue(Integer.parseInt(nominal));
                row.createCell(5).setCellValue(valueNumber);
                row.createCell(6).setCellValue(rateForOneUnit);

                rowNumber++;
            }

            for (int i = 0; i <= 6; i++) { //выравнивание колонок по их длине
               sheet.autoSizeColumn(i);
            }

            FileOutputStream outputStream = new FileOutputStream(excelFileName);

            workbook.write(outputStream);

            workbook.close();
            outputStream.close();

            System.out.println("Выбранные валюты сохранены в Excel-файл: " + excelFileName);

        } catch (Exception e) {
            System.out.println("Произошла ошибка при создании Excel-файла:");
            System.out.println(e.getMessage());
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
