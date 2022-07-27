package com.song.lad.sfeature.qr;


import com.song.lad.sfeature.config.FeatureConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/lad/qr")
public class QrController {

    @Autowired
    FeatureConfig config;

    @PostMapping("/get")
    @ResponseBody
    public ResponseEntity<byte[]> qrWrite(@RequestParam("content") String content, HttpServletResponse res) throws Exception {
        File file = ZXingUtil.encodeImg(config.qrImgPath, "png", content, 690, 690, null);
        //设置响应头
        String fileName = file.getName();
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        fis = new FileInputStream(file);
        byte[] b = new byte[1000];
        int n;
        while ((n = fis.read(b)) != -1) {
            bos.write(b, 0, n);
        }
        byte[] data = bos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8),"ISO-8859-1"));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
