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
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;

/**
 * Clase controladora que se encarga de la vista de estadísticas, se controla
 * las labels, los stackedbar charts y los pie charts
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.3
 */
public class StadisticsController extends BaseController implements Initializable {

    @FXML
    private PieChart pieChart;
    @FXML
    private StackedBarChart<String, Number> ITV_RentSBC;
    @FXML
    private StackedBarChart<String, Number> insuranceSBC;

    @FXML
    private Label chooseVehicles;
    @FXML
    private ToggleButton filterTBC;

    @FXML
    private Label totalVehicles;
    @FXML
    private Label kmVehicles;
    @FXML
    private Label lastcheckVehicles;
    @FXML
    private Label useVehicles;

    private TravellerService vehicleS;
    private List<Traveller> listVehicles;

    private Object selectedValue;

    private VehicleFilter filter;

    private enum VehicleFilter {
        STATUS,
        TYPE,
    }

    private static final List<String> CATEGORIES = List.of("> 2 años", "2 años - 1 año", "1 año - 6 meses", "< 6 meses", "Sin datos");
    private static final Color[] COLORS = {
        Color.RED,
        Color.DARKRED,
        Color.ORANGERED,
        Color.CRIMSON,
        Color.TOMATO,
        Color.BROWN
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        selectedValue = null;
        vehicleS = new TravellerService();
        listVehicles = vehicleS.findAll();

        filter = VehicleFilter.STATUS;

        pieChartConf(listVehicles);
        insuranceConfig(listVehicles);
        itv_rentConfig(listVehicles);
        rechargeLabels();
    }

    //**************************//
    //**SEGUROS STAKEDBARCHART**//
    //**************************//
    /**
     * Controla la creación de las stacked del seguro
     *
     * @param list Lista de vehiculos para configurar el chart de seguros
     */
    private void insuranceConfig(List<Traveller> list) {
        insuranceSBC.setAnimated(false);

        /* Obtener función clasificadora según el filtro */
        Function<Traveller, ?> classifier = getClassifierByFilter(filter);


        /* Map<Clave de agrupación, Map<Categoría, Cuenta>>*/
        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        for (Traveller v : list) {
            Object keyObj = classifier.apply(v);
            if (keyObj == null) {
                continue;
            }

            String key = keyObj.toString();

            /* Aquí calculamos la categoría según fecha o cualquier criterio */
            String cat = CATEGORIES.get(getCategoryByDate(v.getSignUpDate()));

            dataMap
                    .computeIfAbsent(key, k -> new HashMap<>())
                    .merge(cat, 1, Integer::sum);
        }

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        /* Para cada clave (tipo, estado, etc.) en dataMap, crear series */
        for (String key : dataMap.keySet()) {
            Map<String, Integer> counts = dataMap.getOrDefault(key, Collections.emptyMap());

            boolean hasData = counts.values().stream().anyMatch(count -> count > 0);
            if (!hasData) {
                continue;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(key);

            for (String cat : CATEGORIES) {
                int value = counts.getOrDefault(cat, 0);
                series.getData().add(new XYChart.Data<>(cat, value));
            }

            seriesList.add(series);
        }

        insuranceSBC.getData().setAll(seriesList);

        insuranceSBC.setAnimated(true);

        insuranceColor();
    }

    /**
     * Coloca los colores de las barras
     */
    private void insuranceColor() {
        Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Series<String, Number> series : insuranceSBC.getData()) {
                Color color = COLORS[index % COLORS.length];
                String rgb = toRgbString(color);
                String seriesStyle = "-fx-bar-fill: " + rgb + ";";

                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(seriesStyle);

                        String type = series.getName();
                        node.setOnMouseClicked(e -> onFilterValueClick(type));
                    }
                }

                insuranceColorLegend(series.getName(), rgb);
                index++;
            }
        });
    }

    /**
     * Coloca los colores de la leyenda
     */
    private void insuranceColorLegend(String label, String rgb) {
        for (Node legend : insuranceSBC.lookupAll("Label.chart-legend-item")) {
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

    //*******************************//
    //**ITC/ALQUILER STAKEDBARCHART**//
    //*******************************//
    /**
     * Controla la creación de las stacked del itv/rent
     *
     * @param list Lista de vehiculos para configurar el chart de seguros
     */
    private void itv_rentConfig(List<Traveller> list) {
        ITV_RentSBC.setAnimated(false);

        /* Obtener función clasificadora según el filtro */
        Function<Traveller, ?> classifier = getClassifierByFilter(filter);

        /* Map<Clave de agrupación, Map<Categoría, Cuenta>>*/
        Map<String, Map<String, Integer>> dataMap = new HashMap<>();

        for (Traveller v : list) {
            Object keyObj = classifier.apply(v);
            if (keyObj == null) {
                continue;
            }

            String key = keyObj.toString();

            /* Aquí calculamos la categoría según fecha o cualquier criterio */
            String cat = CATEGORIES.get(getCategoryByDate(v.getSignUpDate()));

            dataMap
                    .computeIfAbsent(key, k -> new HashMap<>())
                    .merge(cat, 1, Integer::sum);
        }

        List<XYChart.Series<String, Number>> seriesList = new ArrayList<>();

        /* Para cada clave (tipo, estado, etc.) en dataMap, crear series */
        for (String key : dataMap.keySet()) {
            Map<String, Integer> counts = dataMap.getOrDefault(key, Collections.emptyMap());

            boolean hasData = counts.values().stream().anyMatch(count -> count > 0);
            if (!hasData) {
                continue;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(key);

            for (String cat : CATEGORIES) {
                int value = counts.getOrDefault(cat, 0);
                series.getData().add(new XYChart.Data<>(cat, value));
            }

            seriesList.add(series);
        }

        ITV_RentSBC.getData().setAll(seriesList);

        ITV_RentSBC.setAnimated(true);

        itv_rentColor();
    }

    /**
     * Coloca los colores del las barras
     */
    private void itv_rentColor() {
        Platform.runLater(() -> {
            int index = 0;
            for (XYChart.Series<String, Number> series : ITV_RentSBC.getData()) {
                Color color = COLORS[index % COLORS.length];
                String rgb = toRgbString(color);
                String seriesStyle = "-fx-bar-fill: " + rgb + ";";

                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle(seriesStyle);

                        String type = series.getName();
                        node.setOnMouseClicked(e -> onFilterValueClick(type));
                    }
                }

                itv_rentColorLegend(series.getName(), rgb);
                index++;
            }
        });
    }

    /**
     * Coloca los colores de la leyenda
     */
    private void itv_rentColorLegend(String label, String rgb) {
        for (Node legend : ITV_RentSBC.lookupAll("Label.chart-legend-item")) {
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
    /**
     * Controla la configuración del piechart, añadiendo un label por cada tipo
     * y valor de enumerado
     *
     * @param list Lista de vehiculos para configurar el chart de seguros
     */
    private void pieChartConf(List<Traveller> list) {
        Function<Traveller, ?> classifier = getClassifierByFilter(filter);
        Map<?, Integer> counts = countBy(classifier, list);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<?, Integer> entry : counts.entrySet()) {
            String label = entry.getKey().toString();
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        pieChart.setData(pieChartData);
        pieChart.setLegendVisible(true);

        colorPieChartSlices(COLORS);
    }

    /**
     * Coloca los colores del las barras
     */
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
                    node.setOnMouseClicked(e -> onFilterValueClick(type));

                    colorPieChartLegend(type, rgb);
                    index++;
                }
            }
        });
    }

    /**
     * Coloca los colores de la leyenda
     */
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
    /**
     * Controla la recargar de los labels Se calculan el total de los vehiculos
     * por tipo elegido Se calcula la media de vehiculos por tipo elegido Se
     * calcula la cantidad en porcentaje de coches que su ultimo chekeo fue hace
     * 1 año o más de 1 año Se calcula la cantidad en porcentaje de vehiculos
     * que están en uso
     */
    private void rechargeLabels() {
        int filteredVehiclesCount = 0;
        int totalKms = 0;
        int lastcheckCount = 0;
        int useVehiclesCount = 0;

        for (Traveller v : listVehicles) {
            boolean matches = matchesFilter(v);

            if (matches) {
                filteredVehiclesCount++;
                if (v.getOffice()!= null) {
                    totalKms += v.getId();
                }
            }

            if (matches) {
                LocalDate checkDate = v.getSignUpDate();
                int category = getCategoryByDate(checkDate);
                if (category == 0 || category == 1) {
                    lastcheckCount++;
                }

                if (!v.getDni().isBlank() && !v.getDni().isBlank()) {
                    useVehiclesCount++;
                }
            }
        }

        int averageKms = filteredVehiclesCount > 0 ? totalKms / filteredVehiclesCount : 0;
        int percentLastCheck = filteredVehiclesCount > 0 ? Math.round((float) lastcheckCount / filteredVehiclesCount * 100) : 0;
        int percentUse = filteredVehiclesCount > 0 ? Math.round((float) useVehiclesCount / filteredVehiclesCount * 100) : 0;

        totalVehicles.setText(String.valueOf(filteredVehiclesCount));
        kmVehicles.setText(String.valueOf(averageKms));
        lastcheckVehicles.setText(percentLastCheck + "%");
        useVehicles.setText(percentUse + "%");
    }

    //************//
    //****UTIL****//
    //************//
    /**
     * Metodo que llaman los diferentes gráficos cuando se hace click en alguno
     * de sus nodos de datos (el tipo de nodo depende del filtro), para
     * recalibrar los datos por ese valor
     *
     * @param valueStr el nombre nodo elegido
     */
    private void onFilterValueClick(String valueStr) {
        /* Si ya está seleccionado el mismo valor, se deselecciona y muestra todo*/
        if (selectedValue != null && valueStr.equals(selectedValue.toString())) {
            selectedValue = null;
            pieChartConf(listVehicles);
            insuranceConfig(listVehicles);
            itv_rentConfig(listVehicles);
            chooseVehicles.setText("Todos los Vehículos");
        } else {
            /* Cambiar el selectedValue según el filtro activo*/
            switch (filter) {
                case TYPE -> {
                    selectedValue = TravellerOffice.valueOf(valueStr);
                    chooseVehicles.setText("Vehículos - " + valueStr);
                }
                case STATUS -> {
                    selectedValue = TravellerTrip.valueOf(valueStr.replace(' ', '_'));
                    chooseVehicles.setText("Vehículos - " + valueStr);
                }
                default -> {
                    chooseVehicles.setText("Todos los Vehículos");
                    selectedValue = null;
                    pieChartConf(listVehicles);
                    insuranceConfig(listVehicles);
                    itv_rentConfig(listVehicles);
                    return;
                }

            }

            /*Filtrar la lista según filtro activo y valor seleccionado*/
            List<Traveller> filtered = listVehicles.stream()
                    .filter(v -> {
                        return switch (filter) {
                            case TYPE ->
                                v.getTrip().equals(selectedValue);
                            case STATUS ->
                                v.getTrip().equals(selectedValue);
                            default ->
                                true;
                        };
                    })
                    .collect(Collectors.toList());

            /* Actualizar gráficas con la lista filtrada*/
            pieChartConf(filtered);
            insuranceConfig(filtered);
            itv_rentConfig(filtered);
        }
        rechargeLabels();
    }

    /**
     * Metodo que cambia el filtro por el que se busca
     */
    @FXML
    private void filterTBChange() {
        if (filterTBC.isSelected()) {
            filter = VehicleFilter.TYPE;
            filterTBC.setText("Titularidad");
            pieChart.setTitle("Distribución por Titularidad");

        } else {
            filter = VehicleFilter.STATUS;
            filterTBC.setText("Estado");
            pieChart.setTitle("Distribución por Estado");
        }
        chooseVehicles.setText("Todos los Vehículos");
        pieChartConf(listVehicles);
        insuranceConfig(listVehicles);
        itv_rentConfig(listVehicles);
        rechargeLabels();
    }

    /**
     * Metodo que calcula los dias y devuelve un valor integer para ser tratado
     * dependiendo de la fecha
     *
     * @param date Fecha por la que calcular los días
     * @return valor a tratar: null(4), Mayor a 730 dias(0), Mayor a 365
     * dias(1), Mayor a 180 dias(2) y otro (3)
     */
    private int getCategoryByDate(LocalDate date) {
        if (date == null) {
            return 4;
        }

        long days = Math.abs(ChronoUnit.DAYS.between(LocalDate.now(), date));

        if (days > 730) {
            return 0;
        } else if (days > 365) {
            return 1;
        } else if (days > 180) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Metodo que recupera por que valor se va a clasificar los vehículos
     *
     * @param filter Indica el valor del filtro, pudiendose ser dos;
     * @return retorna la función de clasificación
     */
    private Function<Traveller, ?> getClassifierByFilter(VehicleFilter filter) {
        return switch (filter) {
            case STATUS ->
                Traveller::getOffice;
            case TYPE ->
                Traveller::getTrip;
        };
    }

    /**
     * Metodo que devuelve si el vehículo elegido cumple con las características
     * del filtro actual
     *
     * @param v el vehículo a comprobar
     * @return si hace match o no
     */
    private boolean matchesFilter(Traveller v) {
        if (selectedValue == null) {
            return true;
        }
        return switch (filter) {
            case TYPE ->
                v.getTrip().equals(selectedValue);
            case STATUS ->
                v.getOffice().equals(selectedValue);
        };
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

    private String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

}
