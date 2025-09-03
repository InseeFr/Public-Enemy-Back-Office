package fr.insee.publicenemy.api.application.domain.model;


import java.util.Arrays;
import java.util.Objects;

public record PdfRecap(String filename, byte[] content) {
    @Override
    public String toString() {
        return "PdfRecap{" +
                "filename='" + filename + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PdfRecap pdfRecap = (PdfRecap) o;
        return Objects.deepEquals(content, pdfRecap.content) && Objects.equals(filename, pdfRecap.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, Arrays.hashCode(content));
    }
}
