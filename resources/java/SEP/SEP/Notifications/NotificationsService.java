package SEP.SEP.Notifications;

import SEP.SEP.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@Service
public class NotificationsService {
    @Autowired
    public NotificationsRepository notificationsRepository;

    public Notifications getNotificationsData(Long id){
        Notifications notification= notificationsRepository.getById(id);
        return  notification;
    }

    public void deleteSpielanfragenForUser(User user){
        List<Notifications> notificationsReceived=notificationsRepository.findByReceieverAndType(user,"Spielanfrage");
        notificationsRepository.deleteAll(notificationsReceived);
        List<Notifications> notificationsRequested=notificationsRepository.findByRequesterAndType(user,"Spielanfrage");
        notificationsRepository.deleteAll(notificationsRequested);
    }

}
