package fr.insee.publicenemy.api.application.dto;

import java.io.Serializable;

public class ApiFieldError implements Serializable {
    private static final long serialVersionUID = 1L;
    private String field;
    private String message;
    
    public String getField() {
        return field;
    }

    public void setField(String field) {        
        this.field = field;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApiFieldError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiFieldError [field=" + field + ", message=" + message + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiFieldError other = (ApiFieldError) obj;
        if (field == null) {
            if (other.field != null)
                return false;
        } else if (!field.equals(other.field))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }
}
