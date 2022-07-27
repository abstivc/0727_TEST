package com.song.lad.sfeature.ocr;

import org.springframework.web.multipart.MultipartFile;

public class ImageParam {
    private MultipartFile[] img;

    public MultipartFile[] getImg() {
        return img;
    }

    public void setImg(MultipartFile[] img) {
        this.img = img;
    }
}
