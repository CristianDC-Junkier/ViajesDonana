package ayuntamiento.viajes.service;

import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Traveller;

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
    private Map<Traveller.TravellerTrip, Integer> tripTraveller;

    private final static TravellerService travellerS;
    private List<Traveller> travellersList;
    private List<Traveller> firstPageTravellers;
    private List<Traveller> remainingTravellers;

    private final Color[] COLORS = new Color[]{
        ColorConstants.BLACK,
        ColorConstants.WHITE,
        ColorConstants.RED,
        new DeviceRgb(0, 150, 80)
    };

    private final int numTraInitialPage = 22;
    private final int numTraNextPage = 27;

    static {
        travellerS = new TravellerService();
    }

    /**
     * Metodo que recoge todos los vehículos y cuenta los que son propiedad del
     * ayuntamiento, y los que no lo son, mandando a generar el pdf
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printAll(String name, String dir) throws ControledException, Exception {
        tripTraveller = new HashMap<>();
        own = 0;
        rent = 0;

        travellersList = travellerS.findAll();
        firstPageTravellers = travellersList.stream().limit(numTraInitialPage).toList();
        remainingTravellers = travellersList.stream().skip(numTraInitialPage).toList();

        total = travellersList.size();

        travellersList.forEach(v -> {
            if (v.getOffice() == Traveller.TravellerOffice.Propiedad) {
                own++;
            } else {
                rent++;
            }
            tripTraveller.put(v.getTrip(), tripTraveller.getOrDefault(v.getTrip(), 0) + 1);
        });

        print(name, dir, "Todos");
    }

    /**
     * Metodo que recoge los vehículos por tipo y manda a generar el pdf.
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param type titularidad del vehículo por el cual se va a filtrar
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printType(String name, String dir, String type) throws ControledException, Exception {
        tripTraveller = new HashMap<>();
        own = 0;
        rent = 0;

        switch (type) {
            case "Propiedad" ->
                travellersList = travellerS.findByTrip(0);
            case "Alquiler" ->
                travellersList = travellerS.findByTrip(1);
            case "Otro" ->
                travellersList = travellerS.findByTrip(2);
            default -> {
            }
        }

        firstPageTravellers = travellersList.stream().limit(numTraInitialPage).toList();
        remainingTravellers = travellersList.stream().skip(numTraInitialPage).toList();

        total = travellersList.size();

        travellersList.forEach(v -> {
            if ("Propiedad".equals(v.getOffice().toString())) {
                own++;
            } else {
                rent++;
            }
            tripTraveller.put(v.getTrip(), tripTraveller.getOrDefault(v.getTrip(), 0) + 1);
        });
        print(name, dir, type);
    }

    /**
     * Metodo que recoge todos los vehículos con notificación y manda a generar
     * el pdf
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printNotification(String name, String dir) throws ControledException, Exception {
        tripTraveller = new HashMap<>();
        own = 0;
        rent = 0;

        travellersList = travellerS.findAll();
        firstPageTravellers = travellersList.stream().limit(numTraInitialPage).toList();
        remainingTravellers = travellersList.stream().skip(numTraInitialPage).toList();

        total = travellersList.size();

        travellersList.forEach(v -> {
            if ("Propiedad".equals(v.getOffice().toString())) {
                own++;
            } else {
                rent++;
            }
            tripTraveller.put(v.getTrip(), tripTraveller.getOrDefault(v.getTrip(), 0) + 1);
        });
        print(name, dir, "Notificados");
    }

    /**
     * Función principal que se encarga de controlar los valores que le llegan
     * para llamar a las demás funciones y contruir el documento
     *
     * @param name Nombre del PDF
     * @param dir Dirección de la carpeta
     * @param type Tipo de los vehiculos (Propiedad/Alquilado/Otro) para el
     * label
     * @param document Tipo del documento (Todo/Listado/Notificaciones) para
     * elegir la plantilla
     * @throws ControledException Error Controlado que pasamos hacia arriba
     * @throws Exception Error No controlado que pasamos hacia arriba
     */
    private void print(String name, String dir, String type) throws ControledException, Exception {

        File templatePDF;

        templatePDF = copyResourceToTempFile("ayuntamiento/viajes/template/info_base.pdf", "plantilla");

        String outFolder = dir + File.separator + name + ".pdf";

        /*Crear PdfCanvas para dibujar texto*/
        PdfDocument pdfDocument = new PdfDocument(
                new PdfReader(templatePDF),
                new PdfWriter(outFolder));
        PdfPage page = pdfDocument.getPage(1);
        PdfCanvas canvas = new PdfCanvas(page);

        writeDate(canvas);
        writeLabels(canvas, type);

        writeTable(canvas);
        addPage(type, pdfDocument, canvas);

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
     * @param type Tipo de los vehiculos (Propiedad/Alquilado/Otro) para el
     * label
     * @param template Tipo de template que utiliza (add/base)
     * @param document Tipo de documento que utiliza (Listado/Notificaciones)
     * @param pdfDocument Documento PDF original, por el cual se añaden páginas
     * @param canvas PDF donde se escribe
     * @throws ControledException Error que mostramos al usuario y pasamos hacia
     * arriba
     * @throws Exception Error que no mostramos al usuario y pasamos hacia
     * arriba
     */
    private void addPage(String type, PdfDocument pdfDocument, PdfCanvas canvas) throws ControledException, Exception {
        File addTemplate;

        /*Añadir las diferentes páginas*/
        try {
            addTemplate = copyResourceToTempFile("ayuntamiento/viajes/template/info_add.pdf", "extra");
            addTemplate.deleteOnExit();
        } catch (IOException ioE) {
            throw new Exception("La plantilla del PDF de info_add no fue encontrado en los recursos internos", ioE);
        }

        for (int i = 0; i < remainingTravellers.size(); i += numTraNextPage) {
            List<Traveller> pageVehicles = remainingTravellers.subList(
                    i, Math.min(i + numTraNextPage, remainingTravellers.size())
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
            writeAddTable(canvas, pageVehicles);
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
        canvas.showText(String.valueOf(tripTraveller.getOrDefault(Traveller.TravellerTrip.Buen_Estado, 0)));
        canvas.moveText(columnX[4] - columnX[3], 0);
        canvas.showText(String.valueOf(tripTraveller.getOrDefault(Traveller.TravellerTrip.Mal_Estado, 0)));
        canvas.moveText(columnX[5] - columnX[4], 0);
        canvas.showText(String.valueOf(tripTraveller.getOrDefault(Traveller.TravellerTrip.Averiado, 0)));
        canvas.moveText(columnX[6] - columnX[5], 0);
        canvas.showText(String.valueOf(tripTraveller.getOrDefault(Traveller.TravellerTrip.Reparado, 0)));
        canvas.moveText(columnX[7] - columnX[6], 0);
        canvas.showText(String.valueOf(tripTraveller.getOrDefault(Traveller.TravellerTrip.Fuera_de_Servicio, 0)));

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
    private void writeTable(PdfCanvas canvas) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(COLORS[0]);

        float startY = 560f;

        float rowHeight = 20.55f;
        float y = startY;

        /*Posiciones absolutas para cada columna*/
        float[] columnX = new float[]{
            30f, 105f, 240f, 315f, 425f, 515f};

        for (Traveller v : firstPageTravellers) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getDni());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getName());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getOffice().toString());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getTrip().toString());
            
            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getSignUp());

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
     * @param pageTravellers Lista de Vehículos para escribir
     * @throws IOException Error de escritura
     */
    private void writeAddTable(PdfCanvas canvas, List<Traveller> pageTravellers) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(COLORS[0]);

        float startY = 642f;

        float rowHeight = 20.54f;
        float y = startY;

        /*Posiciones absolutas para cada columna*/
        float[] columnX = new float[]{
            30f, 105f, 240f, 315f, 425f, 515f};

        for (Traveller v : pageTravellers) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getDni());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getName());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getOffice().toString());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getTrip().toString());
            
            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getSignUp());

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
     * @param tempFileName Nombre temporal del archivo (generado
     * automáticamente)
     * @return El Fichero temporal
     * @throws Exception Error de no poder leer el archivo de la ruta
     * especificada o derivados
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
        } catch (Exception ex) {
            throw new Exception("Error al intentar leer los bytes del fichero", ex);
        }

        return tempFile;
    }
}
