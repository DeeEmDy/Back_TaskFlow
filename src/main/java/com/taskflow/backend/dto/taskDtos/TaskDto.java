package com.taskflow.backend.dto.taskDtos;

import org.hibernate.internal.util.collections.Stack;

import com.taskflow.backend.enums.ProgressTaskCategoriesEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    
    private Integer idUser;
    private String title;
    private String descriptionTask;
    private String createdTaskDate;
    private String expirationTaskDate;
    private ProgressTaskCategoriesEnum progressTask;
    private String finalizationTaskDate;
    private Integer score;
}
