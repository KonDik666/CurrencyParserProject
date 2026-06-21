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
import java.io.FileWriter;
import java.io.PrintWriter;
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
            header.createCell(7).setCellValue("Дата сохраненного курса");
            header.createCell(8).setCellValue("Сохраненный курс");
            header.createCell(9).setCellValue("Изменение курса");

            int rowNumber = 1;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String currentDate = LocalDateTime.now().format(formatter);

            String savedDate = getSavedRatesDate();

            for (int i = 0; i < selectedCurrencies.size(); i++) {

                Element valute = selectedCurrencies.get(i);

                String numCode = getTagValue(valute, "NumCode");
                String charCode = getTagValue(valute, "CharCode");
                String nominal = getTagValue(valute, "Nominal");
                String name = getTagValue(valute, "Name");
                String value = getTagValue(valute, "Value");

                double valueNumber = Double.parseDouble(value.replace(",", "."));
                double rateForOneUnit = calculateRateForOneUnit(value, nominal);
                double savedRate = getSavedRate(charCode);

                Row row = sheet.createRow(rowNumber);

                row.createCell(0).setCellValue(currentDate);
                row.createCell(1).setCellValue(numCode);
                row.createCell(2).setCellValue(charCode);
                row.createCell(3).setCellValue(name);
                row.createCell(4).setCellValue(Integer.parseInt(nominal));
                row.createCell(5).setCellValue(valueNumber);
                row.createCell(6).setCellValue(rateForOneUnit);

                if (savedDate.equals("")) {
                    row.createCell(7).setCellValue("Нет данных");
                } else {
                    row.createCell(7).setCellValue(savedDate);
                }

                if (savedRate == -1) {
                    row.createCell(8).setCellValue("Нет данных");
                } else {
                    row.createCell(8).setCellValue(savedRate);
                }

                row.createCell(9).setCellValue(getRateChangeText(valute));

                rowNumber++;
            }

            for (int i = 0; i <= 9; i++) {
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


    //аналогичный метод, но принимает на вход элемент
    public static double getRateForOneUnit(Element valute) {
        String nominal = getTagValue(valute, "Nominal");
        String value = getTagValue(valute, "Value");

        return calculateRateForOneUnit(value, nominal);
    }


    // метод сохраняет все валюты для дальнейшего сравнения и вывода информации о росте/падкнии курса. Метод записывает дату сохранения и все валюты в текстовый файл
    public static void saveRatesForComparison(ArrayList<Element> allCurrencies) {

        try {
            PrintWriter writer = new PrintWriter(new FileWriter("last_rates.txt"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String currentDate = LocalDateTime.now().format(formatter);

            writer.println("DATE;" + currentDate);

            for (int i = 0; i < allCurrencies.size(); i++) {
                Element valute = allCurrencies.get(i);

                String charCode = getTagValue(valute, "CharCode");
                double rateForOneUnit = getRateForOneUnit(valute);

                writer.println(charCode + ";" + rateForOneUnit);
            }

            writer.close();

            System.out.println("Курсы всех валют сохранены для сравнения.");

        } catch (Exception e) {
            System.out.println("Произошла ошибка при сохранении курсов для сравнения:");
            System.out.println(e.getMessage());
        }
    }

    //метод для получения даты последнего сохранения валют, для вывода информации с какой именно даты курс подянлся или опустился
    public static String getSavedRatesDate() {

        try {
            File file = new File("last_rates.txt");

            if (!file.exists()) {
                return "";
            }

            Scanner scanner = new Scanner(file);

            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(";");

                if (parts.length == 2 && parts[0].equals("DATE")) {
                    scanner.close();
                    return parts[1];
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.out.println("Произошла ошибка при чтении даты сохранения:");
            System.out.println(e.getMessage());
        }

        return "";
    }

    //метод ищет посдений сохраненный курс валюты в last_rates.txt, если файла нет возвращает -1, если файл есть возвращет курс
    public static double getSavedRate(String charCode) {

        try {
            File file = new File("last_rates.txt");

            if (!file.exists()) {
                return -1;
            }

            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] parts = line.split(";");

                if (parts.length == 2) {
                    String savedCharCode = parts[0];

                    if (savedCharCode.equalsIgnoreCase(charCode)) {
                        double savedRate = Double.parseDouble(parts[1]);

                        scanner.close();
                        return savedRate;
                    }
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.out.println("Произошла ошибка при чтении сохраненного курса:");
            System.out.println(e.getMessage());
        }

        return -1;
    }

    //метод для обработки информации о росте/падении курса, возвращает готовую строку для вставки в интерфейс. метод сравнивает текущий курс из xml и последний сохраненный курс валюты из txt файла.
    public static String getRateChangeText(Element valute) {

        String charCode = getTagValue(valute, "CharCode");

        double currentRate = getRateForOneUnit(valute);
        double savedRate = getSavedRate(charCode);

        String savedDate = getSavedRatesDate();

        if (savedRate == -1 || savedDate.equals("")) {
            return "Изменение курса: данные для сравнения не сохранены.";
        }

        if (currentRate > savedRate) {
            double difference = currentRate - savedRate;

            return "Изменение курса с " + savedDate + ": курс вырос на " + String.format("%.4f", difference) + " руб.";
        } else if (currentRate < savedRate) {
            double difference = savedRate - currentRate;

            return "Изменение курса с " + savedDate + ": курс снизился на " + String.format("%.4f", difference) + " руб.";
        } else {
            return "Изменение курса с " + savedDate + ": курс не изменился.";
        }
    }

}
