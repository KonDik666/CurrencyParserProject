package CurrencyParserPackage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CurrencyParserGUI {

    public static void createInterface(Document document) {

        ArrayList<Element> allCurrencies = CurrencyParser.getAllCurrencies(document);

        JFrame frame = new JFrame("Парсер курсов валют ЦБ РФ");
        frame.setSize(1100, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (int i = 0; i < allCurrencies.size(); i++) {
            Element valute = allCurrencies.get(i);

            String charCode = CurrencyParser.getTagValue(valute, "CharCode");
            String name = CurrencyParser.getTagValue(valute, "Name");

            listModel.addElement(charCode + " - " + name);
        }

        JList<String> currencyList = new JList<>(listModel);
        currencyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); //для выбора нескольких валют с зажатым ctrl или shift

        JScrollPane listScrollPane = new JScrollPane(currencyList);
        listScrollPane.setPreferredSize(new Dimension(300, 400));

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        JButton showButton = new JButton("Показать информацию");
        JButton selectAllButton = new JButton("Выбрать все");
        JButton clearButton = new JButton("Очистить выбор");
        JButton saveXmlButton = new JButton("Сохранить XML");
        JButton saveExcelButton = new JButton("Сохранить Excel");
        JButton saveCompareButton = new JButton("Сохранить валюты для сравнения");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        buttonPanel.add(showButton);
        buttonPanel.add(selectAllButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(saveXmlButton);
        buttonPanel.add(saveExcelButton);
        buttonPanel.add(saveCompareButton);


        JLabel titleLabel = new JLabel("Выберите одну или несколько валют из списка:");

        frame.setLayout(new BorderLayout());
        frame.add(titleLabel, BorderLayout.NORTH);
        frame.add(listScrollPane, BorderLayout.WEST);
        frame.add(resultScrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        showButton.addActionListener(e -> {
            ArrayList<Element> selectedCurrencies = getSelectedCurrencies(currencyList, allCurrencies);

            if (selectedCurrencies.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Сначала выберите валюту.");
                return;
            }

            StringBuilder resultText = new StringBuilder();

            for (int i = 0; i < selectedCurrencies.size(); i++) {
                Element valute = selectedCurrencies.get(i);

                String numCode = CurrencyParser.getTagValue(valute, "NumCode");
                String charCode = CurrencyParser.getTagValue(valute, "CharCode");
                String nominal = CurrencyParser.getTagValue(valute, "Nominal");
                String name = CurrencyParser.getTagValue(valute, "Name");
                String value = CurrencyParser.getTagValue(valute, "Value");

                double rateForOneUnit = CurrencyParser.calculateRateForOneUnit(value, nominal);

                resultText.append("Валюта выбрана:\n");
                resultText.append("------------------------------\n");
                resultText.append("Цифровой код: ").append(numCode).append("\n");
                resultText.append("Буквенный код: ").append(charCode).append("\n");
                resultText.append("Название: ").append(name).append("\n");
                resultText.append("Номинал: ").append(nominal).append("\n");
                resultText.append("Курс: ").append(value).append("\n");
                resultText.append("Курс за 1 единицу валюты: ").append(rateForOneUnit).append("\n");
                resultText.append(CurrencyParser.getRateChangeText(valute)).append("\n");
                resultText.append("------------------------------\n\n");
            }

            resultArea.setText(resultText.toString());
        });

        selectAllButton.addActionListener(e -> {
            if (listModel.getSize() > 0) {
                currencyList.setSelectionInterval(0, listModel.getSize() - 1);
            }
        });

        clearButton.addActionListener(e -> {
            currencyList.clearSelection();
            resultArea.setText("");
        });

        saveXmlButton.addActionListener(e -> {
            ArrayList<Element> selectedCurrencies = getSelectedCurrencies(currencyList, allCurrencies);

            if (selectedCurrencies.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Сначала выберите валюту для сохранения.");
                return;
            }

            CurrencyParser.createXml(selectedCurrencies);

            JOptionPane.showMessageDialog(frame, "Выбранные валюты сохранены в XML.");
        });

        saveExcelButton.addActionListener(e -> {
            ArrayList<Element> selectedCurrencies = getSelectedCurrencies(currencyList, allCurrencies);

            if (selectedCurrencies.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Сначала выберите валюту для сохранения.");
                return;
            }

            CurrencyParser.createExcel(selectedCurrencies);

            JOptionPane.showMessageDialog(frame, "Выбранные валюты сохранены в Excel.");
        });

        saveCompareButton.addActionListener(e -> {
            CurrencyParser.saveRatesForComparison(allCurrencies);

            JOptionPane.showMessageDialog(frame, "Курсы всех валют сохранены для последующего сравнения.");
        });

        frame.setVisible(true);
    }

    public static ArrayList<Element> getSelectedCurrencies(JList<String> currencyList, ArrayList<Element> allCurrencies) {

        ArrayList<Element> selectedCurrencies = new ArrayList<>();

        int[] selectedIndexes = currencyList.getSelectedIndices();

        for (int i = 0; i < selectedIndexes.length; i++) {
            int index = selectedIndexes[i];

            selectedCurrencies.add(allCurrencies.get(index));
        }

        return selectedCurrencies;
    }
}
