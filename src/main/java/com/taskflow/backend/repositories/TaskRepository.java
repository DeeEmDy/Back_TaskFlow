package com.taskflow.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taskflow.backend.entities.Task;
import com.taskflow.backend.enums.ProgressTaskCategoriesEnum;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    //Para buscar tareas por id de usuario
    List<Task> findByIdUserId(Integer userId);

    //Para realizar búsqueda: (Filtrado) de tareas por categoría de progreso.
    List<Task> findByProgressTask(ProgressTaskCategoriesEnum progressTask);

    //Para realizar búsqueda: (Filtrado) de tareas por estado. : true o false
    List<Task> findByStatus(Boolean status);

    boolean existsByTitle(String title); //Para verificar si el título ya existe

    Task findByTitle(String title); //Para buscar una tarea por título
}
