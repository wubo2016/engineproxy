package com.wyuansmart.phone.engine.service.ai.novelai;

import com.wyuansmart.phone.common.core.entity.Txt2ImageBase;
import com.wyuansmart.phone.engine.communication.http.diffusion.vo.ProgressResponse;
import com.wyuansmart.phone.engine.manager.diffusion.StableDiffusionManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StableDiffusionService {

    @Autowired
    StableDiffusionManger stableDiffusionManger;

    public String txt2Image(Txt2ImageBase txt2Image){
        return stableDiffusionManger.txt2Image(txt2Image);
    }

    public ProgressResponse queryTaskProgress(String taskId){
        return stableDiffusionManger.queryTaskProgress(taskId);
    }
}
