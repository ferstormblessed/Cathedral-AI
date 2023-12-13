package ai;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;
import de.fhkiel.ki.cathedral.game.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
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

        if (!placesNearCathedral.isEmpty()) {
            System.out.println("in near cathedral");
            System.out.println("Size of posible placements: " + placesNearCathedral.size());
            return Optional.of(selectRandomPlacement(placesNearCathedral));

        } else if (!placesNearOwnBuilding.isEmpty()) {
            System.out.println("in near building");
            System.out.println("Size of posible placements: " + placesNearOwnBuilding.size());
            return Optional.of(selectRandomPlacement(placesNearOwnBuilding));
        } else if (!highestScoreBuildings.isEmpty()) {
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

    // evaluate turn (aka SCORE function)
    @Override
    public String evaluateLastTurn(Game game) {
        int i = AreaControlledAroundCathedral(game, game.getCurrentPlayer());
        return String.valueOf(game.getCurrentPlayer());
    }

    public int Score(Placement placement) {
        return 0;
    }

    // get all the posible placements of the playable buildings with the highest
    // score value
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
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Placement> placeNearCathedral(Game game, List<Placement> placements) {
        List<Placement> placesNearCathedral = new ArrayList<>();
        List<Placement> placesTouchingCathedral = new ArrayList<>();

        for (Placement placement : placements) {
            List<Position> silhouette = placement.building().silhouette(placement.direction())
                    .stream()
                    .map(position -> position.plus(placement.position()))
                    .filter(Position::isViable)
                    .distinct()
                    .collect(Collectors.toList());

            Position currentPosition = placement.position();
            boolean isTouching = isTouchingCathedral(game, currentPosition);

            boolean isNearCathedral = silhouette
                    .stream()
                    .anyMatch(position -> Color.Blue == game.getBoard().getField()[position.y()][position.x()]);

            if (isTouching) {
                placesTouchingCathedral.add(placement);
            }

            if (isNearCathedral && !isTouching) {
                placesNearCathedral.add(placement);
            }
        }
        if (!placesTouchingCathedral.isEmpty()) {
            return placesTouchingCathedral;
        } else {
            return placesNearCathedral;
        }

    }

    private boolean isTouchingCathedral(Game game, Position pos) {
        Color[][] field = game.getBoard().getField();
        if (pos.plus(1, 0).isViable() && field[pos.y()][pos.x() + 1] == Color.Blue) {
            return true;
        } else if (pos.minus(1, 0).isViable() && field[pos.y()][pos.x() - 1] == Color.Blue) {
            return true;
        } else if (pos.plus(0, 1).isViable() && field[pos.y() + 1][pos.x()] == Color.Blue) {
            return true;
        } else if (pos.minus(0, 1).isViable() && field[pos.y() - 1][pos.x()] == Color.Blue) {
            return true;
        }
        return false;
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
                    .distinct()
                    .collect(Collectors.toList());

            boolean isNearOwn = silhouette.stream()
                    .anyMatch(position -> placement.building()
                            .getColor() == game.getBoard().getField()[position.y()][position.x()]);

            boolean isNearAWall = silhouette.stream()
                    .anyMatch(position -> position.x() == 0 || position.x() == boardWidth - 1 || position.y() == 0
                            || position.y() == boardHeight - 1);

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

    private int AreaControlledAroundCathedral(Game game, Color player) {
        int colorID = player.getId();
        List<Integer> idsAroundCathedral = IDAroundCathedral(game);
        for (Integer num : idsAroundCathedral) {
            System.out.println(String.format("%d", num));
        }

        if (colorID == 2) {
            // i'm black
            // search for id --> 2(color black) and 3(owned by black)

        } else {
            // i'm white
            // search for id --> 4(color white) and 5(owned by white)
        }

        return 0;
    }

    // this helper function returns all the color id's of the tiles around the
    // Cathedral
    // / # # #
    // # # C # #
    // # C C C #
    // # # C # #
    // / # C #
    // / # # #
    private List<Integer> IDAroundCathedral(Game game) {
        Board board = game.getBoard();
        List<Integer> ids = new ArrayList<>();
        List<Position> cathedral = findCathedral(board);

        Color[][] field = board.getField();
        System.out.println("print field from IDAroundCathedral");
        printField(field);
        // add the checked positions and also the cathedral's positions
        Set<Position> checkedPositions = new HashSet<Position>();

        // adding cathedral's positions
        System.out.println("Cathedral's position");
        for (Position pos : cathedral) {
            checkedPositions.add(pos);
            System.out.println(String.format("%d, %d", pos.x(), pos.y()));
        }

        for (Position pos : cathedral) {
            Position Left = pos.plus(-1, 0);
            Position LeftUpDiagonal = pos.plus(-1, -1);
            Position Up = pos.plus(0, -1);
            Position RightUpDiagonal = pos.plus(1, -1);
            Position Right = pos.plus(1, 0);
            Position RightDownDiagonal = pos.plus(1, 1);
            Position Down = pos.plus(0, 1);
            Position LeftDownDiagonal = pos.plus(-1, 1);
            int idColor = -1;

            System.out.println(String.format("\nchecking position --> (%d, %d)\n", pos.x(), pos.y()));

            if (Left.isViable() && !checkedPositions.contains(Left)) {
                checkedPositions.add(Left);
                idColor = field[Left.y()][Left.x()].getId();
                System.out.println("Checking Left");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, Left.x(), Left.y()));
                ids.add(idColor);
            }
            if (LeftUpDiagonal.isViable() && !checkedPositions.contains(LeftUpDiagonal)) {
                checkedPositions.add(LeftUpDiagonal);
                idColor = field[LeftUpDiagonal.y()][LeftUpDiagonal.x()].getId();
                System.out.println("Checking LeftUpDiagonal");
                System.out.println(
                        String.format("id --> %d, pos --> (%d, %d)", idColor, LeftUpDiagonal.x(), LeftUpDiagonal.y()));
                ids.add(idColor);
            }
            if (Up.isViable() && !checkedPositions.contains(Up)) {
                checkedPositions.add(Up);
                idColor = field[Up.y()][Up.x()].getId();
                System.out.println("Checking Up");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, Up.x(), Up.y()));
                ids.add(idColor);
            }
            if (RightUpDiagonal.isViable() && !checkedPositions.contains(RightUpDiagonal)) {
                checkedPositions.add(RightUpDiagonal);
                idColor = field[RightUpDiagonal.y()][RightUpDiagonal.x()].getId();
                System.out.println("Checking RightUpDiagonal");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, RightUpDiagonal.x(),
                        RightUpDiagonal.y()));
                ids.add(idColor);
            }
            if (Right.isViable() && !checkedPositions.contains(Right)) {
                checkedPositions.add(Right);
                idColor = field[Right.y()][Right.x()].getId();
                System.out.println("Checking Right");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, Right.x(), Right.y()));
                ids.add(idColor);
            }
            if (RightDownDiagonal.isViable() && !checkedPositions.contains(RightDownDiagonal)) {
                checkedPositions.add(RightDownDiagonal);
                idColor = field[RightDownDiagonal.y()][RightDownDiagonal.x()].getId();
                System.out.println("Checking RightDownDiagonal");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, RightDownDiagonal.x(),
                        RightDownDiagonal.y()));
                ids.add(idColor);
            }
            if (Down.isViable() && !checkedPositions.contains(Down)) {
                checkedPositions.add(Down);
                idColor = field[Down.y()][Down.x()].getId();
                System.out.println("Checking Down");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, Down.x(), Down.y()));
                ids.add(idColor);
            }
            if (LeftDownDiagonal.isViable() && !checkedPositions.contains(LeftDownDiagonal)) {
                checkedPositions.add(LeftDownDiagonal);
                idColor = field[LeftDownDiagonal.y()][LeftDownDiagonal.x()].getId();
                System.out.println("Checking LeftDownDiagonal");
                System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor, LeftDownDiagonal.x(),
                        LeftDownDiagonal.y()));
                ids.add(idColor);
            }
        }
        return ids;
    }

    private void printField(Color[][] field) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                System.out.print(String.format("[%d]", field[i][j].getId()));
            }
            System.out.println("");
        }
    }

    private List<Position> findCathedral(Board board) {
        Color[][] field = board.getField();
        List<Position> cathedral = new ArrayList<Position>();
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                // found a tile of the cathedral
                if (field[i][j] == Color.Blue) {
                    // store the x --> j and y --> i coordinates
                    Position pos = new Position(j, i);
                    cathedral.add(pos);
                }
            }
        }
        return cathedral;
    }

    private List<Placement> placeLastPieces(Game game) {
        return game.getPlacableBuildings()
                .stream()
                .map(building -> building.getPossiblePlacements(game))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

    }

    private Placement selectRandomPlacement(List<Placement> placements) {
        return placements.get(new Random().nextInt(placements.size()));
    }
}
