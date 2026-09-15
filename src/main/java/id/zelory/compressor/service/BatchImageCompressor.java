package id.zelory.compressor.service;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Batch image compression utility for processing multiple images.
 */
public class BatchImageCompressor {
    private static final String TAG = "BatchImageCompressor";
    private final ImageCompressionService compressionService;
    private final List<File> imageFiles;
    private ImageCompressionService.ConfigBuilder configBuilder;

    public BatchImageCompressor(Context context) {
        this.compressionService = new ImageCompressionService(context);
        this.imageFiles = new ArrayList<>();
    }

    public BatchImageCompressor addImage(File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            imageFiles.add(imageFile);
        }
        return this;
    }

    public BatchImageCompressor addImages(List<File> imageFiles) {
        if (imageFiles != null) {
            for (File file : imageFiles) {
                addImage(file);
            }
        }
        return this;
    }

    public BatchImageCompressor withConfig(ImageCompressionService.ConfigBuilder configBuilder) {
        this.configBuilder = configBuilder;
        return this;
    }

    public List<File> compress() {
        List<File> results = new ArrayList<>();
        for (File imageFile : imageFiles) {
            try {
                File result;
                if (configBuilder != null) {
                    result = compressionService.compressWithBuilder(imageFile, configBuilder);
                } else {
                    result = compressionService.compressWithDefault(imageFile);
                }
                results.add(result);
                Log.d(TAG, "Compressed: " + imageFile.getName());
            } catch (Exception e) {
                Log.e(TAG, "Failed to compress: " + imageFile.getName(), e);
            }
        }
        return results;
    }

    public void compressAsync(final BatchCompressionCallback callback) {
        Thread thread = new Thread(() -> {
            try {
                List<File> results = compress();
                if (callback != null) {
                    callback.onSuccess(results);
                }
            } catch (Exception e) {
                Log.e(TAG, "Batch compression failed", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
        thread.start();
    }

    public int getImageCount() {
        return imageFiles.size();
    }

    public void clear() {
        imageFiles.clear();
    }

    public interface BatchCompressionCallback {
        void onSuccess(List<File> results);
        void onError(Exception error);
    }
}
