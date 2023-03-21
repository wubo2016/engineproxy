package com.wyuansmart.phone.engine.communication.http.chat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyuansmart.phone.common.core.chat.ChatItem;
import com.wyuansmart.phone.common.core.chat.ChatRequest;
import com.wyuansmart.phone.engine.communication.http.BaseClientHttp;
import com.wyuansmart.phone.engine.communication.http.chat.vo.ChatResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChatGptHttp extends BaseClientHttp {

    private static Logger logger = LoggerFactory.getLogger(ChatGptHttp.class);

    public ChatResponse chat(ChatRequest chatRequest){
        ChatResponse chatResponse = null;
        String path = getServiceAddress() + "/wy/openai/chat/completion";
        JSONObject jsonObject = toChatGptJson(chatRequest);
        if(jsonObject == null){
            return chatResponse;
        }
        String requestbody = jsonObject.toString();
        HttpPost httppost = new HttpPost(path);
        httppost.addHeader("Content-Type","application/json;charset=UTF-8");
        httppost.addHeader("Authorization",getToken());
        StringEntity se = new StringEntity(requestbody,"utf-8");
        se.setContentType("text/json");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
        httppost.setEntity(se);

        String result = "";
        HttpResponse httpResponse;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = (CloseableHttpClient)getHttpClient();
            httpResponse = httpClient.execute(httppost);
            //获取服务器端返回的状态码和输入流，将输入流转换成字符串
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                result= changeInputStream(inputStream, "utf-8");
            } else {
                logger.error("chat api error" + httpResponse.toString());
                return chatResponse;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("txt2img api IOException",e);
            return chatResponse;
        }finally {
            if(null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("chat IOException",e);
                }
            }
        }

        JSONObject jsonObjectRep = JSONObject.parseObject(result);
        chatResponse = JSONObject.toJavaObject(jsonObjectRep, ChatResponse.class);
        return chatResponse;
    }

    private JSONObject toChatGptJson(ChatRequest chatRequest){
        ArrayList<ChatItem> chatItems = chatRequest.getChatItems();
        if (chatItems == null || chatItems.size() <= 0){
            logger.warn("输入的对话内容为空" + chatRequest.toString());
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        String user = chatRequest.getUser();
        if (StringUtils.isEmpty(user)){
            user = "test";
        }
        jsonObject.put("user", user);
        jsonObject.put("maxTokens", chatRequest.getMaxTokens());
        jsonObject.put("aiKey",getKey());

        JSONArray jsonArray = new JSONArray();
        int n = 0;
        for (int i = 0; i < chatItems.size(); i++){
            ChatItem chatItem = chatItems.get(i);
            if (chatItem == null){
                continue;
            }

            JSONObject itemJson = new JSONObject();
            itemJson.put("user",chatItem.getRole());
            itemJson.put("content",chatItem.getContent());
            jsonArray.add(n++,itemJson);
        }
        jsonObject.put("itemList",jsonArray);
        return jsonObject;
    }


}
