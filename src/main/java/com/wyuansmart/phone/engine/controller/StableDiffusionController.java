package com.wyuansmart.phone.engine.controller;

import com.wyuansmart.phone.common.base.dto.JsonObject;
import com.wyuansmart.phone.common.core.entity.Image2Image;
import com.wyuansmart.phone.common.core.entity.Txt2Image;
import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.common.enums.EControlNetType;
import com.wyuansmart.phone.common.exception.ApplicationException;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.service.ai.AuditingService;
import com.wyuansmart.phone.engine.service.ai.novelai.StableDiffusionService;
import com.wyuansmart.phone.engine.util.ImageUtil;
import com.wyuansmart.phone.engine.vo.ImageAuditingRequest;
import com.wyuansmart.phone.engine.vo.Txt2ImageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 绘画相关
 */
@Tag(name = "绘画相关")
@RestController
@RequestMapping("/engine/proxy/ai/novelai")
public class StableDiffusionController {

    @Autowired
    private StableDiffusionService stableDiffusionService;

    @Autowired
    private AuditingService auditingService;

    @PostMapping("/txt2img")
    @Operation(summary = "文本生成绘画")
    public JsonObject<String> txt2img(HttpServletRequest request, @RequestBody Txt2ImageRequest txt2Image ) {
        JsonObject<String> jsonObject = new JsonObject<>(null);
        try {
            String remoteAddress =  getRemoteAddress(request);
            Txt2ImageBase base = toTxt2ImageBase(txt2Image);
            String id = stableDiffusionService.txt2Image(base);
            jsonObject.setData(id);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

    @PostMapping("/img2img")
    @Operation(summary = "文本加图像生成绘画")
    public JsonObject<String> img2img(HttpServletRequest request, @RequestBody Txt2ImageRequest txt2Image ) {
        JsonObject<String> jsonObject = new JsonObject<>(null);
        try {
            String remoteAddress =  getRemoteAddress(request);
            Txt2ImageBase base = toTxt2ImageBase(txt2Image);
            String id = stableDiffusionService.txt2Image(base);
            jsonObject.setData(id);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

    private Txt2ImageBase toTxt2ImageBase(Txt2ImageRequest request){
        Txt2ImageBase base = null;
        boolean haveImage = false;
        if(request.getBase64Image()!= null && request.getBase64Image().length() > 64) {
            byte[] data = ImageUtil.base64String2ByteFun(request.getBase64Image());
            if (data != null && data.length > 64) {
                haveImage = true;
                base = new Image2Image();
                ((Image2Image) base).setInitImage(data);
                ((Image2Image) base).setDenoisingStrength(request.getDenoisingStrength());
            }
        }

        if(base == null) {
            base = new Txt2Image();
        }
        base.setCfgScale(request.getCfgScale());
        base.setHeight(request.getHeight());
        base.setPrompt(request.getPrompt());
        base.setWidth(request.getWidth());
        base.setNegativePrompt(request.getNegativePrompt());
        base.setRestoreFaces(request.isRestoreFaces());
        base.setSamplingMethod(request.getSamplingMethod());
        base.setSdModelCheckpoint(request.getSdModelCheckpoint());
        base.setSteps(request.getSteps());
        base.setAuditing(request.isAuditing());
        base.setImageControlNetType(request.getControlNetType());
        boolean haveControlImage = false;
        if(request.getControlImageBase64()!= null && request.getControlImageBase64().length() > 64) {
            byte[] data = ImageUtil.base64String2ByteFun(request.getControlImageBase64());
            if (data != null && data.length > 64) {
                haveControlImage = true;
                base.setControlNetImage(data);
            }
        }
        base.setControlNetWeight(request.getControlNetWeight());
        if (!haveControlImage && !haveImage){
            base.setImageControlNetType(EControlNetType.not_enable.getValue());
        }
        return base;
    }

    private static String getRemoteAddress(HttpServletRequest request){
        String ipAddress;
        ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


    @GetMapping("/progress/task/{id}")
    @Operation(summary = "查询进度")
    public JsonObject<ProgressResponse> taskProgress(HttpServletRequest request, @PathVariable String id) {

        JsonObject<ProgressResponse> jsonObject = new JsonObject<>(null);

        try {
            String remoteAddress =  getRemoteAddress(request);
            ProgressResponse progressResponse = stableDiffusionService.queryTaskProgress(id);
            jsonObject.setData(progressResponse);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

    @PostMapping("/image/auditing")
    @Operation(summary = "图片审核")
    public JsonObject<AuditingResponse> imageAuditing(HttpServletRequest request, @RequestBody ImageAuditingRequest auditingRequest) {

        JsonObject<AuditingResponse> jsonObject = new JsonObject<>(null);

        try {
            String remoteAddress =  getRemoteAddress(request);
            AuditingResponse progressResponse = auditingService.imageAuditing(auditingRequest.getUrl());
            jsonObject.setData(progressResponse);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

}
