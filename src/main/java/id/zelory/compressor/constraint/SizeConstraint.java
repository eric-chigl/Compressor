package id.zelory.compressor.constraint;

import android.graphics.Bitmap;
import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class SizeConstraint implements Constraint {
    private static final int DEFAULT_STEP_SIZE = 10;
    private static final int DEFAULT_MAX_ITERATION = 10;
    private static final int DEFAULT_MIN_QUALITY = 10;

    private final long maxFileSize;
    private final int stepSize;
    private final int maxIteration;
    private final int minQuality;
    private int iteration = 0;

    public SizeConstraint(long maxFileSize) {
        this(maxFileSize, DEFAULT_STEP_SIZE, DEFAULT_MAX_ITERATION, DEFAULT_MIN_QUALITY);
    }

    public SizeConstraint(long maxFileSize, int stepSize, int maxIteration) {
        this(maxFileSize, stepSize, maxIteration, DEFAULT_MIN_QUALITY);
    }

    public SizeConstraint(long maxFileSize, int stepSize, int maxIteration, int minQuality) {
        this.maxFileSize = maxFileSize;
        this.stepSize = stepSize;
        this.maxIteration = maxIteration;
        this.minQuality = minQuality;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        return imageFile.length() <= maxFileSize || iteration >= maxIteration;
    }

    @Override
    public File satisfy(File imageFile) {
        iteration++;
        int quality = Math.max(100 - iteration * stepSize, minQuality);
        Bitmap bitmap = ImageProcessor.loadBitmap(imageFile);
        return ImageProcessor.overWrite(imageFile, bitmap, quality);
    }
}
