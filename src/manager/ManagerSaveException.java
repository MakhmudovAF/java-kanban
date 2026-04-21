package manager;

/**
 * Исключение, возникающее при ошибках сохранения/загрузки менеджера.
 * Непроверяемое (unchecked), чтобы не менять сигнатуры методов интерфейса.
 */
public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}