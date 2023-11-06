package ai;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;
import de.fhkiel.ki.cathedral.game.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomCleverAI implements Agent {
    @Override
    public String name() {
        return "RandomCleverAI";
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        List<Placement> highestScoreBuildings = getHighestScoreBuildings(game);
        List<Placement> placesNearCathedral = placeNearCathedral(game, highestScoreBuildings);
        List<Placement> placesNearOwnBuilding = placeNearOwnBuilding(game, highestScoreBuildings);

        if(!placesNearCathedral.isEmpty()) {
            System.out.println("in near cathedral");
            System.out.println("Size of posible placements: " + placesNearCathedral.size());
            return Optional.of(selectRandomPlacement(placesNearCathedral));

        } else if (!placesNearOwnBuilding.isEmpty()) {
            System.out.println("in near building");
            System.out.println("Size of posible placements: " + placesNearOwnBuilding.size());
            return Optional.of(selectRandomPlacement(placesNearOwnBuilding));
        }
        else if (!highestScoreBuildings.isEmpty()) {
            System.out.println("in highest building");
            System.out.println("Size of posible placements: " + highestScoreBuildings.size());
            return Optional.of(selectRandomPlacement(highestScoreBuildings));
        } else {
            List<Placement> lastPieces = placeLastPieces(game);
            System.out.println("in last pieces");
            System.out.println("Size of posible placements: " + lastPieces.size());
            if (!lastPieces.isEmpty()) {
                return Optional.of(selectRandomPlacement(lastPieces));
            }
            System.out.println("Size of posible placements (should be empty): " + lastPieces.size());

        }

        System.out.println("No more posible placement");

        return Optional.empty();
    }

    //get all the posible placements of the playable buildings with the highest score value
    private List<Placement> getHighestScoreBuildings(Game game) {
        List<Building> placeableBuildings = game.getPlacableBuildings();

        int highestScore = placeableBuildings
            .stream()
            .mapToInt(Building::score)
            .max()
            .orElse(-1);

        return placeableBuildings 
            .stream()
            .filter(building -> building.score() == highestScore)
            .map(building -> building.getPossiblePlacements(game))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private List<Placement> placeNearCathedral(Game game, List<Placement> placements) {
        List<Placement> places = new ArrayList<>();

        for (Placement placement : placements) {
            List<Position> silhouette = placement.building().silhouette(placement.direction())
                .stream()
                .map(position -> position.plus(placement.position()))
                .filter(Position::isViable)
                .collect(Collectors.toList());

            boolean isNearCathedral = silhouette
                .stream()
                .anyMatch(position -> Color.Blue == game.getBoard().getField()[position.y()][position.x()]);

            if (isNearCathedral) {
                places.add(placement);
            }
        }
        return places;
    }

    private List<Placement> placeNearOwnBuilding(Game game, List<Placement> placements) {
        List<Placement> nearWallAndOwn = new ArrayList<>();
        List<Placement> nearOwnBuilding = new ArrayList<>();
        List<Placement> nearWall = new ArrayList<>();
        int boardWidth = game.getBoard().getField()[0].length;
        int boardHeight = game.getBoard().getField().length;

        for (Placement placement : placements) {
            List<Position> silhouette = placement.building().silhouette(placement.direction())
                .stream()
                .map(position -> position.plus(placement.position()))
                .filter(Position::isViable)
                .collect(Collectors.toList());

            boolean isNearOwn = silhouette.stream()
                .anyMatch(position -> placement.building().getColor() == game.getBoard().getField()[position.y()][position.x()]);

            boolean isNearAWall = silhouette.stream()
                .anyMatch(position -> position.x() == 0 || position.x() == boardWidth - 1 || position.y() == 0 || position.y() == boardHeight - 1);

            if (isNearAWall && isNearOwn) {
                nearWallAndOwn.add(placement);
            } else if (isNearOwn) {
                nearOwnBuilding.add(placement);
            } else if (isNearAWall) {
                nearWall.add(placement);
            }
        }

        if (!nearWallAndOwn.isEmpty()) {
            return nearWallAndOwn;
        } else if (!nearOwnBuilding.isEmpty()) {
            return nearOwnBuilding;
        } else {
            return nearWall;
        }
    }

    private List<Placement> placeLastPieces(Game game) {
        return game.getPlacableBuildings()
            .stream()
            .map(building -> building.getPossiblePlacements(game))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    }

    private Placement selectRandomPlacement(List<Placement> placements) {
        return placements.get(new Random().nextInt(placements.size()));
    }
}

