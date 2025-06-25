package fr.insee.publicenemy.api.infrastructure.csv.exceptions;

public class InterrogationCsvNotFoundException extends RuntimeException {
    public InterrogationCsvNotFoundException(String message) {
        super(message);
    }
}
