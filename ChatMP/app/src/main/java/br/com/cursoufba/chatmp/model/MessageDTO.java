package br.com.cursoufba.chatmp.model;

/**
 * Created by Angelo on 15/10/2015.
 */
public class MessageDTO {

    private String type;

    private String userId;

    private String message;


    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

}