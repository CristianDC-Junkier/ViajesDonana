package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.TravelService;
import ayuntamiento.viajes.service.TravellerService;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Vista de estadísticas para usuarios de un departamento (no admin).
 * Muestra la distribución de viajeros por viaje (PieChart)
 * y la ocupación de plazas (StackedBarChart).
 *
 * @author Cristian
 * @since 2025-08-25
 * @version 1.0
 */
public class StadisticsDepartmentController extends BaseController implements Initializable {

    @FXML
    private PieChart travelPC;

    @FXML
    private StackedBarChart<String, Number> travelBC;

    @FXML
    private Label totalTravellers;
    @FXML
    private Label averageOccupation;

    private TravellerService travellerS;
    private TravelService travelS;

    private List<Traveller> listTraveller;
    private List<Travel> listTravel;

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

    private int numOfTravels = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        travellerS = new TravellerService();
        travelS = new TravelService();

        long deptId = getLoggedUserDepartment();
        listTraveller = travellerS.findByDepartment(deptId);
        listTravel = travelS.findByDepartment(deptId);
        
        numOfTravels = listTravel.size();

        // Distribución de viajeros por viaje (PieChart)
        pieChartConf(listTraveller, Traveller::getTrip, travelPC);

        // Ocupación de viajes (StackedBarChart)
        setupStackedBarChart(listTravel);

        // Labels superiores
        rechargeLabels(listTraveller, listTravel);
    }

    private long getLoggedUserDepartment() {
        // Aquí llamas a tu LoginService.getAdminLog() o similar
        return ayuntamiento.viajes.service.LoginService.getAdminLog().getDepartment();
    }

    /**
     * Recargar labels de estadísticas para usuario normal
     */
    private void rechargeLabels(List<Traveller> travellers, List<Travel> travels) {
        int total = travellers.size();

        // media ocupación en %
        double avgOccupation = travels.stream()
                .filter(t -> t.getSeats_total() > 0)
                .mapToDouble(t -> (double) t.getSeats_occupied() / t.getSeats_total())
                .average()
                .orElse(0) * 100;

        totalTravellers.setText(String.valueOf(total));
        averageOccupation.setText(String.format("%.2f %%", avgOccupation));
    }

    /**
     * Configuración del PieChart de distribución de viajeros por viaje
     */
    private <T> void pieChartConf(List<Traveller> list, Function<Traveller, T> classifier, PieChart pieChart) {
        Map<String, Integer> counts = new HashMap<>();

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

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        counts.forEach((k, v) -> data.add(new PieChart.Data(k + " (" + v + ")", v)));

        pieChart.setData(data);
        pieChart.setLegendVisible(true);
        colorPieChartSlices(COLORS, pieChart);
    }

    /**
     * Configuración del gráfico de barras apiladas (ocupadas vs libres)
     */
    private void setupStackedBarChart(List<Travel> travels) {
        travelBC.getData().clear();

        XYChart.Series<String, Number> occupiedSeries = new XYChart.Series<>();
        occupiedSeries.setName("Ocupadas");

        XYChart.Series<String, Number> freeSeries = new XYChart.Series<>();
        freeSeries.setName("Libres");

        for (Travel t : travels) {
            int occupied = t.getSeats_occupied();
            int total = t.getSeats_total();
            int free = total - occupied;

            String tripName = t.getDescriptor() != null ? t.getDescriptor() : "Viaje " + t.getId();

            occupiedSeries.getData().add(new XYChart.Data<>(tripName, occupied));
            freeSeries.getData().add(new XYChart.Data<>(tripName, free));
        }

        travelBC.getData().addAll(occupiedSeries, freeSeries);
    }

    private static <T> Map<T, Integer> countBy(Function<Traveller, T> classifier, List<Traveller> list) {
        Map<T, Integer> countMap = new HashMap<>();
        for (Traveller v : list) {
            T key = classifier.apply(v);
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }
        return countMap;
    }

    private void colorPieChartSlices(Color[] colors, PieChart pieChart) {
        Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                Node node = data.getNode();
                if (node != null && index < colors.length) {
                    String rgb = toRgbString(colors[index]);

                    node.setStyle("-fx-pie-color: " + rgb + ";");
                    data.getNode().setStyle("-fx-pie-color: " + rgb + ";");
                    index++;
                }
            }
        });
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
