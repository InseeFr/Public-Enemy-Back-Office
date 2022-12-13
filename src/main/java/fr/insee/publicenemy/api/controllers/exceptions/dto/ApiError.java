package fr.insee.publicenemy.api.controllers.exceptions.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiError {
    private Integer status;

    private String path;

    private List<String> messages;

    @JsonIgnore
    private String debugMessage;

    @JsonIgnore
    private String exceptionName;

    @JsonInclude(Include.NON_NULL)
    private List<ApiFieldError> fieldErrors;

    @JsonIgnore
    private String stackTrace;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    private Date timestamp;

    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        this.messages.add(message);
    }
}