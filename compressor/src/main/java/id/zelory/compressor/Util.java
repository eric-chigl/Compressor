package id.zelory.compressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created on : January 24, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class Util {
    private static final String SEPARATOR = File.separator;
    private static final String COMPRESSOR_DIR = "compressor";

    private Util() {
        // Private constructor to prevent instantiation
    }

    private static String getCachePath(Context context) {
        return context.getCacheDir().getPath() + SEPARATOR + COMPRESSOR_DIR + SEPARATOR;
    }

    /**
     * Determine the compress format based on file extension.
     */
    public static Bitmap.CompressFormat getCompressFormat(File imageFile) {
        String extension = getFileExtension(imageFile).toLowerCase();
        switch (extension) {
            case "png":
                return Bitmap.CompressFormat.PNG;
            case "webp":
                return Bitmap.CompressFormat.WEBP;
            default:
                return Bitmap.CompressFormat.JPEG;
        }
    }

    /**
     * Get the file extension from a file.
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }

    /**
     * Get the file extension for a compress format.
     */
    public static String getExtensionForFormat(Bitmap.CompressFormat format) {
        if (format == Bitmap.CompressFormat.PNG) {
            return "png";
        } else if (format == Bitmap.CompressFormat.WEBP) {
            return "webp";
        } else {
            return "jpg";
        }
    }

    /**
     * Load bitmap from file and handle rotation.
     */
    public static Bitmap loadBitmap(File imageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        return determineImageRotation(imageFile, bitmap);
    }

    /**
     * Decode a sampled bitmap from file with specified dimensions.
     */
    public static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        
        // First, decode with inJustDecodeBounds = true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        
        // Calculate density ratio
        float outRatio = (float) options.outWidth / (float) options.outHeight;
        float reqRatio = (float) reqWidth / (float) reqHeight;
        
        if (outRatio > reqRatio) {
            options.inDensity = options.outHeight;
            options.inTargetDensity = reqHeight * options.inSampleSize;
        } else if (outRatio <= reqRatio) {
            options.inDensity = options.outWidth;
            options.inTargetDensity = reqWidth * options.inSampleSize;
        }
        
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
    }

    /**
     * Calculate the appropriate inSampleSize for bitmap scaling.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * Determine image rotation based on EXIF data.
     */
    public static Bitmap determineImageRotation(File imageFile, Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case 6:
                    matrix.postRotate(90);
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 8:
                    matrix.postRotate(270);
                    break;
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            // If EXIF reading fails, return the original bitmap
            return bitmap;
        }
    }

    /**
     * Copy image file to cache directory.
     */
    public static File copyToCache(Context context, File imageFile) throws IOException {
        String cachePath = getCachePath(context);
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        
        File destination = new File(cachePath + imageFile.getName());
        copyFile(imageFile, destination);
        return destination;
    }

    /**
     * Copy one file to another.
     */
    private static void copyFile(File source, File destination) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(destination);
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } finally {
            fis.close();
            fos.close();
        }
    }

    /**
     * Overwrite an image file with compressed bitmap.
     */
    public static File overWrite(File imageFile, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        File result = imageFile;
        
        if (format != getCompressFormat(imageFile)) {
            String newPath = imageFile.getAbsolutePath().substring(0, imageFile.getAbsolutePath().lastIndexOf('.')) + 
                           "." + getExtensionForFormat(format);
            result = new File(newPath);
        }
        
        imageFile.delete();
        saveBitmap(bitmap, result, format, quality);
        return result;
    }

    /**
     * Overwrite with default format and quality.
     */
    public static File overWrite(File imageFile, Bitmap bitmap) {
        return overWrite(imageFile, bitmap, getCompressFormat(imageFile), 100);
    }

    /**
     * Overwrite with specific quality.
     */
    public static File overWrite(File imageFile, Bitmap bitmap, int quality) {
        return overWrite(imageFile, bitmap, getCompressFormat(imageFile), quality);
    }

    /**
     * Save bitmap to file.
     */
    public static void saveBitmap(Bitmap bitmap, File destination, Bitmap.CompressFormat format, int quality) {
        File parentDir = destination.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destination.getAbsolutePath());
            bitmap.compress(format, quality, fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save bitmap", e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Save bitmap with default format and quality.
     */
    public static void saveBitmap(Bitmap bitmap, File destination) {
        saveBitmap(bitmap, destination, getCompressFormat(destination), 100);
    }
}
