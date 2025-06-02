package ayuntamiento.viajes.service;

import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Vehicle;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.awt.Desktop;
import java.util.List;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que se encarga del comportamiento del pdf, de la escritura del mismo y
 * la elección de los diferentes tipos
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-20
 * @version 1.2
 */
public class PDFService {

    private int total;
    private int own;
    private int rent;
    private Map<Vehicle.VehicleStatus, Integer> statusVehicle;

    private final static VehicleService vehicleS;
    private List<Vehicle> vehiclesList;
    private List<Vehicle> firstPageVehicles;
    private List<Vehicle> remainingVehicles;

    private final Color[] COLORS = new Color[]{
        ColorConstants.BLACK,
        ColorConstants.WHITE,
        ColorConstants.RED,
        new DeviceRgb(0, 150, 80)
    };

    private final int numVehInitialPage = 22;
    private final int numVehNextPage = 27;

    static {
        vehicleS = new VehicleService();
    }

    /**
     * Metodo que recoge todos los vehículos y cuenta los que son propiedad del
     * ayuntamiento, y los que no lo son, mandando a generar el pdf
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param document tipo de documento (Notificaciones-Listado-Todo)
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printAll(String name, String dir, String document) throws ControledException, Exception {
        statusVehicle = new HashMap<>();
        own = 0;
        rent = 0;

        vehiclesList = vehicleS.findAll();
        firstPageVehicles = vehiclesList.stream().limit(numVehInitialPage).toList();
        remainingVehicles = vehiclesList.stream().skip(numVehInitialPage).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if (v.getType() == Vehicle.VehicleType.Propiedad) {
                own++;
            } else {
                rent++;
            }
            statusVehicle.put(v.getStatus(), statusVehicle.getOrDefault(v.getStatus(), 0) + 1);
        });

        print(name, dir, "Todos", document);
    }

    /**
     * Metodo que recoge los vehículos por tipo y manda a generar el pdf.
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param type titularidad del vehículo por el cual se va a filtrar
     * @param document tipo de documento (Notificaciones-Listado-Todo)
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printType(String name, String dir, String type, String document) throws ControledException, Exception {
        statusVehicle = new HashMap<>();
        own = 0;
        rent = 0;

        switch (type) {
            case "Propiedad" ->
                vehiclesList = vehicleS.findByType(0);
            case "Alquiler" ->
                vehiclesList = vehicleS.findByType(1);
            case "Otro" ->
                vehiclesList = vehicleS.findByType(2);
            default -> {
            }
        }

        firstPageVehicles = vehiclesList.stream().limit(numVehInitialPage).toList();
        remainingVehicles = vehiclesList.stream().skip(numVehInitialPage).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if ("Propiedad".equals(v.getType().toString())) {
                own++;
            } else {
                rent++;
            }
            statusVehicle.put(v.getStatus(), statusVehicle.getOrDefault(v.getStatus(), 0) + 1);
        });
        print(name, dir, type, document);
    }

    /**
     * Metodo que recoge todos los vehículos con notificación y manda a generar
     * el pdf
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param document tipo de documento (Notificaciones-Listado-Todo)
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printNotification(String name, String dir, String document) throws ControledException, Exception {
        statusVehicle = new HashMap<>();
        own = 0;
        rent = 0;

        vehiclesList = vehicleS.findByWarning();
        firstPageVehicles = vehiclesList.stream().limit(numVehInitialPage).toList();
        remainingVehicles = vehiclesList.stream().skip(numVehInitialPage).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if ("Propiedad".equals(v.getType().toString())) {
                own++;
            } else {
                rent++;
            }
            statusVehicle.put(v.getStatus(), statusVehicle.getOrDefault(v.getStatus(), 0) + 1);
        });
        print(name, dir, "Notificados", document);
    }

    /**
     * Función principal que se encarga de controlar los valores que le llegan
     * para llamar a las demás funciones y contruir el documento
     * 
     * @param name Nombre del PDF
     * @param dir Dirección de la carpeta
     * @param type Tipo de los vehiculos (Propiedad/Alquilado/Otro) para el label
     * @param document Tipo del documento (Todo/Listado/Notificaciones) para elegir la plantilla
     * @throws ControledException Error Controlado que pasamos hacia arriba
     * @throws Exception Error No controlado que pasamos hacia arriba
     */
    private void print(String name, String dir, String type, String document) throws ControledException, Exception {

        File templatePDF;

        if ("Listado".equals(document)) {
            templatePDF = copyResourceToTempFile("ayuntamiento/viajes/template/info_base.pdf", "plantilla");
        } else {
            templatePDF = copyResourceToTempFile("ayuntamiento/viajes/template/notifications_base.pdf", "plantilla");
        }

        String outFolder = dir + File.separator + name + ".pdf";

        /*Crear PdfCanvas para dibujar texto*/
        PdfDocument pdfDocument = new PdfDocument(
                new PdfReader(templatePDF),
                new PdfWriter(outFolder));
        PdfPage page = pdfDocument.getPage(1);
        PdfCanvas canvas = new PdfCanvas(page);

        writeDate(canvas);
        writeLabels(canvas, type);
        if ("Todo".equals(document)) {
            writeTable(canvas, "Notificaciones");
            addPage(type, "add", "Notificaciones", pdfDocument, canvas);
            addPage(type, "base", "Listado", pdfDocument, canvas);
            addPage(type, "add", "Listado", pdfDocument, canvas);
        } else {
            writeTable(canvas, document);
            addPage(type, "add", document, pdfDocument, canvas);
        }

        pdfDocument.close();
        templatePDF.deleteOnExit();

        /*Abrir el PDF generado automáticamente*/
        File pdfFile = new File(outFolder);

        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                throw new ControledException("El PDF no pudo ser abierto automáticamente. "
                        + "Revise la carpeta elegida", "PDFService - print");
            }
        } else {
            throw new ControledException("El PDF no pudo ser generado automáticamente. "
                    + "Revise la carpeta elegida y sus permisos sobre ella", "PDFService - print");
        }
    }

    /**
     * Función que añade la página principal y luego llama a las funciones para
     * escribir en ella en orden.
     * 
     * @param type Tipo de los vehiculos (Propiedad/Alquilado/Otro) para el label
     * @param template Tipo de template que utiliza (add/base)
     * @param document Tipo de documento que utiliza (Listado/Notificaciones)
     * @param pdfDocument Documento PDF original, por el cual se añaden páginas
     * @param canvas PDF donde se escribe
     * @throws ControledException Error que mostramos al usuario y pasamos hacia arriba
     * @throws Exception Error que no mostramos al usuario y pasamos hacia arriba
     */
    private void addPage(String type, String template, String document, PdfDocument pdfDocument, PdfCanvas canvas) throws ControledException, Exception {
        File addTemplate;

        /*Añadir las diferentes páginas*/ 
        if ("Notificaciones".equals(document)) {
            try {
                addTemplate = copyResourceToTempFile("ayuntamiento/viajes/template/notifications_" + template + ".pdf", template);
                addTemplate.deleteOnExit();
            } catch (IOException ioE) {
                throw new Exception("La plantilla del PDF de notifications_" + template + "no fue encontrado en los recursos internos", ioE);
            }
        } else {
            try {
                addTemplate = copyResourceToTempFile("ayuntamiento/viajes/template/info_" + template + ".pdf", template);
                addTemplate.deleteOnExit();
            } catch (IOException ioE) {
                throw new Exception("La plantilla del PDF de info_" + template + "no fue encontrado en los recursos internos", ioE);
            }
        }
        
        if ("add".equals(template)) {
            for (int i = 0; i < remainingVehicles.size(); i += numVehNextPage) {
                List<Vehicle> pageVehicles = remainingVehicles.subList(
                        i, Math.min(i + numVehNextPage, remainingVehicles.size())
                );

                /*Abrimos la plantilla de página adicional*/
                try (PdfDocument tempAddDoc = new PdfDocument(new PdfReader(addTemplate))) {
                    PdfPage templatePage = tempAddDoc.getPage(1).copyTo(pdfDocument);
                    pdfDocument.addPage(templatePage);
                } catch (IOException ioE) {
                    throw new Exception("No se puede abrir la plantilla de página adicional", ioE);
                }

                /*Obtenemos el canvas de la nueva página añadida*/
                PdfPage newPage = pdfDocument.getPage(pdfDocument.getNumberOfPages());
                canvas = new PdfCanvas(newPage);
                writeAddTable(canvas, pageVehicles, document);
            }
        } else {
            /*Abrimos la plantilla de página adicional*/
            try (PdfDocument tempAddDoc = new PdfDocument(new PdfReader(addTemplate))) {
                PdfPage templatePage = tempAddDoc.getPage(1).copyTo(pdfDocument);
                pdfDocument.addPage(templatePage);
            } catch (IOException ioE) {
                throw new Exception("No se puede abrir la plantilla de página principal", ioE);
            }

            /*Obtenemos el canvas de la nueva página añadida*/
            PdfPage newPage = pdfDocument.getPage(pdfDocument.getNumberOfPages());
            canvas = new PdfCanvas(newPage);
            writeDate(canvas);
            writeLabels(canvas, type);
            writeTable(canvas, document);
        }
    }
    
    /**
     * Método que escribe la fecha en el pdf
     * 
     * @param canvas Pdf sobre el que se escribe
     * @throws IOException Error al escribir
     */
    private void writeDate(PdfCanvas canvas) throws IOException {
        canvas.beginText();
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        canvas.setFontAndSize(font, 12);
        canvas.setFillColor(COLORS[1]);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        float x = 490f;
        float y = 657f;
        canvas.moveText(x, y);
        canvas.showText(date);

        canvas.endText();
    }

    /**
     * Método que escribe la linea inicial de las páginas principales
     *
     * @param canvas Página pdf sobre la que se escribirá
     * @param type Tipo de los vehiculos (Propiedad/Alquilado/Otro)
     * @throws IOException Error de escritura
     */
    private void writeLabels(PdfCanvas canvas, String type) throws IOException {
        canvas.beginText();

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        canvas.setFontAndSize(font, 10);
        canvas.setFillColor(COLORS[0]);

        float y = 602f;

        float[] columnX = new float[]{
            35f, 77f, 128f, 178f, 216f, 259f, 309f, 356f, 412f};

        canvas.moveText(columnX[0], y);
        canvas.showText(String.valueOf(total));
        canvas.moveText(columnX[1] - columnX[0], 0);
        canvas.showText(String.valueOf(own));
        canvas.moveText(columnX[2] - columnX[1], 0);
        canvas.showText(String.valueOf(rent));
        canvas.moveText(columnX[3] - columnX[2], 0);
        canvas.showText(String.valueOf(statusVehicle.getOrDefault(Vehicle.VehicleStatus.Buen_Estado, 0)));
        canvas.moveText(columnX[4] - columnX[3], 0);
        canvas.showText(String.valueOf(statusVehicle.getOrDefault(Vehicle.VehicleStatus.Mal_Estado, 0)));
        canvas.moveText(columnX[5] - columnX[4], 0);
        canvas.showText(String.valueOf(statusVehicle.getOrDefault(Vehicle.VehicleStatus.Averiado, 0)));
        canvas.moveText(columnX[6] - columnX[5], 0);
        canvas.showText(String.valueOf(statusVehicle.getOrDefault(Vehicle.VehicleStatus.Reparado, 0)));
        canvas.moveText(columnX[7] - columnX[6], 0);
        canvas.showText(String.valueOf(statusVehicle.getOrDefault(Vehicle.VehicleStatus.Fuera_de_Servicio, 0)));

        canvas.moveText(columnX[8] - columnX[7], 16f);
        canvas.setFillColor(COLORS[2]);

        switch (type) {
            case "Propiedad" ->
                canvas.showText(String.valueOf("Solo vehículos propios."));
            case "Alquiler" ->
                canvas.showText(String.valueOf("Solo vehículos alquilados."));
            case "Otros" ->
                canvas.showText(String.valueOf("Otros vehículos."));
            case "Notificados" ->
                canvas.showText(String.valueOf("Solo vehículos con notificación."));
            default -> {
            }
        }
        canvas.endText();
    }

    /**
     * Método que escribe la tabla principal
     *
     * @param canvas Página pdf sobre la que se escribirá
     * @param document Tipo del documento (Todo/Listado/Notificaciones)
     * @throws IOException Error de escritura
     */
    private void writeTable(PdfCanvas canvas, String document) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(COLORS[0]);

        float startY;
        if ("Notificaciones".equals(document)) {
            startY = 555f;
        } else {
            startY = 560f;
        }

        float rowHeight = 20.55f;
        float y = startY;
        int wType;

        /*Posiciones absolutas para cada columna*/
        float[] columnX;
        if ("Notificaciones".equals(document)) {
            columnX = new float[]{
                28f, 80f, 200f, 260f, 335f, 405f, 470f, 525f
            };
        } else {
            columnX = new float[]{
                30f, 105f, 240f, 315f, 425f, 515f};
        }

        for (Vehicle v : firstPageVehicles) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getNumplate());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getVehicle());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getType().toString());

            if ("Notificaciones".equals(document)) {
                wType = getWarningType(v.getItv_RentDate(), v.getInsuranceDate(), v.getLast_CheckDate());
                canvas.moveText(columnX[3] - columnX[2], 0);
                canvas.showText(getWarning(wType));

                if (wType == 7 || wType == 6 || wType == 5 || wType == 3) {
                    canvas.setFillColor(COLORS[0]);
                }
                canvas.moveText(columnX[4] - columnX[3], 0);
                canvas.showText(v.getItv_rent());

                if (wType == 7 || wType == 6 || wType == 4 || wType == 2) {
                    canvas.setFillColor(COLORS[2]);
                } else {
                    canvas.setFillColor(COLORS[0]);
                }
                canvas.moveText(columnX[5] - columnX[4], 0);
                canvas.showText(v.getInsurance());

                canvas.setFillColor(COLORS[0]);
                canvas.moveText(columnX[6] - columnX[5], 0);
                if (v.getKms_last_check() != null) {
                    canvas.showText(v.getKms_last_check().toString() + " Km");
                } else {
                    canvas.showText("");
                }
                if (wType == 7 || wType == 5 || wType == 4 || wType == 1) {
                    canvas.setFillColor(COLORS[2]);
                } else if (v.getLast_CheckDate() != null
                        && ChronoUnit.DAYS.between(LocalDate.now(),
                                v.getLast_CheckDate()) > 0) {
                    canvas.setFillColor(COLORS[3]);
                } else {
                    canvas.setFillColor(COLORS[0]);
                }
                canvas.moveText(columnX[7] - columnX[6], 0);
                canvas.showText(v.getLast_check());
            } else {
                canvas.moveText(columnX[3] - columnX[2], 0);
                canvas.showText(v.getStatus().toString());

                canvas.moveText(columnX[4] - columnX[3], 0);
                canvas.showText(v.getDestination());

                canvas.moveText(columnX[5] - columnX[4], 0);
                canvas.showText(v.getAllocation());
            }

            canvas.endText();
            canvas.setFillColor(COLORS[0]);
            y -= rowHeight;
        }
    }

    /**
     * Metodo que escribe la tabla de las siguientes páginas a la principal
     *
     * @param canvas Página pdf sobre la que se escribirá
     * @param document Tipo del documento (Todo/Listado/Notificaciones)
     * @param pageVehicles Lista de Vehículos para escribir
     * @throws IOException Error de escritura
     */
    private void writeAddTable(PdfCanvas canvas, List<Vehicle> pageVehicles, String document) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(COLORS[0]);

        float startY;
        if ("Notificaciones".equals(document)) {
            startY = 635f;
        } else {
            startY = 642f;
        }

        float rowHeight = 20.54f;
        float y = startY;
        int wType;

        /*Posiciones absolutas para cada columna*/
        float[] columnX;
        if ("Notificaciones".equals(document)) {
            columnX = new float[]{
                28f, 80f, 200f, 260f, 335f, 405f, 470f, 525f
            };
        } else {
            columnX = new float[]{
                30f, 105f, 240f, 315f, 425f, 515f};
        }

        for (Vehicle v : pageVehicles) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getNumplate());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getVehicle());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getType().toString());

            if ("Notificaciones".equals(document)) {
                wType = getWarningType(v.getItv_RentDate(), v.getInsuranceDate(), v.getLast_CheckDate());
                canvas.moveText(columnX[3] - columnX[2], 0);
                canvas.showText(getWarning(wType));

                if (wType == 7 || wType == 6 || wType == 5 || wType == 3) {
                    canvas.setFillColor(COLORS[2]);
                }
                canvas.moveText(columnX[4] - columnX[3], 0);
                canvas.showText(v.getItv_rent());

                if (wType == 7 || wType == 6 || wType == 4 || wType == 2) {
                    canvas.setFillColor(COLORS[2]);
                } else {
                    canvas.setFillColor(COLORS[0]);
                }
                canvas.moveText(columnX[5] - columnX[4], 0);
                canvas.showText(v.getInsurance());

                canvas.setFillColor(COLORS[0]);
                canvas.moveText(columnX[6] - columnX[5], 0);
                if (v.getKms_last_check() != null) {
                    canvas.showText(v.getKms_last_check().toString() + " Km");
                } else {
                    canvas.showText("");
                }
                if (wType == 7 || wType == 5 || wType == 4 || wType == 1) {
                    canvas.setFillColor(COLORS[2]);
                } else if (v.getLast_CheckDate() != null
                        && ChronoUnit.DAYS.between(LocalDate.now(),
                                v.getLast_CheckDate()) > 0) {
                    canvas.setFillColor(COLORS[3]);
                } else {
                    canvas.setFillColor(COLORS[0]);
                }
                canvas.moveText(columnX[7] - columnX[6], 0);
                canvas.showText(v.getLast_check());
            } else {
                canvas.moveText(columnX[3] - columnX[2], 0);
                canvas.showText(v.getStatus().toString());

                canvas.moveText(columnX[4] - columnX[3], 0);
                canvas.showText(v.getDestination());

                canvas.moveText(columnX[5] - columnX[4], 0);
                canvas.showText(v.getAllocation());
            }

            canvas.endText();
            canvas.setFillColor(COLORS[0]);
            y -= rowHeight;
        }
    }

    /**
     * Método que copia plantilla desde resources a archivo temporal para poder 
     * trabajar con ella
     * 
     * @param resourcePath La ruta al archivo de la plantilla
     * @param tempFileName Nombre temporal del archivo (generado automáticamente)
     * @return El Fichero temporal
     * @throws Exception Error de no poder leer el archivo de la ruta especificada 
     * o derivados
     */
    private File copyResourceToTempFile(String resourcePath, String tempFileName) throws Exception {
        InputStream inputStream = PDFService.class
                .getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("No se encontró el recurso: " + resourcePath);
        }

        File tempFile = File.createTempFile(tempFileName, ".pdf");
        tempFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }catch(Exception ex){
            throw new Exception("Error al intentar leer los bytes del fichero", ex);
        }

        return tempFile;
    }
    /**
     * Método que evuelve el String del warning asociado
     * 
     * @param wType Valor del warning
     * @return String del warning
     */
    private String getWarning(int wType) {
        return switch (wType) {
            case 7 ->
                "Todos";
            case 6 ->
                "ITV/Alq y Seg";
            case 5 ->
                "ITV/Alq y Rev";
            case 4 ->
                "Seg y Rev";
            case 3 ->
                "ITV/Alq";
            case 2 ->
                "Seg";
            case 1 ->
                "Rev";
            default ->
                "";
        };
    }
    /**
     * Método que calcula los warnings para comunicarlo en la ficha
     * 
     * @param itv_rent Fecha del vehículo de la itv o alquiler
     * @param insurance Fecha del vehículo del seguro
     * @param lastCheck Fecha del vehículo de la última revisión
     * @return Valor del filtro
     */
    private int getWarningType(LocalDate itv_rent, LocalDate insurance, LocalDate lastCheck) {
        LocalDate today = LocalDate.now();

        boolean itv_rentNear = itv_rent != null && ChronoUnit.DAYS.between(today, itv_rent) <= 186;
        boolean insuranceNear = insurance != null && ChronoUnit.DAYS.between(today, insurance) <= 186;
        boolean lastCheckFar = lastCheck != null && ChronoUnit.DAYS.between(lastCheck, today) >= 365;

        if (itv_rentNear && insuranceNear && lastCheckFar) {
            return 7; // Los tres cerca o lejos
        } else if (itv_rentNear && insuranceNear) {
            return 6; // ITV/alquiler y Seguro cerca
        } else if (itv_rentNear && lastCheckFar) {
            return 5; // ITV/alquiler cerca y ultima revisión lejos
        } else if (insuranceNear && lastCheckFar) {
            return 4; // Seguro cerca y ultima revisión lejos
        } else if (itv_rentNear) {
            return 3; // Solo ITV/alquiler cerca
        } else if (insuranceNear) {
            return 2; // Solo seguro cerca
        } else if (lastCheckFar) {
            return 1; // Solo última revisión lejos
        } else {
            return 0;
        }
    }

}
