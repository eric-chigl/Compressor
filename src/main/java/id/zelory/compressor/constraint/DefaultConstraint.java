package id.zelory.compressor.constraint;

import android.graphics.Bitmap;
import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class DefaultConstraint implements Constraint {
    private static final int DEFAULT_WIDTH = 612;
    private static final int DEFAULT_HEIGHT = 816;
    private static final Bitmap.CompressFormat DEFAULT_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_QUALITY = 80;

    private final int width;
    private final int height;
    private final Bitmap.CompressFormat format;
    private final int quality;
    private boolean isResolved = false;

    public DefaultConstraint() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_FORMAT, DEFAULT_QUALITY);
    }

    public DefaultConstraint(int width, int height) {
        this(width, height, DEFAULT_FORMAT, DEFAULT_QUALITY);
    }

    public DefaultConstraint(int width, int height, Bitmap.CompressFormat format, int quality) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.quality = quality;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        return isResolved;
    }

    @Override
    public File satisfy(File imageFile) {
        Bitmap bitmap = ImageProcessor.decodeSampledBitmapFromFile(imageFile, width, height);
        bitmap = ImageProcessor.determineImageRotation(imageFile, bitmap);
        File result = ImageProcessor.overWrite(imageFile, bitmap, format, quality);
        isResolved = true;
        return result;
    }
}
