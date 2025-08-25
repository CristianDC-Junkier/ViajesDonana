package ayuntamiento.viajes.common;

import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

/**
 *
 * @version 1.0
 * @since 11/08/2025
 * @author Cristian Delgado Cruz
 */
public class ChoiceBoxUtil {

    public static void setDepartmentNameConverter(ChoiceBox<Department> choiceBox) {
        choiceBox.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                if (department == null) {
                    return "";
                }
                String name = department.getName();
                int underscoreIndex = name.indexOf('_');
                return underscoreIndex >= 0 ? name.replace('_', ' ') : name;
            }

            @Override
            public Department fromString(String string) {
                return null;
            }
        });
    }

    public static void setTravelConverter(ChoiceBox<Travel> choiceBox) {
        choiceBox.setConverter(new StringConverter<Travel>() {
            @Override
            public String toString(Travel travel) {
                if (travel == null) {
                    return "";
                } else if (travel.getBus() == 0) {
                    return travel.getDescriptor();
                } else {
                    return travel.getDescriptor() + " - Bus " + travel.getBus();
                }
            }

            @Override
            public Travel fromString(String string) {
                return null;
            }
        });
    }

    public static void setDisableArrow(ChoiceBox<Department> choiceBox) {
        Platform.runLater(() -> {
            Node arrow = choiceBox.lookup(".arrow");
            if (arrow != null) {
                arrow.setVisible(false);
                arrow.setManaged(false);
            }
        });
    }
}
