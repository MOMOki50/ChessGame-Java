package SEP.SEP.Notifications;

import SEP.SEP.Email.Email;
import SEP.SEP.FinishedGame.FinishedGame;
import SEP.SEP.FinishedGame.FinishedGameRepository;
import SEP.SEP.game.Game;
import SEP.SEP.game.GameRepository;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class NotificationsController {
    @Autowired
    public NotificationsService notificationsService;
    @Autowired
    public NotificationsRepository notificationsRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public Email mail;
    @Autowired
    public GameRepository gameRepository;

    @Autowired
    public FinishedGameRepository finishedGameRepository;

    @PostMapping(value = "/notifications/{friendId}/friendRequest")
    public String addFriends(@PathVariable Long friendId, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        User requester = userRepository.findByEmail(loggedInEmail);
        User receiver = userRepository.getById(friendId);

        boolean alreadyRequested = receiver.getNotifications().stream()
                .anyMatch(notification ->"Freundschaftsanfrage".equals(notification.getType()) && notification.getRequester().equals(requester));

        if (!requester.getFriends().contains(receiver) && !alreadyRequested) {
            Notifications notification = new Notifications("Freundschaftsanfrage", requester , receiver );
            mail.FriendRequestMail(receiver.getEmail(), receiver.getFirstname() + ' ' + receiver.getLastname());
            receiver.getNotifications().add(notification);
            notificationsRepository.save(notification);
            userRepository.save(receiver);
        }
        return "redirect:/profile";
    }

    // Diese Funktion sendet eine Notification an den
    @GetMapping(value = "/notifications/{spielerSchwarz}/{partiename}/gameInvitation")
    public ResponseEntity<Boolean> gameInvitation(@PathVariable String spielerSchwarz,HttpSession session, @PathVariable String partiename) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        User requester = userRepository.findByEmail(loggedInEmail);
        User receiver = userRepository.getByEmail(spielerSchwarz);
        List<Game> gamename = gameRepository.findByPartiename(partiename);
        if(gamename.isEmpty()==false){
            return ResponseEntity.ok(false);
        }
        List<FinishedGame> finishedgamename = finishedGameRepository.findByPartiename(partiename);
        if(finishedgamename.isEmpty()==false){
            return ResponseEntity.ok(false);
        }
        if(receiver==null){
         return ResponseEntity.ok(false);
        }

        Notifications notification = new Notifications("Spielanfrage", requester, receiver);
        notificationsRepository.save(notification);
        receiver.getNotifications().add(notification);
        userRepository.save(receiver);

        return ResponseEntity.ok(true);
    }


    @PostMapping(value = "/notifications/{id}/reject")
    public String rejectFriend(@PathVariable Long id, HttpSession session){
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if(loggedInEmail == null){
            return "redirect:/login";
        }
        //bei Spieleinladung
        if(notificationsRepository.getById(id).getType().equals("Spielanfrage")) {
            Long friendId =notificationsRepository.getById(id).getRequester().getId();
            User requester = userRepository.getById(friendId);
            gameRepository.deleteAll(gameRepository.findBySpielerSchwarzAndPartieGestartetIsFalseAndSpielerWeiß(loggedInEmail,requester.getEmail()));
        }
        User currentUser = userRepository.findByEmail(loggedInEmail);
        Notifications notification = notificationsRepository.getById(id);

        currentUser.getNotifications().remove(notification);
        notificationsRepository.delete(notification);
        userRepository.save(currentUser);


        return "redirect:/notifications";
}


    @PostMapping(value = "/notifications/{id}/friendAccept")
    public String acceptRequest(@PathVariable Long id, HttpSession session) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");

        if (loggedInEmail == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findByEmail(loggedInEmail);
        User newFriend = userRepository.getById(id);

        List<Notifications> notifications = notificationsRepository.findByTypeAndRequesterAndReceiever("Freundschaftsanfrage", newFriend, currentUser);
        currentUser.getNotifications().removeAll(notifications);

        currentUser.getFriends().add(newFriend);
        newFriend.getFriends().add(currentUser);
        notificationsRepository.deleteAll(notifications);
        userRepository.save(currentUser);
        userRepository.save(newFriend);
        return "redirect:/notifications";
    }

    @PostMapping(value = "/notifications/{id}/gameAccept")
    public String acceptGameRequest(@PathVariable Long id, HttpSession session) {
        Long friendId = notificationsRepository.getById(id).getRequester().getId();

            String loggedInEmail = (String) session.getAttribute("loggedInEmail");

            if(loggedInEmail == null) {
                return "redirect:/login";
            }
            User receiever = userRepository.findByEmail(loggedInEmail);
            User requester = userRepository.getById(friendId);

            //NotificationsService.deleteSpielanfragenForUser(receiever);
            //NotificationsService.deleteSpielanfragenForUser(requester);
            //alle Anfragen von und an die beiden Spieler löschen
            List<Notifications> notificationsReceived=notificationsRepository.findByReceieverAndType(receiever,"Spielanfrage");

            List<Notifications> notificationsRequested=notificationsRepository.findByRequesterAndType(receiever,"Spielanfrage");

            //löschen aus PlayerNotification
            List<Notifications> eins =userRepository.getById(receiever.getId()).getNotifications();
            eins.removeAll(notificationsReceived);
            eins.removeAll(notificationsRequested);
            userRepository.getById(receiever.getId()).setNotifications(eins);


            List<Notifications> notificationsReceived2=notificationsRepository.findByReceieverAndType(requester,"Spielanfrage");

            List<Notifications> notificationsRequested2=notificationsRepository.findByRequesterAndType(requester,"Spielanfrage");


            List<Notifications> zwei =userRepository.getById(requester.getId()).getNotifications();
            zwei.removeAll(notificationsReceived2);
            zwei.removeAll(notificationsRequested2);
            userRepository.getById(requester.getId()).setNotifications(zwei);

            //löschen aus Notification
            notificationsRepository.deleteAll(notificationsReceived);
            notificationsRepository.deleteAll(notificationsRequested);
            notificationsRepository.deleteAll(notificationsReceived2);
            notificationsRepository.deleteAll(notificationsRequested2);



            Game game=gameRepository.findBySpielerWeißAndSpielerSchwarz(requester.getEmail(), loggedInEmail).get(0);
            game.setPartieGestartet(true); //spricht immer das erste Spiel an, im 2. Zyklus muss beendetes Spiel aus game gelöscht werden
            gameRepository.save(game);
            gameRepository.deleteAll(gameRepository.findBySpielerSchwarzAndPartieGestartetIsFalseAndSpielerWeiß(loggedInEmail,requester.getEmail()));

            return "redirect:/play";

        }
}