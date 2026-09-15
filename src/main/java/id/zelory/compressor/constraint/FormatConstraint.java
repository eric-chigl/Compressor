package id.zelory.compressor.constraint;

import android.graphics.Bitmap;
import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class FormatConstraint implements Constraint {
    private final Bitmap.CompressFormat format;

    public FormatConstraint(Bitmap.CompressFormat format) {
        this.format = format;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        return format == ImageProcessor.getCompressFormat(imageFile);
    }

    @Override
    public File satisfy(File imageFile) {
        Bitmap bitmap = ImageProcessor.loadBitmap(imageFile);
        return ImageProcessor.overWrite(imageFile, bitmap, format, 100);
    }
}
