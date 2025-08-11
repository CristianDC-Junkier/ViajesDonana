package ayuntamiento.viajes.model;

/**
 *
 * @author Ramón Iglesias
 */
public class Travel {

    private long id;
    private String descriptor;
    private int seats_occupied;
    private int seats_total;
    private Department department;
    private int bus;

    public Travel() {

    }

    public Travel(String descriptor, int seats_ocupied, int seats_total, Department department, int bus) {
        this.descriptor = descriptor;
        this.seats_occupied = seats_ocupied;
        this.seats_total = seats_total;
        this.department = department;
        this.bus = bus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public int getSeats_occupied() {
        return seats_occupied;
    }

    public void setSeats_ocuppied(int seats) {
        this.seats_occupied = seats;
    }

    public int getSeats_total() {
        return seats_total;
    }

    public void setSeats_total(int seats) {
        this.seats_total = seats;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public int getBus() {
        return bus;
    }

    public void setBus(int bus) {
        this.bus = bus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Travel)) {
            return false;
        }
        Travel travel = (Travel) o;
        return id == travel.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

}
