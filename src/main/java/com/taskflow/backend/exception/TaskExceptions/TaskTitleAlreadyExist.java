package com.taskflow.backend.exception.TaskExceptions;


public class TaskTitleAlreadyExist  extends RuntimeException{
    public TaskTitleAlreadyExist(String message) {
        super(message);
    }
}
