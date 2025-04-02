package fr.insee.publicenemy.api.infrastructure.pogues;

import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Objects;

/**
 * Permits to upload content through webclient as if it was a file
 */
public class FileNameAwareByteArrayResource implements Resource {
    private final String fileName;

    private final byte[] byteArray;

    private final String description;

    public FileNameAwareByteArrayResource(String fileName, byte[] byteArray, String description) {
        this.fileName = fileName;
        this.byteArray = byteArray;
        this.description = description;
    }

    /**
     * This implementation return the filename
     */
    @Override
    public String getFilename() {
        return fileName;
    }

    /**
     * This implementation returns a description that includes the passed-in
     * {@code description}, if any.
     */
    @Override
    public String getDescription() {
        return "Byte array resource [" + this.description + "]";
    }

    /**
     * This implementation returns a ByteArrayInputStream for the
     * underlying byte array.
     * @see java.io.ByteArrayInputStream
     */
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.byteArray);
    }

    /**
     * This implementation always returns {@code true}.
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * This implementation always returns {@code true}
     */
    @Override
    public boolean isReadable() {
        return true;
    }

    /**
     * This implementation returns the length of the underlying byte array.
     */
    @Override
    public long contentLength() {
        return this.byteArray.length;
    }

    /**
     * This implementation always returns {@code false}.
     */
    @Override
    public boolean isOpen() {
        return false;
    }

    /**
     * This implementation always returns {@code false}.
     */
    @Override
    public boolean isFile() {
        return false;
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to a URL.
     */
    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    /**
     * This implementation builds a URI based on the URL returned
     * by {@link #getURL()}.
     */
    @Override
    public URI getURI() throws IOException {
        URL url = getURL();
        try {
            return ResourceUtils.toURI(url);
        }
        catch (URISyntaxException ex) {
            throw new IOException("Invalid URI [" + url + "]", ex);
        }
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to an absolute file path.
     */
    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    /**
     * This implementation returns {@link Channels#newChannel(InputStream)}
     * with the result of {@link #getInputStream()}.
     * <p>This is the same as in {@link Resource}'s corresponding default method
     * but mirrored here for efficient JVM-level dispatching in a class hierarchy.
     */
    @Override
    public ReadableByteChannel readableChannel() {
        return Channels.newChannel(getInputStream());
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource is not a file.
     */
    @Override
    public long lastModified() throws IOException {
        throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for checking its last-modified timestamp");
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that relative resources cannot be created for this resource.
     */
    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileNameAwareByteArrayResource that = (FileNameAwareByteArrayResource) o;
        return Objects.equals(fileName, that.fileName) && Arrays.equals(byteArray, that.byteArray) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fileName, description);
        result = 31 * result + Arrays.hashCode(byteArray);
        return result;
    }
}
