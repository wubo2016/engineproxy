package com.wyuansmart.phone.engine.controller;

import com.wyuansmart.phone.common.base.dto.JsonObject;
import com.wyuansmart.phone.common.exception.ApplicationException;
import com.wyuansmart.phone.engine.manager.chat.ChatGptManager;
import com.wyuansmart.phone.engine.manager.chat.ChatGptStatus;
import com.wyuansmart.phone.engine.vo.chat.ChatVoRequest;
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
@RequestMapping("/engine/proxy/ai/chat")
public class ChatGptController {

    @Autowired
    private ChatGptManager chatGptManager;

    @PostMapping("/chat")
    @Operation(summary = "chatGPT对话")
    public JsonObject<String> chatGpt(HttpServletRequest request, @RequestBody ChatVoRequest chatRequest ) {
        JsonObject<String> jsonObject = new JsonObject<>(null);
        try {
            String text  = chatGptManager.addChatTask(chatRequest.toChatRequest());
            jsonObject.setData(text);
        }catch (ApplicationException e){
            jsonObject.setResultCode(Integer.valueOf(e.getCode()));
            jsonObject.setResultMsg(e.getMessage());
        }catch (Exception e){
            jsonObject.setResultMsg(e.getMessage());
        }
        return jsonObject;
    }

    @GetMapping("/progress/{id}")
    @Operation(summary = "查询chatgpt进度")
    public JsonObject<ChatGptStatus> taskProgress(HttpServletRequest request, @PathVariable String id) {

        JsonObject<ChatGptStatus> jsonObject = new JsonObject<>(null);

        try {
            ChatGptStatus progressResponse = chatGptManager.queryStatus(id);
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
