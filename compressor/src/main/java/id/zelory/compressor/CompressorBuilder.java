package id.zelory.compressor;

import android.content.Context;
import android.graphics.Bitmap;
import id.zelory.compressor.constraint.DefaultConstraint;
import id.zelory.compressor.constraint.DestinationConstraint;
import id.zelory.compressor.constraint.FormatConstraint;
import id.zelory.compressor.constraint.QualityConstraint;
import id.zelory.compressor.constraint.ResolutionConstraint;
import id.zelory.compressor.constraint.SizeConstraint;
import java.io.File;

public class CompressorBuilder {
    private final Context context;
    private final File imageFile;
    private final Compression compression;

    public CompressorBuilder(Context context, File imageFile) {
        this.context = context;
        this.imageFile = imageFile;
        this.compression = new Compression();
    }

    public CompressorBuilder setDefault() {
        compression.addConstraint(new DefaultConstraint());
        return this;
    }

    public CompressorBuilder setDefault(int width, int height) {
        compression.addConstraint(new DefaultConstraint(width, height));
        return this;
    }

    public CompressorBuilder setDefault(int width, int height, Bitmap.CompressFormat format, int quality) {
        compression.addConstraint(new DefaultConstraint(width, height, format, quality));
        return this;
    }

    public CompressorBuilder resolution(int width, int height) {
        compression.addConstraint(new ResolutionConstraint(width, height));
        return this;
    }

    public CompressorBuilder quality(int quality) {
        compression.addConstraint(new QualityConstraint(quality));
        return this;
    }

    public CompressorBuilder format(Bitmap.CompressFormat format) {
        compression.addConstraint(new FormatConstraint(format));
        return this;
    }

    public CompressorBuilder size(long maxFileSize) {
        compression.addConstraint(new SizeConstraint(maxFileSize));
        return this;
    }

    public CompressorBuilder size(long maxFileSize, int stepSize, int maxIteration) {
        compression.addConstraint(new SizeConstraint(maxFileSize, stepSize, maxIteration));
        return this;
    }

    public CompressorBuilder destination(File destination) {
        compression.addConstraint(new DestinationConstraint(destination));
        return this;
    }

    public CompressorBuilder addConstraint(Constraint constraint) {
        compression.addConstraint(constraint);
        return this;
    }

    public File compress() {
        return Compressor.compress(context, imageFile, compression);
    }

    public void compress(CompressCallback callback) {
        try {
            File result = compress();
            if (callback != null) callback.onSuccess(result);
        } catch (Exception e) {
            if (callback != null) callback.onError(e);
        }
    }

    @FunctionalInterface
    public interface CompressCallback {
        void onSuccess(File result);
        void onError(Exception error);
    }
}
