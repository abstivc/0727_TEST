package com.song.lad.sfeature.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration("myFeConfig")
public class FeatureConfig {

    @Value("${ocr.host}")
    public String ocrHost;

    @Value("${ocr.api.key}")
    public String ocrAk;

    @Value("${ocr.secret.key}")
    public String ocrSk;

    @Value("${ocr.img.host}")
    public String ocrImgHost;

    @Value("${qr.img.path}")
    public String qrImgPath;
}
