package fr.insee.publicenemy.api.infrastructure.ddi;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;

/**
 * Permits to upload content through webclient as if it was a file
 */
public class FileNameAwareByteArrayResource extends ByteArrayResource {
    private String fileName;

    public FileNameAwareByteArrayResource(String fileName, byte[] byteArray, String description) {
        super(byteArray, description);
        this.fileName = fileName;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null)
            return false;
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileNameAwareByteArrayResource other = (FileNameAwareByteArrayResource) obj;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        return true;
    }
}
