package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.model.Traveller.TravellerTrip;
import ayuntamiento.viajes.model.Traveller.TravellerOffice;
import ayuntamiento.viajes.service.TravellerService;

import java.net.URL;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Clase controladora que se encarga de la vista de estadísticas, se controla
 * las labels y los pie charts
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-03
 * @version 1.0
 */
public class StadisticsController extends BaseController implements Initializable {

    @FXML
    private PieChart travelPC;
    @FXML
    private PieChart officePC;
    @FXML
    private PieChart dayPC;

    @FXML
    private Label totalTravellers;
    @FXML
    private Label averageTravel;
    @FXML
    private Label averageOffice;
    @FXML
    private Label averageDay;
    @FXML
    private Label chooseTravel;

    private TravellerService travellerS;
    private List<Traveller> listTraveller;

    private Object selectedValue;

    private static final Color[] COLORS = {
        Color.web("#0077B6"),
        Color.web("#F3722C"),
        Color.web("#00B4D8"),
        Color.web("#F8961E"),
        Color.web("#90E0EF"),
        Color.web("#F9844A"),
        Color.web("#CAF0F8"),
        Color.web("#FFA07A")
    };

    private enum ChartType {
        TRAVEL, OFFICE, DATE
    }

    private final int numOfTravels = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        selectedValue = null;
        travellerS = new TravellerService();
        listTraveller = travellerS.findAll();

        pieChartConf(listTraveller, Traveller::getTrip, travelPC, ChartType.TRAVEL);
        pieChartConf(listTraveller, Traveller::getDepartment, officePC, ChartType.OFFICE);
        pieChartConf(listTraveller, Traveller::getSignUpDate, dayPC, ChartType.DATE);

        setupPieChartListeners();

        rechargeLabels(listTraveller);
    }

    /**
     * Recargar los valores de los labels segun la lista que se le pasa
     * 
     * @param list Lista de travellers
     * 
     */
    private void rechargeLabels(List<Traveller> list) {
        int total = list.size();

        /* Agrupar por trip y contar viajeros por trip */
        Map<TravellerTrip, Long> tripGroups = list.stream()
                .collect(Collectors.groupingBy(Traveller::getTrip, Collectors.counting()));
        double avgTripCount = tripGroups.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        /* Agrupar por office y contar viajeros por office */
        Map<TravellerOffice, Long> officeGroups = list.stream()
                .collect(Collectors.groupingBy(Traveller::getDepartment, Collectors.counting()));
        double avgOfficeCount = officeGroups.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        /* Agrupar por fecha de signup y contar viajeros por día */
        Map<YearMonth, Long> signupGroupsByMonth = list.stream()
                .map(Traveller::getSignUpDate)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        date -> YearMonth.from(date),
                        Collectors.counting()
                ));

        double avgPerMonth = signupGroupsByMonth.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        totalTravellers.setText(String.valueOf(total));
        averageTravel.setText(String.format("%.2f", avgTripCount));
        averageOffice.setText(String.format("%.2f", avgOfficeCount));
        averageDay.setText(String.format("%.2f", avgPerMonth));
    }

    /**
     * Controla la configuración del piechart, añadiendo un label por cada tipo
     * y valor de enumerado
     * 
     * @param <T> Valor del dato de la función por la cual se va a clasificar
     * @param list lista de travellers
     * @param classifier función clasificadora
     * @param pieChart Piechart el cual se va a configurar
     * @param chartType Tipo de piechart que se va a configurar
     */

    private <T> void pieChartConf(List<Traveller> list, Function<Traveller, T> classifier, PieChart pieChart, ChartType chartType) {
        Map<String, Integer> counts = new HashMap<>();

        switch (chartType) {
            case DATE:
                for (Traveller t : list) {
                    LocalDate date = t.getSignUpDate();
                    if (date != null) {
                        String label = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + date.getYear();
                        counts.put(label, counts.getOrDefault(label, 0) + 1);
                    }
                }
                break;
            case TRAVEL:
                Map<T, Integer> rawCounts = countBy(classifier, list);
                List<Map.Entry<T, Integer>> sorted = rawCounts.entrySet()
                        .stream()
                        .sorted((a, b) -> b.getValue() - a.getValue())
                        .collect(Collectors.toList());

                int others = 0;
                for (int i = 0; i < sorted.size(); i++) {
                    if (i < numOfTravels) {
                        counts.put(sorted.get(i).getKey().toString(), sorted.get(i).getValue());
                    } else {
                        others += sorted.get(i).getValue();
                    }
                }
                if (others > 0) {
                    counts.put("Otros", others);
                }
                break;

            case OFFICE:
            default:
                for (Traveller t : list) {
                    String label = classifier.apply(t).toString();
                    counts.put(label, counts.getOrDefault(label, 0) + 1);
                }
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        counts.forEach((k, v) -> data.add(new PieChart.Data(k + " (" + v + ")", v)));

        pieChart.setData(data);
        pieChart.setLegendVisible(true);
        colorPieChartSlices(COLORS, pieChart);
    }

    private void setupPieChartListeners() {
        travelPC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(data.getName(), Traveller::getTrip, ChartType.TRAVEL));
        });
        officePC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(data.getName(), Traveller::getDepartment, ChartType.OFFICE));
        });
        dayPC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(data.getName(), Traveller::getSignUpDate, ChartType.DATE));
        });
    }

    /**
     * Metodo que llaman los diferentes gráficos cuando se hace click en alguno
     * de sus nodos de datos (el tipo de nodo depende del filtro), para
     * recalibrar los datos por ese valor
     * 
      * @param <T> Valor del dato de la función por la cual se va a clasificar
     * @param clickedNameWithValue nodo clickado
     * @param classifier función clasificadora
     * @param chartType Tipo de piechart que se va a configurar
     */
    private <T> void handlePieChartClick(String clickedNameWithValue, Function<Traveller, T> classifier, ChartType chartType) {
        String clickedValueStr = clickedNameWithValue.replaceAll("\\s*\\(.*\\)$", "").trim();

        if (clickedValueStr.equals(selectedValue)) {
            selectedValue = null;
            chooseTravel.setText("Todos los viajes");
        } else {
            selectedValue = clickedValueStr;
            switch (chartType) {
                case DATE ->
                    chooseTravel.setText("Viajeros desde " + selectedValue);
                case TRAVEL ->
                    chooseTravel.setText("Viajes de " + selectedValue);
                case OFFICE ->
                    chooseTravel.setText("Viajeros desde " + selectedValue);
            }
        }

        List<Traveller> filtered = selectedValue == null ? listTraveller : listTraveller.stream()
                .filter(t -> {
                    T value = classifier.apply(t);
                    if (value == null) {
                        return false;
                    }
                    if (chartType == ChartType.DATE && value instanceof LocalDate) {
                        LocalDate dateValue = (LocalDate) value;
                        String monthLabel = dateValue.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + dateValue.getYear();
                        return clickedValueStr.equals(monthLabel);
                    }
                    return clickedValueStr.equals(value.toString());
                }).toList();

        pieChartConf(filtered, Traveller::getTrip, travelPC, ChartType.TRAVEL);
        pieChartConf(filtered, Traveller::getDepartment, officePC, ChartType.OFFICE);
        pieChartConf(filtered, Traveller::getSignUpDate, dayPC, ChartType.DATE);

        setupPieChartListeners();
        rechargeLabels(filtered);
    }

    /**
     * Metodo que respecto a un función para clasificar y una lista de travellers
     * devuelve el mapa con el enumerado y el recuento de los valores.
     *
     * @param <T> Valor que puede ser cualquiera de los enumerados
     * @param classifier función que extrae los valores del enumerado
     * @param list la lista a contar
     * @return Mapa del enumerado con recuento de sus valores
     */
    private static <T> Map<T, Integer> countBy(Function<Traveller, T> classifier, List<Traveller> list) {
        Map<T, Integer> countMap = new HashMap<>();
        for (Traveller v : list) {
            T key = classifier.apply(v);
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }
        return countMap;
    }

    /**
     * Coloca los colores de los sectores
     */
    private void colorPieChartSlices(Color[] colors, PieChart pieChart) {
        Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                Node node = data.getNode();
                if (node != null && index < colors.length) {
                    String rgb = toRgbString(colors[index]);

                    node.setStyle("-fx-pie-color: " + rgb + ";");
                    data.getNode().setStyle("-fx-pie-color: " + rgb + ";");

                    final String type = data.getName();

                    colorPieChartLegend(type, rgb, pieChart);
                    index++;
                }
            }
        });
    }

    /**
     * Coloca los colores de la leyenda
     */
    private void colorPieChartLegend(String label, String rgb, PieChart pieChart) {
        for (Node legend : pieChart.lookupAll("Label.chart-legend-item")) {
            if (legend instanceof javafx.scene.control.Label labelNode) {
                if (labelNode.getText().equals(label)) {
                    Node symbol = labelNode.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + rgb + ";");
                    }
                }
            }
        }
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

}
