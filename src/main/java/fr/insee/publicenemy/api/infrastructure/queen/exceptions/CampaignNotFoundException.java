package fr.insee.publicenemy.api.infrastructure.queen.exceptions;

import java.io.Serial;

public class CampaignNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -294974946305513071L;

    public CampaignNotFoundException(String message) {
        super(message);
    }
}
