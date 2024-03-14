package SEP.SEP.Chat;

import SEP.SEP.Chat.ChatMessage.ChatMessage;
import SEP.SEP.Chat.ChatMessage.ChatMessage;
import SEP.SEP.Chat.ChatMessage.ChatMessageDTO;
import SEP.SEP.Chat.ChatMessage.ChatMessageRepository;
import SEP.SEP.ChessClub.ChessClub;
import SEP.SEP.ChessClub.ChessClubRepository;
import SEP.SEP.user.User;
import SEP.SEP.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    public ChatRepository chatRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChessClubRepository chessClubRepository;


    public List<Chat> getUserChats(User user) {
        return chatRepository.findChatsByUser(user);
    }

    public boolean createPrivateChat(User user1, User user2){
        try {
            Chat newChat = new Chat();
            chatRepository.save(newChat);

            List<User> participants = new ArrayList<>();
            participants.add(user1);
            participants.add(user2);

            newChat.setParticipants(participants);

            user1.getChats().add(newChat);
            user2.getChats().add(newChat);

            userRepository.save(user1);
            userRepository.save(user2);

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public Chat createGroupChat(List<User> userList){
        try {
            Chat newChat = new Chat();
            for (User user: userList) {
                user.getChats().add(newChat);
                userRepository.save(user);
            }
            newChat.setParticipants(userList);
            chatRepository.save(newChat);
            return newChat;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public Chat createClubChat(List<User> userList, String chessClubName){
        try {
            Chat newChat = new Chat();
            if(chessClubName != null){
                newChat.setChessClubName(chessClubName);
            }
            System.out.println("ChessClubName : " + newChat.getChessClubName());
            ChessClub club = chessClubRepository.findChessClubByClubName(chessClubName);
            for (User user: userList) {
                user.getChats().add(newChat);
                userRepository.save(user);
            }
            newChat.setParticipants(userList);
            club.setChat(newChat);
            chessClubRepository.save(club);
            chatRepository.save(newChat);
            return newChat;
        } catch (NullPointerException e) {
            return null;
        }
    }
    public boolean deleteChat(Chat chat) {
        try {
            for (User user: chat.getParticipants()) {
                user.getChats().remove(chat);
                userRepository.save(user);
            }
            chatRepository.delete(chat);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean leaveChat(Chat chat, User user) {
        try {
            chat.getParticipants().remove(user);
            user.getChats().remove(chat);

            if(chat.getParticipants().size() <= 1) {
                chatRepository.delete(chat);
                return true;
            }
            ChatMessage message = new ChatMessage(user,chat, " hat die Gruppe verlassen! ");
            chat.getMessages().add(message);
            user.getMessages().add(message);
            chatRepository.save(chat);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addParticipant(Long chatId, User user){
        try {
            Chat chat = chatRepository.findById(chatId).get();

            if(chat.getParticipants().contains(user)) return true;

            chat.getParticipants().add(user);
            ChatMessage message = new ChatMessage(user,chat, " ist der Gruppe beigetreten!");
            chat.getMessages().add(message);
            user.getMessages().add(message);
            chatRepository.save(chat);
            userRepository.save(user);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public ChatMessageDTO saveMessage(Long chatId, User user, ChatMessageDTO messageDTO){
        try {
            Chat chat = chatRepository.findById(chatId).get();
            ChatMessage message = new ChatMessage(user, chat, messageDTO.getMessageContent());
            chatMessageRepository.save(message);
            chat.getMessages().add(message);
            chatRepository.save(chat);
            return messageDTO;
        } catch (NullPointerException e) {
            return null;
        }
    }
    public boolean userVerified(Long userId, Long chatId){
        User user = userRepository.findById(userId).orElse(null);
        Chat chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null) {
            return false;
        }
        return chat.getParticipants().contains(user);
    }

    public void setRead(Long messageId){
        System.out.println("accessed setRead with message Id:" + messageId);
        ChatMessage message = chatMessageRepository.findById(messageId).get();
        message.setRead(true);
        chatMessageRepository.save(message);
        System.out.println("set message " + messageId + " to read");
    }

    public void setChatRead(Long chatId, long currentUserId){
        Chat chat = chatRepository.findById(chatId).get();

        for (ChatMessage message : chat.getMessages()) {
            if (message.getSender().getId() != currentUserId) {
                message.setRead(true);
                chatMessageRepository.save(message);
                System.out.println("Set message " + message.getId() + " to read");
                System.out.println("message belongs to " + message.getSender().getId() + " not " + currentUserId);
            }
        }

        System.out.println("Set chat " + chatId + " to read");
    }

    public boolean deleteMessage(Long messageId, Long chatId){
        ChatMessage message = chatMessageRepository.findById(messageId).get();
        Chat chat = chatRepository.findById(chatId).get();
        User user = message.getSender();
        if(!message.isRead()){
            user.getMessages().remove(message);
            chat.getMessages().remove(message);
            chatMessageRepository.delete(message);
            chatRepository.save(chat);
            userRepository.save(user);
            System.out.println("deleted message " + messageId);
            return true;
        }
        else {
            System.out.println("cannot delete message " + messageId);
            return false;
        }
    }

    public boolean editMessage(Long messageId, Long chatId, String newContent){
        ChatMessage message = chatMessageRepository.findById(messageId).get();
        Chat chat = chatRepository.findById(chatId).get();
        User user = message.getSender();
        if(!message.isRead()) {
            message.setContent(newContent);
            chatRepository.save(chat);
            userRepository.save(user);
            chatMessageRepository.save(message);
            System.out.println("edited message " + messageId);
            return true;
        }
        else{
            System.out.println("cannot edit message " + messageId);
            return false;
        }
    }
}