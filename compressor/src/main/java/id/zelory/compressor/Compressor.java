package id.zelory.compressor;

import android.content.Context;
import id.zelory.compressor.constraint.Compression;
import id.zelory.compressor.constraint.Constraint;
import java.io.File;
import java.util.List;

/**
 * Created on : January 22, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class Compressor {
    private static final Compression DEFAULT_COMPRESSION = createDefaultCompression();

    private Compressor() {
        // Private constructor to prevent instantiation
    }

    /**
     * Compress an image file with optional compression constraints.
     *
     * @param context The Android application context
     * @param imageFile The image file to compress
     * @param compressionConsumer The compression configuration consumer
     * @return The compressed image file
     */
    public static File compress(Context context, File imageFile, CompressionConsumer compressionConsumer) {
        Compression compression = new Compression();
        if (compressionConsumer != null) {
            compressionConsumer.accept(compression);
        } else {
            applyDefaults(compression);
        }
        return compressInternal(context, imageFile, compression);
    }

    /**
     * Compress an image file with default compression constraints.
     *
     * @param context The Android application context
     * @param imageFile The image file to compress
     * @return The compressed image file
     */
    public static File compress(Context context, File imageFile) {
        return compress(context, imageFile, null);
    }

    private static File compressInternal(Context context, File imageFile, Compression compression) {
        File result = Util.copyToCache(context, imageFile);
        List<Constraint> constraints = compression.getConstraints();
        
        for (Constraint constraint : constraints) {
            while (!constraint.isSatisfied(result)) {
                result = constraint.satisfy(result);
            }
        }
        
        return result;
    }

    private static void applyDefaults(Compression compression) {
        compression.addConstraint(new id.zelory.compressor.constraint.DefaultConstraint());
    }

    private static Compression createDefaultCompression() {
        Compression compression = new Compression();
        applyDefaults(compression);
        return compression;
    }

    /**
     * Functional interface for compression configuration.
     */
    @FunctionalInterface
    public interface CompressionConsumer {
        void accept(Compression compression);
    }
}
