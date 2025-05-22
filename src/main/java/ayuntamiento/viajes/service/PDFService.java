package ayuntamiento.viajes.service;

import ayuntamiento.viajes.model.Vehicle;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
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

/**
 *
 * @author Cristian
 */
public class PDFService {

    private int total;
    private int own;
    private int rent;

    private final static VehicleService vehicleS;
    private List<Vehicle> vehiclesList;
    private List<Vehicle> firstPageVehicles;
    private List<Vehicle> remainingVehicles;

    static {
        vehicleS = new VehicleService();
    }

    public void printAll(String name, String dir) {
        own = 0;
        rent = 0;

        vehiclesList = vehicleS.findAll();
        firstPageVehicles = vehiclesList.stream().limit(22).toList();
        remainingVehicles = vehiclesList.stream().skip(22).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if ("Propio".equals(v.getType())) {
                own++;
            } else {
                rent++;
            }
        });
        print(name, dir, "Todos");
    }

    public void printType(String name, String dir, String type) {
        own = 0;
        rent = 0;
        
        switch(type){
            case "Propio" -> 
                vehiclesList = vehicleS.findByType(0);
            case "Alquilado" -> 
                vehiclesList = vehicleS.findByType(1);
            case "Prestado" -> 
                vehiclesList = vehicleS.findByType(2);
            default -> {
            }
        }

        firstPageVehicles = vehiclesList.stream().limit(22).toList();
        remainingVehicles = vehiclesList.stream().skip(22).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if ("Propio".equals(v.getType().toString())) {
                own++;
            } else {
                rent++;
            }
        });
        print(name, dir, type);
    }

    public void printNotification(String name, String dir) {
        own = 0;
        rent = 0;

        vehiclesList = vehicleS.findByWarning();
        firstPageVehicles = vehiclesList.stream().limit(22).toList();
        remainingVehicles = vehiclesList.stream().skip(22).toList();

        total = vehiclesList.size();

        vehiclesList.forEach(v -> {
            if ("Propio".equals(v.getType().toString())) {
                own++;
            } else {
                rent++;
            }
        });
        print(name, dir, "Notificados");
    }

    private void print(String name, String dir, String type) {

        File templatePDF = null;
        File addTemplate;

        try {
            templatePDF = copyResourceToTempFile("ayuntamiento/vehiculos/template/base.pdf", "plantilla");
            InputStream inputStream = PDFService.class.getClassLoader().getResourceAsStream("ayuntamiento/vehiculos/template/base.pdf");

            if (inputStream == null) {
                throw new FileNotFoundException("Archivo no encontrado");
            }
        } catch (IOException ex) {
            // HAY QUE MANDARLO HACIA ARRIBA
            System.out.println("FALLO 1");
        }

        String outFolder = dir + File.separator + name + ".pdf";

        // Crear PdfCanvas para dibujar texto
        try ( // Leer el PDF existente
                PdfDocument pdfDocument = new PdfDocument(
                        new PdfReader(templatePDF),
                        new PdfWriter(outFolder))) {
            PdfPage page = pdfDocument.getPage(1);
            PdfCanvas canvas = new PdfCanvas(page);

            writeDate(canvas);
            writeLabels(canvas, type);
            writeTable(canvas);

            // Añadir las diferentes páginas 
            addTemplate = copyResourceToTempFile("ayuntamiento/vehiculos/template/add.pdf", "add");

            for (int i = 0; i < remainingVehicles.size(); i += 27) {
                List<Vehicle> pageVehicles = remainingVehicles.subList(
                        i, Math.min(i + 27, remainingVehicles.size())
                );

                try ( // Abrimos la plantilla de página adicional
                        PdfDocument tempAddDoc = new PdfDocument(new PdfReader(addTemplate))) {
                    PdfPage templatePage = tempAddDoc.getPage(1).copyTo(pdfDocument);
                    pdfDocument.addPage(templatePage);
                } catch (Exception ex) {
                    // HAY QUE MANDARLO HACIA ARRIBA
                    System.out.println("FALLO 2." + i + " " + ex.getCause() + " " + ex.getMessage());
                }

                // Obtenemos el canvas de la nueva página añadida
                PdfPage newPage = pdfDocument.getPage(pdfDocument.getNumberOfPages());
                canvas = new PdfCanvas(newPage);
                writeAddTable(canvas, pageVehicles);
            }
        } catch (Exception ex) {
            // HAY QUE MANDARLO HACIA ARRIBA
            System.out.println("FALLO 2 " + ex.getCause() + " " + ex.getMessage());
        }

        // Abrir el PDF generado automáticamente
        File pdfFile = new File(outFolder);
        if (pdfFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(pdfFile);
                } catch (Exception ex) {
                    // HAY QUE MANDARLO HACIA ARRIBA
                    System.out.println("FALLO 4 " + ex.getCause() + " " + ex.getMessage());
                };
            }
        }
    }

    private void writeDate(PdfCanvas canvas) throws IOException {
        canvas.beginText();
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        canvas.setFontAndSize(font, 12);
        canvas.setFillColor(ColorConstants.WHITE);

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        float x = 430f;
        float y = 650;
        canvas.moveText(x, y);
        canvas.showText(date);

        canvas.endText();
    }

    private void writeLabels(PdfCanvas canvas, String type) throws IOException {
        canvas.beginText();

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(ColorConstants.BLACK);

        float x = 94f;
        float y = 605f;

        canvas.moveText(x, y);
        canvas.showText(String.valueOf(total));
        canvas.moveText(52f, 0);
        canvas.showText(String.valueOf(own));
        canvas.moveText(57f, 0);
        canvas.showText(String.valueOf(rent));

        canvas.moveText(120f, 10f);
        canvas.setFillColor(ColorConstants.RED);
        canvas.setFontAndSize(font, 10);

        if (total == 0) {
            canvas.showText(String.valueOf("No existen vehículos actualmente."));
        } else {
            switch (type) {
                case "Propio" ->
                    canvas.showText(String.valueOf("Solo vehículos propios."));
                case "Alquilado" ->
                    canvas.showText(String.valueOf("Solo vehículos alquilados."));
                case "Notificados" ->
                    canvas.showText(String.valueOf("Solo vehículos con notificación"));
                default -> {
                }
            }
        }
        canvas.endText();
    }

    private void writeTable(PdfCanvas canvas) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(ColorConstants.BLACK);

        float startY = 560f;
        float rowHeight = 21.25f;
        float y = startY;
        int wType;

        // Posiciones absolutas para cada columna
        float[] columnX = new float[]{
            80f, 145f, 210f, 270f, 335f, 420f, 480f
        };

        for (Vehicle v : firstPageVehicles) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getNumplate());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getBrand());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getModel());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getType().toString());

            wType = getWarningType(v.getItv_RentDate(), v.getInsuranceDate());
            canvas.moveText(columnX[4] - columnX[3], 0);
            canvas.showText(getWarning(wType));

            if (wType == 3 || wType == 2) {
                canvas.setFillColor(ColorConstants.RED);
            }
            canvas.moveText(columnX[5] - columnX[4], 0);
            canvas.showText(v.getItv_rent());

            if (wType == 3 || wType == 1) {
                canvas.setFillColor(ColorConstants.RED);
            } else {
                canvas.setFillColor(ColorConstants.BLACK);
            }
            canvas.moveText(columnX[6] - columnX[5], 0);
            canvas.showText(v.getInsurance());

            canvas.endText();
            canvas.setFillColor(ColorConstants.BLACK);
            y -= rowHeight;
        }
    }

    private void writeAddTable(PdfCanvas canvas, List<Vehicle> pageVehicles) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        canvas.setFontAndSize(font, 8);
        canvas.setFillColor(ColorConstants.BLACK);

        float startY = 648f;
        float rowHeight = 21.25f;
        float y = startY;
        int wType;

        // Posiciones absolutas para cada columna
        float[] columnX = new float[]{
            80f, 145f, 210f, 270f, 335f, 420f, 480f
        };

        for (Vehicle v : firstPageVehicles) {
            canvas.beginText();

            canvas.moveText(columnX[0], y);
            canvas.showText(v.getNumplate());

            canvas.moveText(columnX[1] - columnX[0], 0);
            canvas.showText(v.getBrand());

            canvas.moveText(columnX[2] - columnX[1], 0);
            canvas.showText(v.getModel());

            canvas.moveText(columnX[3] - columnX[2], 0);
            canvas.showText(v.getType().toString());

            wType = getWarningType(v.getItv_RentDate(), v.getInsuranceDate());
            canvas.moveText(columnX[4] - columnX[3], 0);
            canvas.showText(getWarning(wType));

            if (wType == 3 || wType == 2) {
                canvas.setFillColor(ColorConstants.RED);
            }
            canvas.moveText(columnX[5] - columnX[4], 0);
            canvas.showText(v.getItv_rent());

            if (wType == 3 || wType == 1) {
                canvas.setFillColor(ColorConstants.RED);
            } else {
                canvas.setFillColor(ColorConstants.BLACK);
            }
            canvas.moveText(columnX[6] - columnX[5], 0);
            canvas.showText(v.getInsurance());

            canvas.endText();
            canvas.setFillColor(ColorConstants.BLACK);
            y -= rowHeight;
        }
    }

    // Copiar plantilla desde resources a archivo temporal
    private File copyResourceToTempFile(String resourcePath, String tempFileName) throws IOException {
        InputStream inputStream = PDFService.class.getClassLoader().getResourceAsStream(resourcePath);
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
        }

        return tempFile;
    }

    private String getWarning(int wType) {
        switch (wType) {
            case 3:
                return "   Ambos    ";
            case 2:
                return "ITV/Alquiler";
            case 1:
                return "   Seguro   ";
            default:
                return "            ";
        }
    }

    private int getWarningType(LocalDate itv, LocalDate insurance) {
        LocalDate today = LocalDate.now();

        boolean itvNear = itv != null && ChronoUnit.DAYS.between(today, itv) <= 186;
        boolean insuranceNear = insurance != null && ChronoUnit.DAYS.between(today, insurance) <= 186;

        if (itvNear && insuranceNear) {
            return 3;
        } else if (itvNear) {
            return 2;
        } else if (insuranceNear) {
            return 1;
        } else {
            return 0;
        }
    }
}
