package id.zelory.compressor.constraint;

import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class DestinationConstraint implements Constraint {
    private final File destination;

    public DestinationConstraint(File destination) {
        this.destination = destination;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        return imageFile.getAbsolutePath().equals(destination.getAbsolutePath());
    }

    @Override
    public File satisfy(File imageFile) {
        return ImageProcessor.copyFile(imageFile, destination);
    }
}
