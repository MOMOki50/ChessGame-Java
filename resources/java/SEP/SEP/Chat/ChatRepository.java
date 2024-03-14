package SEP.SEP.Chat;

import SEP.SEP.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat,Long> {
    @Query("SELECT c FROM Chat c WHERE :user MEMBER OF c.participants")
    List<Chat> findChatsByUser(@Param("user") User user);

}