package SEP.SEP.Chat;

import java.util.List;

public class GroupChatCreationDto {
    private List<Long> ids;
    private String chessClubName;

    // Getters and setters
    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getChessClubName() {
        return chessClubName;
    }

    public void setChessClubName(String chessClubName) {
        this.chessClubName = chessClubName;
    }
}

