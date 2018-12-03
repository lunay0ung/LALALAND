package com.example.luna.lalaland.All.IdeaNote;

/**
 * Created by LUNA on 2018-10-25.
 */

/*
* 아이디어 노트에 들어갈 수 있는 아이템
* -제목, 내용과 노트의 타입(음성이 포함된 메모와 텍스트만 있는 메모에 따라 레이아웃이 다름), 음성파일의 이름
*
* */

public class NoteItem {



    String title, content;
    private int type;
    String fileName;
    Boolean isVoiceNote;


    //텍스트만
    public NoteItem(String title, String content, Boolean isVoiceNote)
    {
        this.title = title;
        this.content = content;
        this.isVoiceNote = isVoiceNote;

    }

    //텍스트+녹음파일
    public NoteItem(String fileName, String title, String content, Boolean isVoiceNote)
    {
        this.fileName = fileName;
        this.title = title;
        this.content = content;
        this.isVoiceNote = isVoiceNote;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getVoiceNote() {
        return isVoiceNote;
    }

    public void setisVoiceNote(Boolean voiceNote) {
        isVoiceNote = voiceNote;
    }
}
