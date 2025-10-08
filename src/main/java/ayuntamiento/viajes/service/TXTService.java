package ayuntamiento.viajes.service;

import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Traveller;
import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase que se encarga del comportamiento del pdf, de la escritura del mismo y
 * la elección de los diferentes tipos
 *
 * @author Cristian Delgado Cruz
 * @since 2025-10-08
 * @version 1.0
 */
public class TXTService {

    private final static TravellerService travellerS;

    static {
        travellerS = new TravellerService();
    }

    /**
     * Metodo que recoge todos los travellers por viaje, mandando a generar el
     * txt
     *
     * @param name nombre elegido para el documento
     * @param dir directorio elegido para colocar el documento
     * @param trip codigo del viaje
     * @throws ControledException Excepciones que mostramos al usuario vienen de
     * print
     * @throws Exception Excepciones que no mostramos al usuario vienen de print
     */
    public void printByTravel(String name, String dir, long trip) throws ControledException, Exception {
        List<Traveller> travellerList;
        travellerList = travellerS.findByTrip(trip);
        
        File txtFile = new File(dir, name);
        try (FileWriter writer = new FileWriter(txtFile)) {
            for (Traveller t : travellerList) {
                writer.write(t.getPhone() + System.lineSeparator()); 
            }
        } catch (IOException e) {
            throw new ControledException("El TXT no pudo ser generado. "
                    + "Revise la carpeta elegida", "TXTService - print");
        }

        /*Abrir el TXT generado automáticamente*/
        if (txtFile.exists()) {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(txtFile);
            } else {
                throw new ControledException("El TXT no pudo ser abierto automáticamente. "
                        + "Revise la carpeta elegida", "TXTService - print");
            }
        } else {
            throw new ControledException("El TXT no pudo ser generado automáticamente. "
                    + "Revise la carpeta elegida y sus permisos sobre ella", "TXTService - print");
        }
    }
}
