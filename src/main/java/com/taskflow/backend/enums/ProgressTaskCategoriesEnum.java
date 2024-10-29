package com.taskflow.backend.enums;

/**
 * Enum que representa las categorías de progreso de una tarea.
 */
public enum ProgressTaskCategoriesEnum {
    PENDIENTE("Pendiente"),    // La tarea aún no ha comenzado
    HACIENDO("Haciendo"),      // La tarea está en progreso
    FINALIZADA("Finalizada");   // La tarea ha sido completada

    private final String displayName;

    // Constructor del enum para establecer el nombre para mostrar
    ProgressTaskCategoriesEnum(String displayName) {
        this.displayName = displayName;
    }

    // Método para obtener el nombre para mostrar
    public String getDisplayName() {
        return displayName;
    }
}
