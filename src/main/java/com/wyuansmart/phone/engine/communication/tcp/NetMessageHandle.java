package com.wyuansmart.phone.engine.communication.tcp;

import com.wyuansmart.phone.common.client.AppNodeClient;
import com.wyuansmart.phone.common.core.chat.ChatItem;
import com.wyuansmart.phone.common.core.chat.ChatRequest;
import com.wyuansmart.phone.common.core.entity.Image2Image;
import com.wyuansmart.phone.common.core.entity.Txt2Image;
import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.common.core.entity.cloud.AuditingResponse;
import com.wyuansmart.phone.common.enums.EChatUserRole;
import com.wyuansmart.phone.common.enums.EControlNetType;
import com.wyuansmart.phone.common.protobuf.*;
import com.wyuansmart.phone.common.server.protobuf.NetMsgContext;
import com.wyuansmart.phone.common.server.protobuf.NetMsgService;
import com.wyuansmart.phone.common.server.protobuf.ProtobufMessage;
import com.wyuansmart.phone.common.util.CommonObj;
import com.wyuansmart.phone.engine.common.ApplicationException;
import com.wyuansmart.phone.engine.communication.http.chat.vo.ChatResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.Txt2imgResponse;
import com.wyuansmart.phone.engine.manager.chat.ChatGptManager;
import com.wyuansmart.phone.engine.manager.chat.ChatGptStatus;
import com.wyuansmart.phone.engine.manager.diffusion.StableDiffusionManger;
import com.wyuansmart.phone.engine.service.ai.AuditingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
public class NetMessageHandle {
    private static Logger logger = LoggerFactory.getLogger(NetMessageHandle.class);

    @Autowired
    AppNodeClient appNodeClient;

    @Autowired
    StableDiffusionManger stableDiffusionManger;

    @Autowired
    AuditingService auditingService;

    @Autowired
    ChatGptManager chatGptManager;

    @PostConstruct
    public void init() {
        appNodeClient.registerMessageHandle(this);
    }

    /**
     * 处理文本绘画任务
     * @param ctx
     * @param message
     * @throws Exception
     */
    @NetMsgService(id= MessageTypeProto.MessageType.IMessageProtocolType.MSG_TXT2IMG_TASK_REQ_VALUE, syncOnSession=false)
    public void txt2Image(NetMsgContext ctx, ProtobufMessage message)throws Exception{
        logger.debug("txt2Image, SourceAddress:"+ message.getSourceAddress()+ "TargetAddress:"+message.getTargetAddress()+ ",from: "+ctx.getSession().toString());
        try {
            Txt2imgTaskProto.Txt2imgTask txt2imgTask = Txt2imgTaskProto.Txt2imgTask.parseFrom(message.getProtobuf());
            Txt2ImageBase base = null;
            boolean isImg2Img = false;
            boolean havaImageControlNet = false;
            byte[] initImage = null;
            byte[] netImage = null;
            if(txt2imgTask.getIsImg2Img()){
                if(txt2imgTask.getInitImage() != null
                        && txt2imgTask.getInitImage().size() > 64){
                    isImg2Img = true;
                }else if(!StringUtils.isEmpty(txt2imgTask.getInitImageUrlKey())){
                    //从本地缓存获取
                    initImage = stableDiffusionManger.getUrlImageCacheData(txt2imgTask.getInitImageUrlKey());
                    if(initImage != null && initImage.length > 64){
                        isImg2Img = true;
                    }
                }
            }
            int controlNetType = txt2imgTask.getImageControlNetType();
            if (controlNetType > EControlNetType.not_enable.getValue()){
                if(txt2imgTask.getControlNetImage() != null
                        && txt2imgTask.getControlNetImage().size() > 64){
                    havaImageControlNet = true;
                }else if(!StringUtils.isEmpty(txt2imgTask.getControlNetImageUrlKey())){
                    //从本地缓存获取
                    netImage = stableDiffusionManger.getUrlImageCacheData(txt2imgTask.getControlNetImageUrlKey());
                    if(netImage != null && netImage.length > 64){
                        havaImageControlNet = true;
                    }
                }

                if (!havaImageControlNet &&  !isImg2Img){
                    controlNetType = -1;
                }
            }

            if (isImg2Img){
                base = Image2Image.toImage2Image(txt2imgTask);
                if ( initImage != null){
                    ((Image2Image)base).setInitImage(initImage);
                }
            }else{
                base = Txt2Image.toTxt2Image(txt2imgTask);
            }

            base.setImageControlNetType(controlNetType);
            if(netImage != null){
                base.setControlNetImage(netImage);
            }
            String taskId = stableDiffusionManger.txt2Image(base);

            if (StringUtils.isEmpty(taskId)){
                ctx.sendResponse(-1,"没有配置绘画引擎");
                return;
            }
            Txt2imgTaskProto.Txt2imgTaskResponse.Builder builder = Txt2imgTaskProto.Txt2imgTaskResponse.newBuilder();
            builder.setTaskId(CommonObj.getValidString(taskId));
            builder.setUserTag(CommonObj.getValidString(base.getUserTag()));
            builder.setCapacity(stableDiffusionManger.getCapacity());
            builder.setTaskQueueSize(stableDiffusionManger.getTaskQueueSize());
            ctx.setResponseProtobuf(builder.build().toByteString());
            ctx.sendResponse(0,"");
        }catch (Exception ex){
            logger.error("updateNode exception:" ,ex);
        }
    }

    /**
     * 查询绘画进度
     * @param ctx
     * @param message
     * @throws Exception
     */
    @NetMsgService(id= MessageTypeProto.MessageType.IMessageProtocolType.MSG_GET_TASK_PROGRESS_VALUE, syncOnSession=false)
    public void taskProgress(NetMsgContext ctx, ProtobufMessage message)throws Exception{
        logger.debug("taskProgress, SourceAddress:"+ message.getSourceAddress()+ "TargetAddress:"+message.getTargetAddress()+ ",from: "+ctx.getSession().toString());
        try {
            TaskProgressProto.TaskProgress taskProgress = TaskProgressProto.TaskProgress.parseFrom(message.getProtobuf());
            ProgressResponse response = stableDiffusionManger.queryTaskProgress(taskProgress.getTaskId());
            if(response == null){
                ctx.sendResponse(-1,"没有找到绘画任务");
                return;
            }
            TaskProgressProto.TaskProgressResponse.Builder builder = TaskProgressProto.TaskProgressResponse.newBuilder();
            builder.setTaskId(CommonObj.getValidString(taskProgress.getTaskId()));
            builder.setProgress(CommonObj.getValidFloat(response.getProgress()));
            builder.setActive(response.isActive());
            builder.setCompleted(response.isCompleted());
            builder.setCapacity(stableDiffusionManger.getCapacity());
            builder.setTaskQueueSize(stableDiffusionManger.getTaskQueueSize());
            builder.setEta(CommonObj.getValidFloat(response.getEta()));
            builder.setAestheticScore(CommonObj.getValidFloat(response.getAestheticScore()));
            builder.setTextInfo(CommonObj.getValidString(response.getTextinfo()));
            if (response.isQueued()){
                builder.setQueued(CommonObj.getValidInteger(response.getQueueIndex()));
            }else {
                builder.setQueued(0);
            }
            if (response instanceof Txt2imgResponse){
                Txt2imgResponse txt2imgResponse = (Txt2imgResponse) response;
                builder.setImageUrl(CommonObj.getValidString(txt2imgResponse.getImageUrl()));
                builder.setInfo(txt2imgResponse.getInfo());
                ArrayList<String> arrayList = txt2imgResponse.getImageUrls();
                for (String url : arrayList){
                    builder.addUrls(url);
                }
                AuditingResponse auditingResponse = txt2imgResponse.getAuditingResponse();
                if (auditingResponse != null){
                    ImageAuditingProto.ImageAuditingResponse.Builder builder1 = ImageAuditingProto.ImageAuditingResponse.newBuilder();
                    builder1.setResult(auditingResponse.getResult());
                    builder1.setLabel(auditingResponse.getLabel());
                    builder1.setSubLabel(auditingResponse.getSubLabel());
                    builder1.setScore(auditingResponse.getScore());
                    builder1.setResponse(auditingResponse.getResponse());
                    builder.setAuditingResponse(builder1);
                }
            }
            ctx.setResponseProtobuf(builder.build().toByteString());
            ctx.sendResponse(0,"");
        }catch (Exception ex){
            logger.error("updateNode exception:" ,ex);
        }
    }

    /**
     * 图片审核
     * @param ctx
     * @param message
     * @throws Exception
     */
    @NetMsgService(id= MessageTypeProto.MessageType.IMessageProtocolType.MSG_IMAGE_AUDITING_REQ_VALUE, syncOnSession=false)
    public void imageAuditing(NetMsgContext ctx, ProtobufMessage message)throws Exception{
        logger.debug("auditingImage, SourceAddress:"+ message.getSourceAddress()+ "TargetAddress:"+message.getTargetAddress()+ ",from: "+ctx.getSession().toString());
        try {
            ImageAuditingProto.ImageAuditing imageAuditing = ImageAuditingProto.ImageAuditing.parseFrom(message.getProtobuf());
            AuditingResponse auditingResponse = auditingService.imageAuditing(imageAuditing.getImageUrl());
            if (auditingResponse != null){
                ImageAuditingProto.ImageAuditingResponse.Builder builder1 = ImageAuditingProto.ImageAuditingResponse.newBuilder();
                builder1.setResult(auditingResponse.getResult());
                builder1.setLabel(auditingResponse.getLabel());
                builder1.setSubLabel(auditingResponse.getSubLabel());
                builder1.setScore(auditingResponse.getScore());
                builder1.setResponse(auditingResponse.getResponse());
                ctx.setResponseProtobuf(builder1.build().toByteString());
                ctx.sendResponse(0,"");
            }else {
                ctx.sendResponse(-1,"图片审核异常");
            }
        }catch (Exception ex){
            logger.error("updateNode exception:" ,ex);
            ctx.sendResponse(-1,ex.getMessage());
        }
    }

    /**
     * 请求AI会话
     * @param ctx
     * @param message
     * @throws Exception
     */
    @NetMsgService(id= MessageTypeProto.MessageType.IMessageProtocolType.MSG_CHAT_TASK_REQ_VALUE, syncOnSession=false)
    public void chatGptTask(NetMsgContext ctx, ProtobufMessage message)throws Exception{
        logger.debug("chatGptTask, SourceAddress:"+ message.getSourceAddress()+ "TargetAddress:"+message.getTargetAddress()+ ",from: "+ctx.getSession().toString());
        try {
            ChatGptTaskProto.ChatGptTask chatGptTask = ChatGptTaskProto.ChatGptTask.parseFrom(message.getProtobuf());
            ChatRequest request = toChatRequest(chatGptTask);
            String id = chatGptManager.addChatTask(request);
            ChatGptTaskProto.ChatGptTaskResponse.Builder response = ChatGptTaskProto.ChatGptTaskResponse.newBuilder();
            response.setTaskId(CommonObj.getValidString(id));
            response.setCapacity(chatGptManager.getCapacity());
            response.setTaskQueueSize(chatGptManager.getTaskQueueSize());
            response.setCode(200);
            ctx.setResponseProtobuf(response.build().toByteString());
            ctx.sendResponse(0,"");
        }catch (ApplicationException e){
            ChatGptTaskProto.ChatGptTaskResponse.Builder response = ChatGptTaskProto.ChatGptTaskResponse.newBuilder();
            response.setCode(e.getCode());
            response.setMessage(CommonObj.getValidString(e.getMessage()));
            response.setCapacity(chatGptManager.getCapacity());
            response.setTaskQueueSize(chatGptManager.getTaskQueueSize());
            ctx.setResponseProtobuf(response.build().toByteString());
            ctx.sendResponse(0,"");
        } catch (Exception ex){
            logger.error("updateNode exception:" ,ex);
            ctx.sendResponse(-1,ex.getMessage());
        }
    }

    /**
     * proto 转ChatRequest
     * @param chatGptTask
     * @return
     */
    private ChatRequest toChatRequest(ChatGptTaskProto.ChatGptTask chatGptTask){
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setUser(chatRequest.getUser());
        chatRequest.setMaxTokens(chatRequest.getMaxTokens());
        ArrayList<ChatItem> list = new ArrayList<>();
        for (int i = 0; i < chatGptTask.getChatItemsCount(); i++){
            ChatItem chatItem = new ChatItem();
            ChatGptTaskProto.ChatItem item = chatGptTask.getChatItems(i);
            chatItem.setUserRole(EChatUserRole.getUserRole(item.getRole()));
            chatItem.setContent(item.getContent());
            list.add(chatItem);
        }
        chatRequest.setChatItems(list);
        return chatRequest;
    }

    /**
     * 查询chatGPT对话进度
     * @param ctx
     * @param message
     * @throws Exception
     */
    @NetMsgService(id= MessageTypeProto.MessageType.IMessageProtocolType.MSG_GET_CHAT_PROGRESS_VALUE, syncOnSession=false)
    public void chatGPTProgress(NetMsgContext ctx, ProtobufMessage message)throws Exception{
        logger.debug("chatGPTProgress, SourceAddress:"+ message.getSourceAddress()+ "TargetAddress:"+message.getTargetAddress()+ ",from: "+ctx.getSession().toString());
        try {
            ChatGptTaskProgressProto.ChatGptTaskProgress taskProgress = ChatGptTaskProgressProto.ChatGptTaskProgress.parseFrom(message.getProtobuf());
            ChatGptStatus status = chatGptManager.queryStatus(taskProgress.getTaskId());
            if(status == null){
                ctx.sendResponse(-1,"没有找到会话任务");
                return;
            }

            ChatGptTaskProgressProto.ChatProgressResponse.Builder builder = ChatGptTaskProgressProto.ChatProgressResponse.newBuilder();
            builder.setTaskId(CommonObj.getValidString(taskProgress.getTaskId()));
            builder.setProgress(CommonObj.getValidFloat(status.getProgress()));
            builder.setQueued(0);
            if (status.getStatus() == ChatGptStatus.RUN_STATUS){
                builder.setActive(true);
                builder.setCompleted(false);
            }else if (status.getStatus() == ChatGptStatus.END_STATUS){
                builder.setActive(false);
                builder.setCompleted(true);
                ChatResponse response = status.getChatResponse();
                if(response != null){
                    builder.setCode(CommonObj.getValidInteger(response.getCode()));
                    builder.setMessage(CommonObj.getValidString(response.getMessage()));
                    builder.setContent(CommonObj.getValidString(response.getData()));
                }
            } else {
                builder.setActive(false);
                builder.setCompleted(false);
                builder.setQueued(CommonObj.getValidInteger(status.getQueueIndex()));
            }
            ctx.setResponseProtobuf(builder.build().toByteString());
            ctx.sendResponse(0,"");
        }catch (Exception ex){
            logger.error("updateNode exception:" ,ex);
            ctx.sendResponse(-1,ex.getMessage());
        }
    }

}
