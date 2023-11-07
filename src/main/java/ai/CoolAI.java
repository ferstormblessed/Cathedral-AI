package ai;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CoolAI implements Agent {
    @Override
    public String name() {
        return "Cool";
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        List<Placement> possiblePlacements = new ArrayList<>();
        
        

        for(Building building : game.getPlacableBuildings()){
            for(Direction direction : building.getTurnable().getPossibleDirections()){
                for(int x=0; x<10; ++x){
                    for(int y=0; y<10; ++y){
                        Placement potetialPlacement = new Placement(new Position(x,y), direction, building);
                        if(game.takeTurn(potetialPlacement, true)){
                            possiblePlacements.add(potetialPlacement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }

        System.out.println(possiblePlacements.size());
        if(!possiblePlacements.isEmpty()) {
            return Optional.of(possiblePlacements.get(new Random().nextInt(possiblePlacements.size())));
        }
        return Optional.empty();
    }
}

