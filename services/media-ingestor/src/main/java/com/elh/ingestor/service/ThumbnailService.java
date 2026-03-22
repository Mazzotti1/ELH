package com.elh.ingestor.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ThumbnailService {

    @Value("${media.thumbnail.width:320}")
    private int width;

    @Value("${media.thumbnail.height:320}")
    private int height;

    @Value("${media.thumbnail.quality:0.8}")
    private double quality;

    public byte[] generateThumbnail(byte[] imageData, String mimeType) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Thumbnails.of(new ByteArrayInputStream(imageData))
                    .size(width, height)
                    .outputQuality(quality)
                    .outputFormat(getOutputFormat(mimeType))
                    .toOutputStream(out);

            log.debug("Thumbnail gerada: {}x{} ({} bytes -> {} bytes)", width, height, imageData.length, out.size());
            return out.toByteArray();
        } catch (IOException e) {
            log.warn("Falha ao gerar thumbnail: {}", e.getMessage());
            return null;
        }
    }

    public boolean canGenerateThumbnail(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/") &&
                !mimeType.contains("svg") &&
                !mimeType.contains("gif");
    }

    private String getOutputFormat(String mimeType) {
        if (mimeType != null && mimeType.contains("png")) return "png";
        return "jpg";
    }
}
