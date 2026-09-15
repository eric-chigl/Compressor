# 后端Java项目中使用图片压缩工具

## 1. Maven依赖配置

在你的项目 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>id.zelory</groupId>
    <artifactId>compressor</artifactId>
    <version>3.0.1-java8</version>
</dependency>
```

## 2. 基本使用方式

### 2.1 最简单的使用方式

```java
import id.zelory.compressor.Compressor;
import java.io.File;
import android.content.Context;

// 使用默认压缩配置
File imageFile = new File("/path/to/image.jpg");
File compressedFile = Compressor.compress(context, imageFile);
```

### 2.2 使用构建器模式（推荐）

```java
import id.zelory.compressor.Compressor;
import android.graphics.Bitmap;
import java.io.File;

File imageFile = new File("/path/to/image.jpg");

// 链式调用，灵活配置
File compressedFile = Compressor.builder(context, imageFile)
    .resolution(800, 600)        // 设置分辨率
    .quality(80)                  // 设置质量 (0-100)
    .format(Bitmap.CompressFormat.JPEG)  // 设置格式
    .compress();
```

## 3. 后端服务工具使用

### 3.1 创建服务实例

```java
import id.zelory.compressor.service.ImageCompressionService;
import android.content.Context;

ImageCompressionService compressionService = new ImageCompressionService(context);
```

### 3.2 常见压缩场景

#### 默认压缩
```java
File compressedFile = compressionService.compressWithDefault(imageFile);
```

#### 按分辨率压缩
```java
File compressedFile = compressionService.compressWithResolution(imageFile, 800, 600);
```

#### 按质量压缩
```java
File compressedFile = compressionService.compressWithQuality(imageFile, 75);
```

#### 按文件大小压缩
```java
// 压缩到最大 500KB
File compressedFile = compressionService.compressWithMaxSize(imageFile, 500 * 1024);
```

#### 格式转换
```java
File compressedFile = compressionService.compressWithFormat(imageFile, Bitmap.CompressFormat.PNG);
```

#### 多约束组合压缩
```java
File compressedFile = compressionService.compressWithMultiple(
    imageFile,
    800,              // width
    600,              // height
    80,               // quality
    500 * 1024        // maxFileSize (500KB)
);
```

#### 指定输出位置
```java
File destination = new File("/path/to/output/compressed.jpg");
File compressedFile = compressionService.compressToDestination(imageFile, destination);
```

### 3.3 异步压缩（后端常用）

```java
compressionService.compressAsync(imageFile, new ImageCompressionService.CompressionCallback() {
    @Override
    public void onSuccess(File result) {
        System.out.println("压缩成功: " + result.getAbsolutePath());
        // 处理压缩后的文件
    }

    @Override
    public void onError(Exception error) {
        System.err.println("压缩失败: " + error.getMessage());
    }
});
```

### 3.4 自定义配置构建

```java
File compressedFile = compressionService.compressWithBuilder(imageFile, builder -> {
    builder.resolution(1024, 768)
           .quality(90)
           .format(Bitmap.CompressFormat.JPEG);
});
```

## 4. 批量处理

### 4.1 批量压缩图片

```java
import id.zelory.compressor.service.BatchImageCompressor;
import java.util.List;
import java.io.File;

BatchImageCompressor batchCompressor = new BatchImageCompressor(context);

// 添加要压缩的图片
batchCompressor.addImage(new File("/path/to/image1.jpg"))
               .addImage(new File("/path/to/image2.jpg"))
               .addImage(new File("/path/to/image3.jpg"));

// 执行压缩
List<File> compressedFiles = batchCompressor.compress();
```

### 4.2 异步批量压缩

```java
batchCompressor.compressAsync(new BatchImageCompressor.BatchCompressionCallback() {
    @Override
    public void onSuccess(List<File> results) {
        System.out.println("批量压缩完成，共 " + results.size() + " 张");
    }

    @Override
    public void onError(Exception error) {
        System.err.println("批量压缩失败: " + error.getMessage());
    }
});
```

### 4.3 批量压缩with自定义配置

```java
List<File> results = new BatchImageCompressor(context)
    .addImage(imageFile1)
    .addImage(imageFile2)
    .addImage(imageFile3)
    .withConfig(builder -> builder.resolution(640, 480).quality(75))
    .compress();
```

## 5. 获取图片信息

```java
ImageCompressionService.ImageInfo imageInfo = compressionService.getImageInfo(imageFile);

System.out.println("文件大小: " + imageInfo.getFormattedSize());
System.out.println("文件扩展名: " + imageInfo.getExtension());
System.out.println("压缩格式: " + imageInfo.getFormat());
```

## 6. 保存和复用配置

```java
// 保存配置
ImageCompressionService.CompressionConfig config = 
    new ImageCompressionService.CompressionConfig(
        builder -> builder.resolution(800, 600).quality(80)
    );
compressionService.saveConfig("mobile_config", config);

// 复用配置
File compressedFile = compressionService.compressWithConfig(imageFile, "mobile_config");
```

## 7. 后端REST API集成示例

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import id.zelory.compressor.service.ImageCompressionService;
import id.zelory.compressor.service.BatchImageCompressor;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    
    private final ImageCompressionService compressionService;
    
    public ImageController(ImageCompressionService compressionService) {
        this.compressionService = compressionService;
    }
    
    /**
     * 单张图片压缩
     */
    @PostMapping("/compress")
    public ResponseEntity<?> compressImage(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "quality", defaultValue = "80") int quality) {
        try {
            File tempFile = new File(file.getOriginalFilename());
            file.transferTo(tempFile);
            
            File compressedFile = compressionService.compressWithQuality(tempFile, quality);
            
            return ResponseEntity.ok(new CompressResponse(true, compressedFile.getAbsolutePath()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(new CompressResponse(false, e.getMessage()));
        }
    }
    
    /**
     * 获取图片信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getImageInfo(@RequestParam("path") String filePath) {
        File imageFile = new File(filePath);
        ImageCompressionService.ImageInfo imageInfo = compressionService.getImageInfo(imageFile);
        return ResponseEntity.ok(imageInfo);
    }
}
```

## 8. 约束说明

### DefaultConstraint
- 默认分辨率: 612x816
- 默认格式: JPEG
- 默认质量: 80

### ResolutionConstraint
- 按指定分辨率压缩
- 保持宽高比

### QualityConstraint
- 按质量等级压缩
- 范围: 0-100

### SizeConstraint
- 按最大文件大小压缩
- 自动迭代降低质量

### FormatConstraint
- 转换图片格式
- 支持: JPEG, PNG, WEBP

### DestinationConstraint
- 指定输出位置
- 自动创建目录

## 9. 性能���议

1. **使用异步处理**: 大文件或批量处理时使用异步方法
2. **配置复用**: 相同配置使用保存的配置对象
3. **内存管理**: 及时删除原始文件或临时文件
4. **线程池**: 使用线程池处理批量任务而非创建新线程

## 10. 常见问题

**Q: 如何处理HEIC格式？**
A: 使用FormatConstraint转换为JPEG或PNG

**Q: 如何获取压缩前后的文件大小对比？**
A: 使用ImageInfo获取原始大小，压缩后调用File.length()获取新大小

**Q: 支持什么操作系统？**
A: Java 8+，任何支持Java的操作系统
