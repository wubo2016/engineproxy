package com.wyuansmart.phone.engine.config;

import java.util.ArrayList;

public class ControlNetConfig {


    private boolean enable;

    private ArrayList<ControlNetModelConfig> models;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public ArrayList<ControlNetModelConfig> getModels() {
        return models;
    }

    public void setModels(ArrayList<ControlNetModelConfig> models) {
        this.models = models;
    }
}
