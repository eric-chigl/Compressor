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

public class ImageProcessor {
    private static final String SEPARATOR = File.separator;
    private static final String COMPRESSOR_DIR = "compressor";
    private static final int BUFFER_SIZE = 1024;

    private ImageProcessor() {}

    public static String getCachePath(Context context) {
        return context.getCacheDir().getPath() + SEPARATOR + COMPRESSOR_DIR + SEPARATOR;
    }

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

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : "";
    }

    public static String getExtensionForFormat(Bitmap.CompressFormat format) {
        if (format == Bitmap.CompressFormat.PNG) {
            return "png";
        } else if (format == Bitmap.CompressFormat.WEBP) {
            return "webp";
        } else {
            return "jpg";
        }
    }

    public static Bitmap loadBitmap(File imageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        return determineImageRotation(imageFile, bitmap);
    }

    public static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        
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

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

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
            return bitmap;
        }
    }

    public static File copyToCache(Context context, File imageFile) {
        try {
            String cachePath = getCachePath(context);
            File cacheDir = new File(cachePath);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File destination = new File(cachePath + imageFile.getName());
            copyFile(imageFile, destination);
            return destination;
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file to cache", e);
        }
    }

    public static File copyFile(File source, File destination) {
        try {
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(destination);
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            } finally {
                fis.close();
                fos.close();
            }
            return destination;
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file", e);
        }
    }

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

    public static File overWrite(File imageFile, Bitmap bitmap) {
        return overWrite(imageFile, bitmap, getCompressFormat(imageFile), 100);
    }

    public static File overWrite(File imageFile, Bitmap bitmap, int quality) {
        return overWrite(imageFile, bitmap, getCompressFormat(imageFile), quality);
    }

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
                    //ignore
                }
            }
        }
    }

    public static void saveBitmap(Bitmap bitmap, File destination) {
        saveBitmap(bitmap, destination, getCompressFormat(destination), 100);
    }
}
