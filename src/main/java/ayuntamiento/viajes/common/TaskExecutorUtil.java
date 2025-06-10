package ayuntamiento.viajes.common;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Clase que se utiliza para hacer que carga de funciones en segundo plano
 * sin afectar al entorno visual
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-06
 * @version 1.0
 */
public class TaskExecutorUtil {

    public static <T> void runAsync(Callable<T> taskCallable, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return taskCallable.call();
            }
        };

        task.setOnSucceeded(e -> {
            T result = task.getValue();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
        });

        task.setOnFailed(e -> {
            Throwable error = task.getException();
            if (onError != null) {
                onError.accept(error);
            }
        });

        new Thread(task).start();
    }
}
