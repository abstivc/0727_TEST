package com.song.lad.sfeature.ocr;

public class CommonResponse {

    public CommonResponse(String resCode, String resMsg, String code, Object data) {
        this.resCode = resCode;
        this.resMsg = resMsg;
        this.code = code;
        this.data = data;
    }

    private String resCode;
    private String resMsg;
    private String code;
    private Object data;

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
