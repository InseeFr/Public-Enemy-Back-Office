package fr.insee.publicenemy.api.infrastructure.ddi.exceptions;

import java.io.Serial;

public class PoguesJsonNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -849428056698255064L;

    public PoguesJsonNotFoundException(String message){
        super(message);
    }
}
