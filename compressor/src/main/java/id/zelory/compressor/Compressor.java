package id.zelory.compressor;

import android.content.Context;
import id.zelory.compressor.constraint.DefaultConstraint;
import java.io.File;
import java.util.List;

public class Compressor {
    private Compressor() {}

    public static File compress(Context context, File imageFile, Compression compression) {
        if (context == null) throw new IllegalArgumentException("Context cannot be null");
        if (imageFile == null || !imageFile.exists()) throw new IllegalArgumentException("Image file does not exist");
        if (compression == null) throw new IllegalArgumentException("Compression cannot be null");

        File result = ImageProcessor.copyToCache(context, imageFile);
        List<Constraint> constraints = compression.getConstraints();
        
        for (Constraint constraint : constraints) {
            while (!constraint.isSatisfied(result)) {
                result = constraint.satisfy(result);
            }
        }
        return result;
    }

    public static File compress(Context context, File imageFile) {
        Compression compression = new Compression();
        compression.addConstraint(new DefaultConstraint());
        return compress(context, imageFile, compression);
    }

    public static CompressorBuilder builder(Context context, File imageFile) {
        return new CompressorBuilder(context, imageFile);
    }
}
