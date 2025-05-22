package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Vehicle;
import ayuntamiento.viajes.service.VehicleService;
import java.net.URL;

import java.time.LocalDate;
import java.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Cristian
 */
public class StadisticsController extends BaseController implements Initializable {

    @FXML
    private PieChart pieChart;
    @FXML
    private StackedBarChart<String, Number> stackedBarChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private BorderPane graphics;

    @FXML
    private Label totalVehicles;
    @FXML
    private Label typeVehicles;
    @FXML
    private Label itvVehicles;
    @FXML
    private Label insuranceVehicle;

    private VehicleService vehicleS;
    private List<Vehicle> listVehicles;
    private String selectedType;

    private static final List<String> CATEGORIES = List.of("> 2 años", "2 años - 1 año", "1 año - 6 meses", "< 6 meses");
    private static final List<String> TYPES = List.of("Alquilado", "Prestado", "Propio");
    private static final Color[] COLORS = {Color.RED, Color.DARKRED, Color.CRIMSON, Color.DARKSALMON};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        
        selectedType = null;
        vehicleS = new VehicleService();
        listVehicles = vehicleS.findAll();

        pieChartConf(listVehicles);
        barChartConfig(listVehicles);
        stackedBarChartConf(listVehicles);
        rechargeLabels();
    }

    //************//
    //**BARCHART**//
    //************//
    private void barChartConfig(List<Vehicle> list) {

        barChart.setAnimated(false);

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.getCategories().addAll(CATEGORIES);

        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        for (Vehicle v : list) {
            String type = v.getType().name();
            if (!TYPES.contains(type)) {
                continue;
            }

            String cat = getCategoryByDate(v.getItv_RentDate());

            dataMap
                    .computeIfAbsent(type, k -> new HashMap<>())
                    .merge(cat, 1, Integer::sum);
        }

        // Agregar las series a la gráfica
        for (String type : TYPES) {
            Map<String, Integer> counts = dataMap.getOrDefault(type, Collections.emptyMap());

            if (counts.values().stream().anyMatch(count -> count > 0)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(type);

                for (String cat : CATEGORIES) {
                    int value = counts.getOrDefault(cat, 0);
                    series.getData().add(new XYChart.Data<>(cat, value));
                }

                seriesList.add(series);
            }
        }

        // Limpiar y agregar las series actualizadas al gráfico
        barChart.getData().setAll(seriesList);

        barChart.setAnimated(true);

        // Colorear las series para un gráfico normal de barras
        colorBarChartSeries();
    }

    private void colorBarChartSeries() {
        Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Series<String, Number> series : barChart.getData()) {
                Color color = COLORS[index % COLORS.length];
                String rgb = toRgbString(color);
                String seriesStyle = "-fx-bar-fill: " + rgb + ";";

                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(seriesStyle);

                        String type = series.getName();
                        node.setOnMouseClicked(e -> typeVehicleClick(type));
                    }
                }

                colorBarChartLegend(series.getName(), rgb);
                index++;
            }
        });
    }

    private void colorBarChartLegend(String label, String rgb) {
        for (Node legend : barChart.lookupAll("Label.chart-legend-item")) {
            if (legend instanceof Label labelNode) {
                if (label.equals(labelNode.getText())) {
                    Node symbol = labelNode.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + rgb + ";");
                    }
                }
            }
        }
    }

    //******************//
    //**STAKEDBARCHART**//
    //******************//
    private void stackedBarChartConf(List<Vehicle> list) {

        stackedBarChart.setAnimated(false);

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.getCategories().addAll(CATEGORIES);

        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        // Contabilizar los datos según el criterio ITV
        for (Vehicle v : list) {
            String type = v.getType().name();
            if (!TYPES.contains(type)) {
                continue;
            }

            // Agrupar por fecha de ITV
            String cat = getCategoryByDate(v.getItv_RentDate());

            dataMap
                    .computeIfAbsent(type, k -> new HashMap<>())
                    .merge(cat, 1, Integer::sum);
        }

        // Agregar las series a la gráfica
        for (String type : TYPES) {
            Map<String, Integer> counts = dataMap.getOrDefault(type, Collections.emptyMap());

            if (counts.values().stream().anyMatch(count -> count > 0)) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(type);

                for (String cat : CATEGORIES) {
                    int value = counts.getOrDefault(cat, 0);
                    series.getData().add(new XYChart.Data<>(cat, value));
                }

                seriesList.add(series);
            }
        }

        // Limpiar y agregar las series actualizadas al gráfico
        stackedBarChart.getData().setAll(seriesList);

        stackedBarChart.setAnimated(true);

        // Colorear las series para StackedBarChart
        colorStackedBarChartSeries();
    }

    private void colorStackedBarChartSeries() {
        Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Series<String, Number> series : stackedBarChart.getData()) {
                Color color = COLORS[index % COLORS.length];
                String rgb = toRgbString(color);
                String seriesStyle = "-fx-bar-fill: " + rgb + ";";

                // Apply color to each bar in the series
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(seriesStyle);

                        // Click event for each bar
                        String type = series.getName();
                        node.setOnMouseClicked(e -> typeVehicleClick(type));
                    }
                }

                // Sync the legend color for the series
                colorStackedBarChartLegend(series.getName(), rgb);
                index++;
            }
        });
    }

    private void colorStackedBarChartLegend(String label, String rgb) {
        for (Node legend : stackedBarChart.lookupAll("Label.chart-legend-item")) {
            if (legend instanceof Label labelNode) {
                if (label.equals(labelNode.getText())) {
                    Node symbol = labelNode.getGraphic();
                    if (symbol != null) {
                        symbol.setStyle("-fx-background-color: " + rgb + ";");
                    }
                }
            }
        }
    }

    //************//
    //**PIECHART**//
    //************//
    private void pieChartConf(List<Vehicle> list) {
        int alquiladoCount = 0;
        int prestadoCount = 0;
        int propioCount = 0;

        for (Vehicle v : list) {
            String type = v.getType().name();
            switch (type) {
                case "Alquilado" ->
                    alquiladoCount++;
                case "Prestado" ->
                    prestadoCount++;
                case "Propio" ->
                    propioCount++;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (alquiladoCount > 0) {
            pieChartData.add(new PieChart.Data("Alquilado", alquiladoCount));
        }
        if (prestadoCount > 0) {
            pieChartData.add(new PieChart.Data("Prestado", prestadoCount));
        }
        if (propioCount > 0) {
            pieChartData.add(new PieChart.Data("Propio", propioCount));
        }

        pieChart.setData(pieChartData);
        pieChart.setLegendVisible(true);

        colorPieChartSlices(COLORS);
    }

    private void colorPieChartSlices(Color[] colors) {
        Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                Node node = data.getNode();
                if (node != null && index < colors.length) {
                    String rgb = toRgbString(colors[index]);

                    node.setStyle("-fx-pie-color: " + rgb + ";");
                    data.getNode().setStyle("-fx-pie-color: " + rgb + ";");

                    final String type = data.getName();
                    node.setOnMouseClicked(e -> typeVehicleClick(type));

                    colorPieChartLegend(type, rgb);
                    index++;
                }
            }
        });
    }

    private void colorPieChartLegend(String label, String rgb) {
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

    //************//
    //***LABELS***//
    //************//
    private void rechargeLabels() {
        int totalVehiclesCount = 0;
        int typeUseCount = 0;
        int typeNotCount = 0;
        int itvYearCount = 0;
        int insuranceYearCount = 0;

        for (Vehicle v : listVehicles) {

            totalVehiclesCount++;

            if (selectedType == null) {
                if (v.getType().name().equals("Propio")) {
                    typeUseCount++;
                } else {
                    typeNotCount++;
                }
            } else {
                if (v.getType().name().equals(selectedType)) {
                    typeUseCount++;
                } else {
                    typeNotCount++;
                }
            }

            if (selectedType == null || v.getType().name().equals(selectedType)) {
                LocalDate itvDate = v.getItv_RentDate();
                String itvCategory = getCategoryByDate(itvDate);
                if (itvCategory.equals("1 año - 6 meses") || itvCategory.equals("< 6 meses")) {
                    itvYearCount++;
                }

                LocalDate insuranceDate = v.getItv_RentDate();
                String insuranceCategory = getCategoryByDate(insuranceDate);
                if (insuranceCategory.equals("1 año - 6 meses") || insuranceCategory.equals("< 6 meses")) {
                    insuranceYearCount++;
                }
            }
        }

        int percentUse = totalVehiclesCount > 0 ? Math.round((float) typeUseCount / totalVehiclesCount * 100) : 0;
        int percentNot = totalVehiclesCount > 0 ? Math.round((float) typeNotCount / totalVehiclesCount * 100) : 0;
        if (selectedType == null) {
            typeUseCount = totalVehiclesCount;
        }
        int percentItv = totalVehiclesCount > 0 ? Math.round((float) itvYearCount / typeUseCount * 100) : 0;
        int percentInsurance = totalVehiclesCount > 0 ? Math.round((float) insuranceYearCount / typeUseCount * 100) : 0;

        if (selectedType == null) {
            typeVehicles.setText("P " + percentUse + "% - " + percentNot + "% A");
            totalVehicles.setText(String.valueOf(totalVehiclesCount));
        } else {
            typeVehicles.setText(percentUse + "%");
            totalVehicles.setText(String.valueOf(typeUseCount));
        }

        itvVehicles.setText(percentItv + "%");
        insuranceVehicle.setText(percentInsurance + "%");
    }

    //************//
    //****UTIL****//
    //************//
    private void typeVehicleClick(String vehicleType) {
        if (vehicleType.equals(selectedType)) {
            selectedType = null;
            pieChartConf(listVehicles);
            barChartConfig(listVehicles);
            stackedBarChartConf(listVehicles);
        } else {
            selectedType = vehicleType;
            List<Vehicle> filtered = new ArrayList<>();
            for (Vehicle v : listVehicles) {
                if (v.getType().name().equals(vehicleType)) {
                    filtered.add(v);
                }
            }
            pieChartConf(filtered);
            barChartConfig(filtered);
            stackedBarChartConf(filtered);
        }
        rechargeLabels();
    }

    private String getCategoryByDate(LocalDate date) {
        Period period = Period.between(LocalDate.now(), date);

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        if (years > 2 || (years == 2 && (months > 0 || days > 0))) {
            return "> 2 años";
        } else if (years == 2 || (years == 1 && (months > 0 || days > 0))) {
            return "2 años - 1 año";
        } else if ((years == 1 && months == 0 && days == 0) || (months > 6 || (months == 6 && days > 0))) {
            return "1 año - 6 meses";
        } else {
            return "< 6 meses";
        }
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public BorderPane getGraphics() {
        return graphics;
    }

}
