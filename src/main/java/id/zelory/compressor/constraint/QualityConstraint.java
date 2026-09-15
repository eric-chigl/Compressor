package id.zelory.compressor.constraint;

import android.graphics.Bitmap;
import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class QualityConstraint implements Constraint {
    private final int quality;
    private boolean isResolved = false;

    public QualityConstraint(int quality) {
        this.quality = quality;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        return isResolved;
    }

    @Override
    public File satisfy(File imageFile) {
        Bitmap bitmap = ImageProcessor.loadBitmap(imageFile);
        File result = ImageProcessor.overWrite(imageFile, bitmap, quality);
        isResolved = true;
        return result;
    }
}
