package com.wyuansmart.phone.engine.vo;


import io.swagger.v3.oas.annotations.media.Schema;

public class TranslateRequest {
    @Schema(description = "翻译的内容，最大2000字符")
    private String SourceText;

    @Schema(description = "源语言类型 auto：自动识别 zh：简体中文 zh-TW：繁体中文 en：英语 ja：日语， ko：韩语",defaultValue = "auto")
    private String Source;
    @Schema(description = "翻译的目标言类型zh：简体中文 zh-TW：繁体中文 en：英语 ja：日语",defaultValue = "en")
    private String Target;

    public String getSourceText() {
        return SourceText;
    }

    public void setSourceText(String sourceText) {
        SourceText = sourceText;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getTarget() {
        return Target;
    }

    public void setTarget(String target) {
        Target = target;
    }
}
