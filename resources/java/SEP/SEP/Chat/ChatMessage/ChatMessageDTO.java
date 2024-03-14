package SEP.SEP.Chat.ChatMessage;

public class ChatMessageDTO {
    private Long userID;
    private String messageContent;
    private String username;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(Long userID, String messageContent, String username) {
        this.userID = userID;
        this.messageContent = messageContent;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}