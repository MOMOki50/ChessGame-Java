package SEP.SEP.ComputerGame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




@Service
public class ComputerGameService {

    @Autowired
    private ComputerGameRepository computerGameRepository;

    public ComputerGame getComputerGame(String spieler){
        if(computerGameRepository.findBySpieler(spieler).isEmpty())
            return null;
        return computerGameRepository.findBySpieler(spieler).get(0);
    }
}
