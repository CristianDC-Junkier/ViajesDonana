package ayuntamiento.viajes.common;

/**
 *
 * @author Ramón Iglesias
 */
public enum Departments {
    Colegios,
    Asuntos_Sociales,
    Deporte,
    Cultura,
    Ayuntamiento,
    Temporeros,
    Admin;

    @Override
    public String toString() {
        return name().replace('_', ' ');
    }
}
