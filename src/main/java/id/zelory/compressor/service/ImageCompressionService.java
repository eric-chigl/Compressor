package id.zelory.compressor.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import id.zelory.compressor.Compressor;
import id.zelory.compressor.CompressorBuilder;
import id.zelory.compressor.Compression;
import id.zelory.compressor.ImageProcessor;
import id.zelory.compressor.constraint.DefaultConstraint;
import id.zelory.compressor.constraint.DestinationConstraint;
import id.zelory.compressor.constraint.FormatConstraint;
import id.zelory.compressor.constraint.QualityConstraint;
import id.zelory.compressor.constraint.ResolutionConstraint;
import id.zelory.compressor.constraint.SizeConstraint;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Backend service utility class for image compression operations.
 * Provides high-level API for various image compression scenarios.
 */
public class ImageCompressionService {
    private static final String TAG = "ImageCompressionService";
    private final Context context;
    private final Map<String, CompressionConfig> configCache = new HashMap<>();

    public ImageCompressionService(Context context) {
        this.context = context;
    }

    /**
     * Compress image with default settings (612x816, JPEG, quality 80).
     */
    public File compressWithDefault(File imageFile) {
        return Compressor.compress(context, imageFile);
    }

    /**
     * Compress image with custom resolution.
     */
    public File compressWithResolution(File imageFile, int width, int height) {
        return Compressor.builder(context, imageFile)
                .resolution(width, height)
                .compress();
    }

    /**
     * Compress image with quality constraint.
     */
    public File compressWithQuality(File imageFile, int quality) {
        return Compressor.builder(context, imageFile)
                .quality(quality)
                .compress();
    }

    /**
     * Compress image with format conversion.
     */
    public File compressWithFormat(File imageFile, Bitmap.CompressFormat format) {
        return Compressor.builder(context, imageFile)
                .format(format)
                .compress();
    }

    /**
     * Compress image to fit maximum file size.
     */
    public File compressWithMaxSize(File imageFile, long maxFileSize) {
        return Compressor.builder(context, imageFile)
                .size(maxFileSize)
                .compress();
    }

    /**
     * Compress image with multiple constraints.
     */
    public File compressWithMultiple(File imageFile, int width, int height, int quality, long maxFileSize) {
        return Compressor.builder(context, imageFile)
                .resolution(width, height)
                .quality(quality)
                .size(maxFileSize)
                .compress();
    }

    /**
     * Compress image to specific destination.
     */
    public File compressToDestination(File imageFile, File destination) {
        return Compressor.builder(context, imageFile)
                .setDefault()
                .destination(destination)
                .compress();
    }

    /**
     * Compress image with custom configuration builder.
     */
    public File compressWithBuilder(File imageFile, ConfigBuilder configBuilder) {
        CompressorBuilder builder = Compressor.builder(context, imageFile);
        configBuilder.configure(builder);
        return builder.compress();
    }

    /**
     * Asynchronously compress image with callback.
     */
    public void compressAsync(final File imageFile, final CompressionCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                File result = compressWithDefault(imageFile);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Compression failed", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
        thread.start();
    }

    /**
     * Asynchronously compress image with custom builder and callback.
     */
    public void compressAsync(final File imageFile, final ConfigBuilder configBuilder, 
                             final CompressionCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                File result = compressWithBuilder(imageFile, configBuilder);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Async compression failed", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
        thread.start();
    }

    /**
     * Get image information without compression.
     */
    public ImageInfo getImageInfo(File imageFile) {
        return new ImageInfo(imageFile);
    }

    /**
     * Save compression configuration for reuse.
     */
    public void saveConfig(String configName, CompressionConfig config) {
        configCache.put(configName, config);
    }

    /**
     * Load saved compression configuration.
     */
    public CompressionConfig getConfig(String configName) {
        return configCache.get(configName);
    }

    /**
     * Apply saved configuration to compress image.
     */
    public File compressWithConfig(File imageFile, String configName) {
        CompressionConfig config = getConfig(configName);
        if (config == null) {
            throw new IllegalArgumentException("Configuration not found: " + configName);
        }
        return compressWithBuilder(imageFile, config.getConfigBuilder());
    }

    @FunctionalInterface
    public interface ConfigBuilder {
        void configure(CompressorBuilder builder);
    }

    public interface CompressionCallback {
        void onSuccess(File result);
        void onError(Exception error);
    }

    public static class CompressionConfig {
        private final ConfigBuilder configBuilder;

        public CompressionConfig(ConfigBuilder configBuilder) {
            this.configBuilder = configBuilder;
        }

        public ConfigBuilder getConfigBuilder() {
            return configBuilder;
        }
    }

    public static class ImageInfo {
        private final File file;
        private final long fileSize;
        private final String extension;
        private final Bitmap.CompressFormat format;

        public ImageInfo(File file) {
            this.file = file;
            this.fileSize = file.length();
            this.extension = ImageProcessor.getFileExtension(file);
            this.format = ImageProcessor.getCompressFormat(file);
        }

        public File getFile() {
            return file;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getExtension() {
            return extension;
        }

        public Bitmap.CompressFormat getFormat() {
            return format;
        }

        public String getFormattedSize() {
            return formatFileSize(fileSize);
        }

        private String formatFileSize(long bytes) {
            if (bytes <= 0) return "0 B";
            String[] units = {"B", "KB", "MB", "GB"};
            int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
            double size = bytes / Math.pow(1024, unitIndex);
            return String.format("%.2f %s", size, units[unitIndex]);
        }
    }
}
