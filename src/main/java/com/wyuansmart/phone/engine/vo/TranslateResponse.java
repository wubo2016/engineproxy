package com.wyuansmart.phone.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class TranslateResponse {
    @Schema(description = "翻译的内容")
    private String TargetText;
    @Schema(description = "翻译的目标言类型zh：简体中文 zh-TW：繁体中文 en：英语 ja：日语",defaultValue = "en")
    private String Target;

    public String getTargetText() {
        return TargetText;
    }

    public void setTargetText(String targetText) {
        TargetText = targetText;
    }

    public String getTarget() {
        return Target;
    }

    public void setTarget(String target) {
        Target = target;
    }
}
