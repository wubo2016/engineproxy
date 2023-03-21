package com.wyuansmart.phone.engine.controller;

import com.wyuansmart.phone.common.base.dto.JsonObject;
import com.wyuansmart.phone.common.exception.ApplicationException;
import com.wyuansmart.phone.engine.service.ai.TranslateService;
import com.wyuansmart.phone.engine.vo.TranslateRequest;
import com.wyuansmart.phone.engine.vo.TranslateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 翻译
 */
@Tag(name = "翻译相关")
@RestController
@RequestMapping("/engine/proxy/ai/translate")
public class TranslateController {

    @Autowired
    private TranslateService translateService;

    @PostMapping("/text")
    @Operation(summary = "文本翻译")
    public JsonObject<TranslateResponse> textTranslate(HttpServletRequest request, @RequestBody TranslateRequest translateRequest ) {
        JsonObject<TranslateResponse> jsonObject = new JsonObject<>(null);
        try {
            String text  = translateService.textTranslate(translateRequest.getSource(),translateRequest.getSourceText(),translateRequest.getTarget());
            TranslateResponse response = new TranslateResponse();
            response.setTargetText(text);
            response.setTarget(translateRequest.getTarget());
            jsonObject.setData(response);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

}
