package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.LoginService;
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

public class StadisticsDepartmentController extends BaseController implements Initializable {

    @FXML
    private PieChart travelPC;
    @FXML
    private StackedBarChart<String, Number> travelBC;
    @FXML
    private Label totalTravellers;
    @FXML
    private Label averageOccupation;
    @FXML
    private Label department;

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

        department.setText(LoginService.getAdminDepartment().getName().replace("_", " "));

        travellerS = new TravellerService();
        travelS = new TravelService();

        long deptId = getLoggedUserDepartment();
        listTraveller = travellerS.findByDepartment(deptId);
        listTravel = travelS.findByDepartment(deptId);

        numOfTravels = listTravel.size();

        // Configurar PieChart y StackedBarChart
        setupPieChart(listTraveller, travelPC);
        setupStackedBarChart(listTravel);

        // Recargar labels
        rechargeLabels(listTraveller, listTravel);
    }

    private long getLoggedUserDepartment() {
        return LoginService.getAdminLog().getDepartment();
    }

    private void rechargeLabels(List<Traveller> travellers, List<Travel> travels) {
        int total = travellers.size();
        double avgOccupation = travels.stream()
                .filter(t -> t.getSeats_total() > 0)
                .mapToDouble(t -> (double) t.getSeats_occupied() / t.getSeats_total())
                .average()
                .orElse(0) * 100;

        totalTravellers.setText(String.valueOf(total));
        averageOccupation.setText(String.format("%.2f %%", avgOccupation));
    }

    private void setupPieChart(List<Traveller> list, PieChart pieChart) {
        Map<String, Integer> counts = new HashMap<>();

        for (Traveller t : list) {
            Travel travel = listTravel.stream()
                    .filter(tr -> tr.getId() == t.getTrip())
                    .findFirst().orElse(null);
            String label = (travel != null) ? travel.getDescriptor().replace("_", " ") : "Sin viaje";
            counts.put(label, counts.getOrDefault(label, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sorted = counts.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());

        Map<String, Integer> finalCounts = new HashMap<>();
        int others = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (i < numOfTravels) {
                finalCounts.put(sorted.get(i).getKey(), sorted.get(i).getValue());
            } else {
                others += sorted.get(i).getValue();
            }
        }
        if (others > 0) {
            finalCounts.put("Otros", others);
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        finalCounts.forEach((k, v) -> data.add(new PieChart.Data(k + " (" + v + ")", v)));
        pieChart.setData(data);
        pieChart.setLegendVisible(true);

        colorPieChartSlices(COLORS, pieChart);
    }

    private void colorPieChartSlices(Color[] colors, PieChart pieChart) {
        Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                String rgb = toRgbString(colors[index % colors.length]);
                data.getNode().setStyle("-fx-pie-color: " + rgb + ";");
                colorPieChartLegend(data.getName(), rgb, pieChart);
                index++;
            }
        });
    }

    private void colorPieChartLegend(String label, String rgb, PieChart pieChart) {
        for (Node legend : pieChart.lookupAll("Label.chart-legend-item")) {
            if (legend instanceof Label labelNode) {
                if (labelNode.getText().equals(label)) {
                    Node symbol = labelNode.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + rgb + ";");
                    }
                }
            }
        }
    }

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

            String tripName = (t.getDescriptor() != null)
                    ? t.getDescriptor().replace("_", " ")
                    : "Viaje " + t.getId();

            occupiedSeries.getData().add(new XYChart.Data<>(tripName, occupied));
            freeSeries.getData().add(new XYChart.Data<>(tripName, free));
        }

        travelBC.getData().addAll(occupiedSeries, freeSeries);

        // Colorear barras y leyenda
        applyBarColors(occupiedSeries, freeSeries);
    }

    /**
     * Aplica colores a las barras del StackedBarChart
     */
    private void applyBarColors(XYChart.Series<String, Number> occupiedSeries, XYChart.Series<String, Number> freeSeries) {
        Platform.runLater(() -> {
            // Colores de barras
            setSeriesColor(occupiedSeries, "#0077B6"); // azul oscuro
            setSeriesColor(freeSeries, "#90E0EF");     // azul claro

            // Colores de leyenda
            setLegendColor("Ocupadas", "#0077B6");
            setLegendColor("Libres", "#90E0EF");
        });
    }

    /**
     * Aplica un color a todos los nodos de una serie
     */
    private void setSeriesColor(XYChart.Series<String, Number> series, String color) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + color + ";");
            }
        }
    }

    /**
     * Aplica color a la leyenda del gráfico
     */
    private void setLegendColor(String seriesName, String color) {
        for (Node legend : travelBC.lookupAll(".chart-legend-item")) {
            if (legend instanceof Label labelNode) {
                if (labelNode.getText().equals(seriesName)) {
                    Node symbol = labelNode.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + color + ";");
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
