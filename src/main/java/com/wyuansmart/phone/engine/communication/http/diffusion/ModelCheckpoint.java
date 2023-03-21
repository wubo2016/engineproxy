package com.wyuansmart.phone.engine.communication.http.diffusion;

public class ModelCheckpoint {
    private String title;//": "v1-5-pruned-emaonly.ckpt [cc6cb27103]",
    private String modelName;//": "v1-5-pruned-emaonly",
    private String hash; //": "cc6cb27103",
    private String sha256;//": "cc6cb27103417325ff94f52b7a5d2dde45a7515b25c255d8e396c90014281516",
    private String filename;//": "/data1/novelai/stable-diffusion-webui/models/Stable-diffusion/v1-5-pruned-emaonly.ckpt",
    private String config; //": null

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "ModelCheckpoint{" +
                "title='" + title + '\'' +
                ", modelName='" + modelName + '\'' +
                ", hash='" + hash + '\'' +
                ", sha256='" + sha256 + '\'' +
                ", filename='" + filename + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
