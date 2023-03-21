package com.wyuansmart.phone.engine.vo.chat;

import com.wyuansmart.phone.common.core.chat.ChatItem;
import com.wyuansmart.phone.common.core.chat.ChatRequest;
import com.wyuansmart.phone.common.enums.EChatUserRole;

import java.util.ArrayList;

public class ChatVoRequest {
    private ArrayList<ChatItemVo> chatItems;

    private int maxTokens = 1024;

    private String user;

    public ArrayList<ChatItemVo> getChatItems() {
        return chatItems;
    }

    public void setChatItems(ArrayList<ChatItemVo> chatItems) {
        this.chatItems = chatItems;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ChatRequest toChatRequest(){
        ChatRequest request = new ChatRequest();
        request.setUser(getUser());
        request.setMaxTokens(getMaxTokens());
        ArrayList<ChatItem> chatItemArrayList = new ArrayList<>();
        for (ChatItemVo itemVo : chatItems){
            ChatItem chatItem = new ChatItem();
            chatItem.setContent(itemVo.getContent());
            chatItem.setUserRole(EChatUserRole.getUserRole(itemVo.getRole()));
            chatItemArrayList.add(chatItem);
        }
        request.setChatItems(chatItemArrayList);
        return request;
    }
}
