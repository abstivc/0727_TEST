package com.song.lad.sfeature.qr;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ZXingUtil {
    /**
     * 生成二维码
     * @param imgPath 生成的二维码保存路径
     * @param format 二维码图片格式 例如: png
     * @param content 二维码内容
     * @param width 宽度 430
     * @param height 高度 430
     * @param logo logo图标路径
     * @throws Exception
     */
    public static File encodeImg(String imgPath,String format,String content,int width,int height,String logo) throws Exception {
        if (format == null || format.trim().length()==0){
            format = "png";
        }
        Hashtable<EncodeHintType,Object > hints = new Hashtable<EncodeHintType,Object>() ;
        //排错率  L<M<Q<H
        hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H) ;
        //编码
        hints.put( EncodeHintType.CHARACTER_SET, "utf-8") ;
        //外边距：margin
        hints.put( EncodeHintType.MARGIN, 1) ;
        /*
         * content : 需要加密的 文字
         * BarcodeFormat.QR_CODE:要解析的类型（二维码）
         * hints：加密涉及的一些参数：编码、排错率
         */
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE,width,height, hints);
        //内存中的一张图片：此时需要的图片 是二维码-> 需要一个boolean[][] ->BitMatrix
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int x=0;x<width;x++) {
            for(int y=0;y<height;y++) {
                image.setRGB(x, y,     (bitMatrix.get(x,y)? 0x000000:0xFFFFFF)  );
            }
        }
        //画logo
        if (logo!=null && logo.trim().length()>0){
            image = LogoUtil.logoMatrix(image, logo);
        }
        //string->file
        File file = new File(imgPath);
        //生成图片
        ImageIO.write(image, format, file);
        return file;
    }

    //解密：二维码->文字
    public static String decodeImg(File file) throws Exception {
        if(!file.exists()) return null;
        //file->内存中的一张图片
        BufferedImage imge = ImageIO.read(file);

        MultiFormatReader formatReader = new MultiFormatReader();
        LuminanceSource source = new BufferedImageLuminanceSource(imge);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
        //图片 ->result
        Map map = new HashMap();
        map.put(EncodeHintType.CHARACTER_SET, "utf-8");
        Result result = formatReader.decode(binaryBitmap  ,map);

        System.out.println("解析结果："+ result.toString());
        return result.toString();
    }

}
