package SEP.SEP.friends;

import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    public FriendService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User mit dieser ID nicht gefunden."));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new IllegalStateException("Freund mit dieser ID nicht gefunden."));
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
        userRepository.save(user);
        userRepository.save(friend);
    }
}
