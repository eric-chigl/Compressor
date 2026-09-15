package id.zelory.compressor.constraint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import id.zelory.compressor.Constraint;
import id.zelory.compressor.ImageProcessor;
import java.io.File;

public class ResolutionConstraint implements Constraint {
    private final int width;
    private final int height;

    public ResolutionConstraint(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isSatisfied(File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        return options.outWidth - width <= 0 || options.outHeight - height <= 0;
    }

    @Override
    public File satisfy(File imageFile) {
        Bitmap bitmap = ImageProcessor.decodeSampledBitmapFromFile(imageFile, width, height);
        bitmap = ImageProcessor.determineImageRotation(imageFile, bitmap);
        return ImageProcessor.overWrite(imageFile, bitmap);
    }
}
