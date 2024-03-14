package SEP.SEP.Notifications;

import SEP.SEP.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

    List<Notifications> findByRequesterAndType(User user, String type);

    Notifications getById(Long id);

    List<Notifications> findByReceieverAndType(User user, String type);

    List<Notifications> findByTypeAndRequesterAndReceiever(String type, User requester, User receiver);

}
