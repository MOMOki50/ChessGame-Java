package SEP.SEP.Chat;

import SEP.SEP.Chat.ChatMessage.ChatMessage;
import SEP.SEP.Chat.ChatMessage.ChatMessageDTO;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    public ChatRepository chatRepository;
    @Autowired
    public ChatService chatService;
    @Autowired
    public UserRepository userRepository;

    @MessageMapping("/chat/sendMessage/{chatId}")
    @SendTo("/topic/chat/messages/{chatId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long chatId, ChatMessageDTO message) {
        // Speichert die Nachricht mit dem Benutzer und der Chat-ID
        User user = userRepository.findById(message.getUserID()).get();
        return chatService.saveMessage(chatId, user, message);
    }

    @GetMapping(value = "/Chats")
    public String getChats(HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        User user = userRepository.findByEmail(loggedInEmail);
        List<Chat> chats = chatService.getUserChats(user);
        List<User> alleUser = userRepository.findAll();
        alleUser.remove(user);
        model.addAttribute("allChats", chats);
        model.addAttribute("currentUser", user);
        model.addAttribute("allUser", alleUser);
        return "AllChats";
    }

    @GetMapping(value = "/Chat/{chatId}")
    public String getChat(@PathVariable Long chatId,HttpSession session, Model model) {
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if(loggedInEmail == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(loggedInEmail);
        if(chatRepository.existsById(chatId)) {
            if(chatService.userVerified(user.getId(),chatId)) {
                model.addAttribute("currentUser", user);
                model.addAttribute("chat", chatRepository.findById(chatId).get());
                return "Chat";
            } else {
                return "redirect:/profile";
            }
        } else {
            return "redirect:/profile";
        }
    }
    @PostMapping(value = "/Chat/joinChat/{chatId}")
    public String joinGroupChat(@PathVariable Long chatId, HttpSession session){
        String loggedInEmail = (String) session.getAttribute("loggedInEmail");
        if(loggedInEmail == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(loggedInEmail);
        if(chatService.addParticipant(chatId, user)){
            return "redirect:/Chat/" + chatId;
        } else {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/Chat/create/{userId}")
    public String createChat(@PathVariable Long userId, HttpSession session) {
        String email = (String) session.getAttribute("loggedInEmail");
        User user1 = userRepository.findByEmail(email);
        User user2 = userRepository.findById(userId).get();
        try {
            chatService.createPrivateChat(user1,user2);
            return "redirect:/Chats";
        } catch (Exception e) {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/Chat/createGroupChat")
    public String createGroupChat(@RequestBody List<Long> ids,HttpSession session) {
        String email = (String) session.getAttribute("loggedInEmail");
        User user = userRepository.findByEmail(email);
        List<User> ChatParticipants = userRepository.findAllById(ids);
        ChatParticipants.add(user);
        try {
            chatService.createGroupChat(ChatParticipants);
            return "redirect:/Chats";
        } catch (Exception e) {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/Chat/createClubChat")
    public String createGroupChat(@RequestBody GroupChatCreationDto creationDto, HttpSession session) {
        String email = (String) session.getAttribute("loggedInEmail");
        User user = userRepository.findByEmail(email);
        List<User> ChatParticipants = userRepository.findAllById(creationDto.getIds());
        ChatParticipants.add(user);
        try {
            chatService.createClubChat(ChatParticipants, creationDto.getChessClubName());
            return "redirect:/Chats";
        } catch (Exception e) {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/Chat/delete/{chatId}")
    public String deleteChat(@PathVariable Long chatId, HttpSession session){
        Chat chat = chatRepository.findById(chatId).get();

        if(chat == null) {
            return "redirect:/profile";
        }

        if(chatService.deleteChat(chat)) {
            return "redirect:/Chats";
        } else {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/Chat/leave/{chatId}")
    public String leaveGroupChat(@PathVariable Long chatId, HttpSession session){
        Chat chat = chatRepository.findById(chatId).get();
        String email = (String) session.getAttribute("loggedInEmail");

        if (email == null){
            return "rediredt:/login";
        }

        User user = userRepository.findByEmail(email);

        if(chat == null) {
            return "redirect:/profile";
        }

        if(chatService.leaveChat(chat, user)) {
            return "redirect:/Chats";
        } else {
            return "redirect:/profile";
        }
    }

    @PostMapping(value = "/markAsRead/{chatId}/{messageId}") //mapping um einzelne Nachricht als gelesen zu markieren
    public String markAsRead(@PathVariable Long messageId, @PathVariable Long chatId) {
        chatService.setRead(messageId);
        return "redirect:/Chat/{chatId}";
    }



    @PostMapping(value = "/Chat/{chatId}/markChatAsRead") //mapping, um  Chat als gelesen zu markieren
    public String markChatAsRead(@RequestParam Long currentUserId, @PathVariable Long chatId) {
        chatService.setChatRead(chatId, currentUserId);
        return "redirect:/Chat/{chatId}";
    }

   @PostMapping(value = "/editMessage/{chatId}/{messageId}")
   public ResponseEntity<String> editMessage(@PathVariable Long messageId, @PathVariable Long chatId, @RequestParam String newContent) {
       boolean isEdited = chatService.editMessage(messageId, chatId, newContent);
       if (isEdited) {
           return ResponseEntity.ok("true");
       } else {
           return ResponseEntity.ok("false");
       }
   }
    @PostMapping(value = "/deleteMessage/{chatId}/{messageId}") //mapping für Nachricht entfernen
    public String deleteMessage(@PathVariable Long messageId, @PathVariable Long chatId) {
        System.out.println("accessed deleteMessage with message Id:" + messageId);
        chatService.deleteMessage(messageId, chatId);
        return "redirect:/Chat/{chatId}";
    }

}



