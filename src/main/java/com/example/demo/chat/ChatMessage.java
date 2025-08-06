package com.example.demo.chat;

public class ChatMessage {
    public String type;     // "public", "private", "join", "leave", "id", "nickname"
    public String from;
    public String to;
    public String message;
    public String nickname; 
    public String imageData; // Base64 para imágenes
    public String imageType; // "png", "jpg", etc.

  
	public ChatMessage() {}
	
    public ChatMessage(String type, String from, String to, String message, String nickname) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.message = message;
        this.nickname = nickname;
    }
    
    public String getNickname() {
  		return nickname;
  	}
  	public void setNickname(String nickname) {
  		this.nickname = nickname;
  	}
    
    
	@Override
	public String toString() {
		return "ChatMessage [type=" + type + ", from=" + from + ", to=" + to + ", message=" + message + ", nickname="
				+ nickname + "]";
	}
    
    
    
    
}