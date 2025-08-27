package ayuntamiento.viajes.service;

import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Travel;
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
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Clase que se encarga del comportamiento del pdf, de la escritura del mismo y
 * la elección de los diferentes tipos
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-03
 * @version 1.3
 */
public class PDFService {

    private final static TravellerService travellerS;
    private final static DepartmentService departmentS;
    private final static TravelService travelS;

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
        departmentS = new DepartmentService();
        travelS = new TravelService();
    }

    /**
     * Metodo que recoge todos los travellers y cuenta los que son propiedad del
     * ayuntamiento, y los que no lo son, mandando a generar el pdf
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param sort atributo sobre el que se va a ordenar la lista
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printAll(String name, String dir, String sort) throws ControledException, Exception {
        long adminDep = LoginService.getAdminDepartment().getId();
        List<Travel> travelList;

        if (adminDep == 7) {
            travelList = travelS.findAll();
        } else {
            travelList = travelS.findByDepartment(adminDep);
        }

        print(name, dir, travelList);
    }

    /**
     * Metodo que recoge los viajes por departamento y manda a generar el pdf.
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param trip viaje por el que se va a filtrar
     * @param sort criterio para ordenar la lista
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printType(String name, String dir, long trip, String sort) throws ControledException, Exception {
        travellersList = sort(travellerS.findByTrip(trip), sort);

        firstPageTravellers = travellersList.stream().limit(numTraInitialPage).toList();
        remainingTravellers = travellersList.stream().skip(numTraInitialPage).toList();

        List<Travel> t = List.of(travelS.findById(trip).get());
        print(name, dir, t);
    }

    /**
     * Función principal que se encarga de controlar los valores que le llegan
     * para llamar a las demás funciones y contruir el documento
     *
     * @param name Nombre del PDF
     * @param dir Dirección de la carpeta
     * @param trips Tipo de los viajes para el label
     * @throws ControledException Error Controlado que pasamos hacia arriba
     * @throws Exception Error No controlado que pasamos hacia arriba
     */
    private void print(String name, String dir, List<Travel> trips) throws ControledException, Exception {

        File templatePDF;

        templatePDF = copyResourceToTempFile("ayuntamiento/viajes/template/info_base.pdf", "plantilla");

        String outFolder = dir + File.separator + name + ".pdf";

        /*Crear PdfCanvas para dibujar texto*/
        PdfDocument pdfDocument = new PdfDocument(
                new PdfReader(templatePDF),
                new PdfWriter(outFolder));
        PdfPage page = pdfDocument.getPage(1);
        PdfCanvas canvas = new PdfCanvas(page);
        
        if (trips.size() > 1) {
            for (Travel t : trips) {
                travellersList = travellerS.findByTrip(t.getId());

                firstPageTravellers = travellersList.stream().limit(numTraInitialPage).toList();
                remainingTravellers = travellersList.stream().skip(numTraInitialPage).toList();
                writeDate(canvas);
                writeLabels(canvas, t);
                writeTable(canvas);
                addPage(pdfDocument, canvas);

                if (trips.getLast() != t) {
                    PdfDocument tempAddDoc = new PdfDocument(new PdfReader(templatePDF));
                    PdfPage templatePage = tempAddDoc.getPage(1).copyTo(pdfDocument);
                    pdfDocument.addPage(templatePage);
                    PdfPage newPage = pdfDocument.getPage(pdfDocument.getNumberOfPages());
                    canvas = new PdfCanvas(newPage);
                }
            }
        } else {
            writeDate(canvas);
            writeLabels(canvas, trips.getFirst());
            writeTable(canvas);
            addPage(pdfDocument, canvas);
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
     * @param pdfDocument Documento PDF original, por el cual se añaden páginas
     * @param canvas PDF donde se escribe
     * @throws ControledException Error que mostramos al usuario y pasamos hacia
     * arriba
     * @throws Exception Error que no mostramos al usuario y pasamos hacia
     * arriba
     */
    private void addPage(PdfDocument pdfDocument, PdfCanvas canvas) throws ControledException, Exception {
        File addTemplate;

        /*Añadir las diferentes páginas*/
        try {
            addTemplate = copyResourceToTempFile("ayuntamiento/viajes/template/info_add.pdf", "extra");
            addTemplate.deleteOnExit();
        } catch (IOException ioE) {
            throw new Exception("La plantilla del PDF de info_add no fue encontrado en los recursos internos", ioE);
        }

        for (int i = 0; i < remainingTravellers.size(); i += numTraNextPage) {
            List<Traveller> pageTravellers = remainingTravellers.subList(
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
            writeAddTable(canvas, pageTravellers);
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

        float x = 350f;
        float y = 657f;
        canvas.moveText(x, y);
        canvas.showText(date);

        canvas.endText();
    }

    /**
     * Método que escribe la linea inicial de las páginas principales
     *
     * @param canvas Página pdf sobre la que se escribirá
     * @param travel Tipo del viaje
     * @throws IOException Error de escritura
     */
    private void writeLabels(PdfCanvas canvas, Travel travel) throws IOException {
        canvas.beginText();

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        canvas.setFontAndSize(font, 10);
        canvas.setFillColor(COLORS[0]);

        float y = 612f;

        float[] columnX = new float[]{
            65f, 130f, 200f, 178f, 216f, 259f, 309f, 356f, 412f};

        canvas.moveText(columnX[0], y);
        canvas.showText(Integer.toString(travel.getSeats_total()));
        canvas.moveText(columnX[1] - columnX[0], 0);
        canvas.showText(Integer.toString(travel.getSeats_occupied()));
        canvas.moveText(columnX[2] - columnX[1], 0);
        canvas.showText(Integer.toString(travel.getSeats_total() - travel.getSeats_occupied()));

        canvas.moveText(columnX[6] - columnX[2], 10f);
        canvas.setFillColor(COLORS[2]);
        canvas.showText("Viaje " + travel.getDescriptor());

        canvas.endText();
    }

    /**
     * Método que escribe la tabla principal
     *
     * @param canvas Página pdf sobre la que se escribirá
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
            50f, 105f, 255f, 410f, 500f};

        for (Traveller v : firstPageTravellers) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getDni());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getName());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(travelS.findById(v.getTrip()).get().getDescriptor());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(departmentS.findById(v.getDepartment()).get().getName().replace('_', ' '));

            canvas.moveText(columnX[4] - columnX[3], 0);
            canvas.showText(v.getSignup());

            canvas.endText();
            canvas.setFillColor(COLORS[0]);
            y -= rowHeight;
        }
    }

    /**
     * Metodo que escribe la tabla de las siguientes páginas a la principal
     *
     * @param canvas Página pdf sobre la que se escribirá
     * @param pageTravellers Lista de Vehículos para escribir
     * @throws IOException Error de escritura
     */
    private void writeAddTable(PdfCanvas canvas, List<Traveller> pageTravellers) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(COLORS[0]);

        float startY = 635f;

        float rowHeight = 20.54f;
        float y = startY;

        /*Posiciones absolutas para cada columna*/
        float[] columnX = new float[]{
            50f, 105f, 260f, 415f, 500f};

        for (Traveller v : pageTravellers) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getDni());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getName());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(travelS.findById(v.getTrip()).get().getDescriptor());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(departmentS.findById(v.getDepartment()).get().getName().replace('_', ' '));

            canvas.moveText(columnX[4] - columnX[3], 0);
            canvas.showText(v.getSignup());

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

    private List<Traveller> sort(List<Traveller> list, String sort) {
        List<Traveller> sortList = null;
        switch (sort) {
            case "Nombre" -> {
                sortList = list.stream().sorted(Comparator.comparing(Traveller::getName)).collect(Collectors.toList());
            }
            case "Viaje" -> {
                //sortList = list.stream().sorted(Comparator.comparing(Traveller::getTrip).reversed()).collect(Collectors.toList());
            }
            case "Fecha de Inscripción" -> {
                sortList = list.stream().sorted(Comparator.comparing(Traveller::getSignUpDate)).collect(Collectors.toList());
            }
            case "Departamento" -> {
                //sortList = list.stream().sorted(Comparator.comparing(Traveller::getDepartment).reversed()).collect(Collectors.toList());
            }
            case "DNI" -> {
                sortList = list.stream().sorted(Comparator.comparing(Traveller::getDni)).collect(Collectors.toList());
            }
        }
        return sortList;
    }
}
