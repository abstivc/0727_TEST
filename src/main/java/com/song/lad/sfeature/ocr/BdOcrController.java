package com.song.lad.sfeature.ocr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.song.lad.sfeature.config.FeatureConfig;
import com.song.lad.sfeature.util.Base64Util;
import com.song.lad.sfeature.util.HttpUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping("/lad/ocr")
public class BdOcrController {

    @Autowired
    @Qualifier("myFeConfig")
    FeatureConfig featureConfig;

    Map jvmMap = new HashMap();

    @PostMapping("/refresh")
    @ResponseBody
    public CommonResponse refreshAccessToken() {

        if (jvmMap.containsKey("access_token")) {
            Date expTime = (Date) jvmMap.get("expires_time");
            if (expTime ==null || expTime.after(new Date())) {
                return new CommonResponse("0000", "刷新成功", "", jvmMap.get("access_token").toString());
            }
        }
        // 尝试从 redis / 数据库获取 accessToken 和 有效期
        Map res = getAuth(featureConfig.ocrAk, featureConfig.ocrSk);
        if (res == null || !res.containsKey("access_token")) {
            return new CommonResponse("9999", "刷新异常", "", null);
        }

        jvmMap.put("access_token", res.get("access_token").toString());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, Integer.valueOf(res.get("expires_in").toString()));
        jvmMap.put("expires_time", calendar.getTime());
        res.put("expires_time", calendar.getTime());
        // 保存到数据库
        return new CommonResponse("0000", "刷新成功", "", res);
    }

    @PostMapping("/convert")
    @ResponseBody
    public CommonResponse convert(ImageParam imageReq) {
        if (imageReq == null || imageReq.getImg().length < 1) {
            return new CommonResponse("1001", "上传文件为空", "", "请上传图片");
        }
        if (!jvmMap.containsKey("access_token")) {
            refreshAccessToken();
        }
        MultipartFile [] files = imageReq.getImg();
        String at = jvmMap.get("access_token").toString();
        StringBuilder allContent = new StringBuilder();
        for (MultipartFile file : files) {
            try {
                String content = webImage(IOUtils.toByteArray(file.getInputStream()), at);
                // 格式转换
                allContent.append(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new CommonResponse("0000", "处理成功", "", allContent);
    }


    /* ------------------------------ 分割 ------------------------------------- */



    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Secret Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public Map getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = featureConfig.ocrHost;
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:" + result);
            Map res = JSON.parseObject(result, Map.class);
            return res;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }


    /**
     * 重要提示代码中所需工具类
     * FileUtil,Base64Util,HttpUtil,GsonUtils请从
     * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
     * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
     * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
     * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
     * 下载
     */
    public String webImage(byte[] imgData, String accessToken) {
        // 请求url
        String url = featureConfig.ocrImgHost;
        try {
            // 本地文件路径
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");
            String param = "image=" + imgParam;
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String result = HttpUtil.post(url, accessToken, param);
            StringBuilder rr = convertResult(result);
            return rr.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private StringBuilder convertResult(String result) {
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray words = (JSONArray)jsonObject.get("words_result");
        for (int i = 0; i < words.size(); i++) {
            JSONObject js = (JSONObject) words.get(i);
            String w = js.getString("words");
            sb.append(w);
            sb.append(System.lineSeparator());
        }
        sb.append("-------------------------------------分頁");
        return sb;
    }
}
