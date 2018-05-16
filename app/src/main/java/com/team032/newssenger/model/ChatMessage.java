package com.team032.newssenger.model;

import com.google.firebase.database.ServerValue;

import java.io.File;

public class ChatMessage {

    private String id;          // DB에 저장할 ID
    private String name;        // 이름
    private String text;        // 메시지
    private Object timestamp;   // 시간

    //private String photoUrl;    // 프로필 사진 경로
    //private String imageUrl;    // 첨부 이미지 경로

    private String title, description, link, siteName;
    private File imageFile;

    public ChatMessage(){

    }

    //생성자 만들기
    public ChatMessage(String text, String name, Object timestamp) {
        this.text = text;
        this.name = name;
        //this.photoUrl = photoUrl;
        //this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    //getter and setter 생성
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    /* public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }  */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}