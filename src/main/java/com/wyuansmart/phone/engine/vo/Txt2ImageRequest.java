package com.wyuansmart.phone.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class Txt2ImageRequest {

    /**
     * 指定模型
     */
    @Schema(description = "指定绘画模块",defaultValue = "AsiaFacemix-pruned-fix")
    private String sdModelCheckpoint;

    /**
     * 绘画描述
     */
    @Schema(description = "绘画描述",defaultValue = "")
    private String  prompt;

    /**
     * 不出现的描述
     */
    @Schema(description = "不出现的描述",defaultValue = "EasyNegative, paintings, sketches, (worst quality:2), (low quality:2), (normal quality:2), lowres, normal quality, ((monochrome)), ((grayscale)), skin spots, acnes, skin blemishes, age spot, glans,extra fingers,fewer fingers,strange fingers,bad hand")
    private String negativePrompt;

    /**
     * 采样迭代步数
     */
    @Schema(description = "采样迭代步数",defaultValue = "20")
    private int steps = 20;

    /**
     * 采样方法
     * 见ESamplingMethod Euler a ：富有创造力
     * Euler：最最常见基础的算法，最简单的，也是最快的  LMS：eular的延伸算法，相对更稳定一点，30步就比较稳定了
     */
    @Schema(description = "采样方法",defaultValue = "Euler a")
    private String samplingMethod = "Euler a";

    /**
     * 是否进行面部修复
     */
    @Schema(description = "是否进行面部修复 ",defaultValue = "false")
    private boolean restoreFaces = false ;


    /**
     * 图像与你的提示的匹配程度 增加这个值将导致图像更接近你的提示,但过高会让图像色彩过于饱和
     */
    @Schema(description = "图像与你的提示的匹配程度",defaultValue = "7")
    private float cfgScale = 7;


    /**
     *  高度
     */
    @Schema(description = "图像高度",defaultValue = "512")
    private int height = 512;

    /**
     * 宽度
     */
    @Schema(description = "图像宽度",defaultValue = "512")
    private int width = 512;

    /**
     * 跟输入的图像差异度度
     */
    @Schema(description = "跟输入的图像差异度度",defaultValue = "0.5")
    private float denoisingStrength = 0.5f;

    /**
     * 输入的初始图像
     */
    @Schema(description = "输入的初始图像",defaultValue = "")
    private String base64Image;

    @Schema(description = "是否需要图片审核",defaultValue = "false")
    private boolean auditing;


    @Schema(description = "启用控制作画模型方式",defaultValue = "-1")
    private int controlNetType = -1;

    @Schema(description = "控制作画图片 base64信息",defaultValue = "")
    private String controlImageBase64;

    @Schema(description = "控制作画图片权重",defaultValue = "1")
    private float controlNetWeight = 1f;

    public String getSdModelCheckpoint() {
        return sdModelCheckpoint;
    }

    public void setSdModelCheckpoint(String sdModelCheckpoint) {
        this.sdModelCheckpoint = sdModelCheckpoint;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getNegativePrompt() {
        return negativePrompt;
    }

    public void setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getSamplingMethod() {
        return samplingMethod;
    }

    public void setSamplingMethod(String samplingMethod) {
        this.samplingMethod = samplingMethod;
    }

    public boolean isRestoreFaces() {
        return restoreFaces;
    }

    public void setRestoreFaces(boolean restoreFaces) {
        this.restoreFaces = restoreFaces;
    }

    public float getCfgScale() {
        return cfgScale;
    }

    public void setCfgScale(float cfgScale) {
        this.cfgScale = cfgScale;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getDenoisingStrength() {
        return denoisingStrength;
    }

    public void setDenoisingStrength(float denoisingStrength) {
        this.denoisingStrength = denoisingStrength;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public boolean isAuditing() {
        return auditing;
    }

    public void setAuditing(boolean auditing) {
        this.auditing = auditing;
    }

    public int getControlNetType() {
        return controlNetType;
    }

    public void setControlNetType(int controlNetType) {
        this.controlNetType = controlNetType;
    }

    public String getControlImageBase64() {
        return controlImageBase64;
    }

    public void setControlImageBase64(String controlImageBase64) {
        this.controlImageBase64 = controlImageBase64;
    }

    public float getControlNetWeight() {
        return controlNetWeight;
    }

    public void setControlNetWeight(float controlNetWeight) {
        this.controlNetWeight = controlNetWeight;
    }
}
