package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.model.Traveller.TravellerTrip;
import ayuntamiento.viajes.model.Traveller.TravellerOffice;
import ayuntamiento.viajes.service.TravellerService;
import java.net.URL;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;

/**
 * Clase controladora que se encarga de la vista de estadísticas, se controla
 * las labels, los stackedbar charts y los pie charts
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
    private PieChart datPC;

    @FXML
    private Label totalTravellers;
    @FXML
    private Label averageTravel;
    @FXML
    private Label averageOffice;
    @FXML
    private Label averageDay;

    private TravellerService travellerS;
    private List<Traveller> listTraveller;

    private Object selectedValue;

    public enum ChartType {
        TRAVEL, OFFICE, DATE
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        selectedValue = null;
        travellerS = new TravellerService();
        //listTraveller = travellerS.findAll();

        listTraveller = List.of(
                new Traveller("12345678A", "Ana Pérez", 0, 0, "01/05/2023"),
                new Traveller("23456789B", "Luis Gómez", 1, 1, "01/05/2023"), // mismo día que Ana
                new Traveller("34567890C", "María López", 2, 2, "20/03/2023"),
                new Traveller("45678901D", "Carlos Ruiz", 0, 3, "05/06/2023"),
                new Traveller("56789012E", "Laura Martín", 1, 4, "12/02/2023"),
                new Traveller("67890123F", "Javier Sánchez", 2, 5, "10/01/2023"),
                new Traveller("78901234G", "Sofía Torres", 0, 0, "20/03/2023"), // mismo día que María
                new Traveller("89012345H", "Miguel Fernández", 1, 1, "30/04/2023"),
                new Traveller("90123456I", "Elena Díaz", 2, 2, "07/03/2023"),
                new Traveller("01234567J", "Pedro Morales", 0, 3, "19/06/2023"),
                new Traveller("11223344K", "Isabel Ruiz", 1, 4, "12/02/2023"), // mismo día que Laura
                new Traveller("22334455L", "Raúl Jiménez", 2, 5, "28/01/2023")
        );

        pieChartConf(listTraveller, Traveller::getTrip, travelPC);
        pieChartConf(listTraveller, Traveller::getOffice, officePC);
        pieChartConf(listTraveller, Traveller::getSignUpDate, datPC);

        setupPieChartListeners();

        rechargeLabels(listTraveller);
    }

    /**
     * Controla la configuración del piechart, añadiendo un label por cada tipo
     * y valor de enumerado
     *
     * @param list Lista de vehiculos para configurar el chart de seguros
     */
    private <T> void pieChartConf(List<Traveller> list, Function<Traveller, T> classifier, PieChart pieChart) {
        Map<T, Integer> counts = countBy(classifier, list);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<T, Integer> entry : counts.entrySet()) {
            String label = entry.getKey().toString();
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        pieChart.setData(pieChartData);

        for (PieChart.Data data : pieChart.getData()) {
            String name = data.getName();
            double value = data.getPieValue();
            data.setName(name + " (" + (int) value + ")");
        }

        pieChart.setLegendVisible(true);
    }

    /**
     *
     */
    private void rechargeLabels(List<Traveller> list) {
        int total = list.size();

        // Agrupar por trip y contar viajeros por trip
        Map<TravellerTrip, Long> tripGroups = list.stream()
                .collect(Collectors.groupingBy(Traveller::getTrip, Collectors.counting()));
        double avgTripCount = tripGroups.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // Agrupar por office y contar viajeros por office
        Map<TravellerOffice, Long> officeGroups = list.stream()
                .collect(Collectors.groupingBy(Traveller::getOffice, Collectors.counting()));
        double avgOfficeCount = officeGroups.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // Agrupar por fecha de signup y contar viajeros por día
        Map<LocalDate, Long> signupGroups = list.stream()
                .collect(Collectors.groupingBy(Traveller::getSignUpDate, Collectors.counting()));
        double avgPerDay = signupGroups.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        // Setear los valores a los labels
        totalTravellers.setText(String.valueOf(total));
        averageTravel.setText(String.format("%.2f", avgTripCount));
        averageOffice.setText(String.format("%.2f", avgOfficeCount));
        averageDay.setText(String.format("%.2f", avgPerDay));
    }

    private void setupPieChartListeners() {
        travelPC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(travelPC, data.getName(), Traveller::getTrip));
        });
        officePC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(officePC, data.getName(), Traveller::getOffice));
        });
        datPC.getData().forEach(data -> {
            data.getNode().setOnMouseClicked(e -> handlePieChartClick(datPC, data.getName(), Traveller::getSignUpDate));
        });
    }

    /**
     * Metodo que llaman los diferentes gráficos cuando se hace click en alguno
     * de sus nodos de datos (el tipo de nodo depende del filtro), para
     * recalibrar los datos por ese valor
     *
     * @param valueStr el nombre nodo elegido
     */
    private <T> void handlePieChartClick(
            PieChart pieChart,
            Object clickedValue,
            Function<Traveller, T> classifier
    ) {
        if (clickedValue.equals(selectedValue)) {
            selectedValue = null;
            pieChartConf(listTraveller, classifier, pieChart);
        } else {
            selectedValue = clickedValue;

            List<Traveller> filtered = listTraveller.stream()
                    .filter(t -> clickedValue.equals(classifier.apply(t)))
                    .collect(Collectors.toList());

            pieChartConf(filtered, Traveller::getTrip, travelPC);
            pieChartConf(filtered, Traveller::getOffice, officePC);
            pieChartConf(filtered, Traveller::getSignUpDate, datPC);

            rechargeLabels(filtered);
        }
    }

    /**
     * Metodo que respecto a un función para clasificar y una lista de vehículos
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

}
