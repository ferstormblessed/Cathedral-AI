package ai;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Turn;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;
import de.fhkiel.ki.cathedral.game.Color;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

        // SORTING LISTS with respect to SCORE
        // hacer sort de todos los posibles movimientos reespecto a su score
        List<PlacementScore> placesNearCathedralWithScore = new ArrayList<PlacementScore>();
        for (Placement placement : placesNearCathedral) {
            int score = Score(game, placement);
            PlacementScore p = new PlacementScore(placement, score);
            placesNearCathedralWithScore.add(p);
        }

        List<PlacementScore> placesNearOwnBuildingWithScore = new ArrayList<PlacementScore>();
        for (Placement placement : placesNearOwnBuilding) {
            int score = Score(game, placement);
            PlacementScore p = new PlacementScore(placement, score);
            placesNearOwnBuildingWithScore.add(p);
        }

        List<PlacementScore> highestScoreBuildingsWithScore = new ArrayList<PlacementScore>();
        for (Placement placement : highestScoreBuildings) {
            int score = Score(game, placement);
            PlacementScore p = new PlacementScore(placement, score);
            placesNearOwnBuildingWithScore.add(p);
        }
        // System.out.println("Before sort");
        // for (PlacementScore p : placesNearCathedralWithScore) {
        // System.out.print(String.format(" %s --> %d ",
        // p.getPlacement().building().getName(), p.score));
        // }
        // System.out.println("");
        // System.out.println("After sort");

        // SORTING
        // se podria hacer una optimizacion aqui para no calcular cuando realmente no se
        // necesita porque no se usan por ejemplo placesNearOwnBuilding y
        // highestScoreBuilding cuando near cathedral todavia tiene elementos
        Collections.sort(placesNearCathedralWithScore, Comparator.comparingInt(PlacementScore::getScore));
        Collections.sort(placesNearOwnBuildingWithScore, Comparator.comparingInt(PlacementScore::getScore));
        Collections.sort(highestScoreBuildingsWithScore, Comparator.comparingInt(PlacementScore::getScore));

        // for (PlacementScore p : placesNearOwnBuildingWithScore) {
        // System.out.print(String.format(" %s --> %d ",
        // p.getPlacement().building().getName(), p.score));
        // }
        // System.out.println("");

        if (!placesNearCathedralWithScore.isEmpty()) {
            System.out.println("in near cathedral");
            System.out.println("Size of posible placements: " + placesNearCathedralWithScore.size());
            return Optional
                    .of(placesNearCathedralWithScore.get(placesNearCathedralWithScore.size() - 1).getPlacement());
        } else if (!placesNearOwnBuildingWithScore.isEmpty()) {
            System.out.println("in near building");
            System.out.println("Size of posible placements: " + placesNearOwnBuildingWithScore.size());
            return Optional
                    .of(placesNearOwnBuildingWithScore.get(placesNearOwnBuildingWithScore.size() - 1).getPlacement());
        } else if (!highestScoreBuildingsWithScore.isEmpty()) {
            System.out.println("in highest building");
            System.out.println("Size of posible placements: " + highestScoreBuildingsWithScore.size());
            return Optional.of(highestScoreBuildingsWithScore.get(0).getPlacement());
        } else {
            List<Placement> lastPieces = placeLastPieces(game);
            List<PlacementScore> lastPiecesWithScore = new ArrayList<PlacementScore>();

            for (Placement placement : lastPieces) {
                int score = Score(game, placement);
                PlacementScore p = new PlacementScore(placement, score);
                lastPiecesWithScore.add(p);
            }
            Collections.sort(lastPiecesWithScore, Comparator.comparingInt(PlacementScore::getScore));

            System.out.println("in last pieces");
            System.out.println("Size of posible placements: " + lastPiecesWithScore.size());
            if (!lastPiecesWithScore.isEmpty()) {
                return Optional.of(lastPiecesWithScore.get(0).getPlacement());
            }
            System.out.println("Size of posible placements (should be empty): " + lastPiecesWithScore.size());

        }

        System.out.println("No more posible placement");

        return Optional.empty();
    }

    // evaluate turn (aka SCORE function)
    @Override
    public String evaluateLastTurn(Game game) {
        if (game.lastTurn().getTurnNumber() < 2) {
            return "";
        }
        Placement p = game.lastTurn().getAction();
        game.undoLastTurn();
        int i = Score(game, p);
        String s = String.format("Building --> %s, Score --> %d", game.lastTurn().getAction().building().getName(), i);
        return s;
    }

    public int Score(Game game, Placement placement) {
        // create random int to add variaty to the scores for now
        // Random r = new Random();
        // int random = r.nextInt(10);
        int score = 0;

        // score += random;

        int areaAroundCathedralMultiplier = 5;
        int notInAreaControlledMulitiplier = 2;
        int areaWonMulitiplier = 10;

        Color[][] lastTurnField = game.getBoard().getField();

        if (game.takeTurn(placement, false)) {
            // System.out.println(
            // String.format("Move made is --> %s, current player is --> %d",
            // placement.building().getName(),
            // game.getCurrentPlayer().getId()));
            // System.out.println("board after making move");
            // printField(game.getBoard().getField());
            Color[][] currentTurnField = game.getBoard().getField();
            int currentPlayer = game.getCurrentPlayer().getId();
            score += areaAroundCathedralMultiplier
                    * AreaControlledAroundCathedral(currentTurnField, currentPlayer);
            score += PlaceNotInControlledTerritory(lastTurnField, placement, currentPlayer)
                    * notInAreaControlledMulitiplier;
            score += areaWon(currentPlayer, lastTurnField, currentTurnField) * areaWonMulitiplier;
            // System.out.println(String.format("Resulting score --> %d", score));
            game.undoLastTurn();
        }

        return score;
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

    private int areaWon(int player, Color[][] previosField, Color[][] currentField) {
        int score = 0;
        if (player == 4) {
            // im black
            int prevScore = getAreaControlledByPlayer(2, previosField);
            int currScore = getAreaControlledByPlayer(2, currentField);
            score = currScore - prevScore;
        } else {
            // im white
            int prevScore = getAreaControlledByPlayer(4, previosField);
            int currScore = getAreaControlledByPlayer(4, currentField);
            score = currScore - prevScore;
        }
        return score;
    }

    private int getAreaControlledByPlayer(int player, Color[][] field) {
        int score = 0;
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                if (field[i][j].getId() == player || field[i][j].getId() == player + 1) {
                    score += 1;
                }
            }
        }
        return score;
    }

    private int AreaControlledAroundCathedral(Color[][] field, int player) {
        List<Integer> idsAroundCathedral = IDAroundCathedral(field);
        // for (Integer num : idsAroundCathedral) {
        // System.out.print(String.format(" %d ", num));
        // }
        // System.out.println("");
        // System.out.println(String.format("current player --> %d", player));
        int area = 0;

        // i get the currentplayer which is the oppenent because i made the last move
        // so the if is inverted sozusagen
        // real ids
        // black --> 2
        // white --> 4
        // but we do the inverse

        if (player == 4) {
            // i'm black
            // search for id --> 2(color black) and 3(owned by black)
            for (Integer id : idsAroundCathedral) {
                if (id == 2 || id == 3) {
                    area += 1;
                    // System.out.println(String.format("found a match, area --> %d", area));
                }
            }
        } else {
            // i'm white
            // search for id --> 4(color white) and 5(owned by white)
            for (Integer id : idsAroundCathedral) {
                if (id == 4 || id == 5) {
                    area += 1;
                    // System.out.println(String.format("found a match, area --> %d", area));
                }
            }
        }
        // System.out.println(String.format("Area(score) --> %d", area));

        return area;
    }

    // this helper function returns all the color id's of the tiles around the
    // Cathedral
    // / # # #
    // # # C # #
    // # C C C #
    // # # C # #
    // / # C #
    // / # # #
    private List<Integer> IDAroundCathedral(Color[][] field) {
        List<Integer> ids = new ArrayList<>();
        List<Position> cathedral = findCathedral(field);

        // System.out.println("\nprint field from IDAroundCathedral\n");
        // printField(field);
        // add the checked positions and also the cathedral's positions
        Set<Position> checkedPositions = new HashSet<Position>();

        // adding cathedral's positions
        // System.out.println("\nCathedral's position\n");
        for (Position pos : cathedral) {
            checkedPositions.add(pos);
            // System.out.println(String.format("%d, %d", pos.x(), pos.y()));
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

            // System.out.println(String.format("\nchecking position --> (%d, %d)", pos.x(),
            // pos.y()));

            if (Left.isViable() && !checkedPositions.contains(Left)) {
                checkedPositions.add(Left);
                idColor = field[Left.y()][Left.x()].getId();
                // System.out.println("Checking Left");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // Left.x(), Left.y()));
                ids.add(idColor);
            }
            if (LeftUpDiagonal.isViable() && !checkedPositions.contains(LeftUpDiagonal)) {
                checkedPositions.add(LeftUpDiagonal);
                idColor = field[LeftUpDiagonal.y()][LeftUpDiagonal.x()].getId();
                // System.out.println("Checking LeftUpDiagonal");
                // System.out.println(
                // String.format("id --> %d, pos --> (%d, %d)", idColor, LeftUpDiagonal.x(),
                // LeftUpDiagonal.y()));
                ids.add(idColor);
            }
            if (Up.isViable() && !checkedPositions.contains(Up)) {
                checkedPositions.add(Up);
                idColor = field[Up.y()][Up.x()].getId();
                // System.out.println("Checking Up");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // Up.x(), Up.y()));
                ids.add(idColor);
            }
            if (RightUpDiagonal.isViable() && !checkedPositions.contains(RightUpDiagonal)) {
                checkedPositions.add(RightUpDiagonal);
                idColor = field[RightUpDiagonal.y()][RightUpDiagonal.x()].getId();
                // System.out.println("Checking RightUpDiagonal");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // RightUpDiagonal.x(),
                // RightUpDiagonal.y()));
                ids.add(idColor);
            }
            if (Right.isViable() && !checkedPositions.contains(Right)) {
                checkedPositions.add(Right);
                idColor = field[Right.y()][Right.x()].getId();
                // System.out.println("Checking Right");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // Right.x(), Right.y()));
                ids.add(idColor);
            }
            if (RightDownDiagonal.isViable() && !checkedPositions.contains(RightDownDiagonal)) {
                checkedPositions.add(RightDownDiagonal);
                idColor = field[RightDownDiagonal.y()][RightDownDiagonal.x()].getId();
                // System.out.println("Checking RightDownDiagonal");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // RightDownDiagonal.x(),
                // RightDownDiagonal.y()));
                ids.add(idColor);
            }
            if (Down.isViable() && !checkedPositions.contains(Down)) {
                checkedPositions.add(Down);
                idColor = field[Down.y()][Down.x()].getId();
                // System.out.println("Checking Down");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // Down.x(), Down.y()));
                ids.add(idColor);
            }
            if (LeftDownDiagonal.isViable() && !checkedPositions.contains(LeftDownDiagonal)) {
                checkedPositions.add(LeftDownDiagonal);
                idColor = field[LeftDownDiagonal.y()][LeftDownDiagonal.x()].getId();
                // System.out.println("Checking LeftDownDiagonal");
                // System.out.println(String.format("id --> %d, pos --> (%d, %d)", idColor,
                // LeftDownDiagonal.x(),
                // LeftDownDiagonal.y()));
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

    private List<Position> findCathedral(Color[][] field) {
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

    // Discourage player from placing pieces in already controlled territory
    private int PlaceNotInControlledTerritory(Color[][] fieldLastTurn, Placement placement, int player) {
        int score = 0;
        // placement.pos = la coordena central de la pieza
        // obtener las coordenadas, respecto al centro de la pieza
        // buscar respecto a esas coordenadas en el field anterior
        Position pieceCentralPosition = placement.position();
        List<Position> buildingPositions = placement.building().turn(placement.direction());

        List<Position> positionToCheck = new ArrayList<Position>();
        for (Position pos : buildingPositions) {
            Position p = pieceCentralPosition.plus(pos);
            positionToCheck.add(p);
        }

        for (Position pos : positionToCheck) {
            int colorID = fieldLastTurn[pos.y()][pos.x()].getId();

            if (player == 4) {
                // i'm black
                // search for id --> 3(owned by black) is a minus
                // because area is controlled by us
                // search for id --> 0(not owned) is a plus
                // we expand our territory
                if (colorID == 3) {
                    score -= 1;
                }
                if (colorID == 0) {
                    score += 1;
                }
            } else {
                // i'm white
                // search for id --> 5(owned by black) is a minus
                // because area is controlled by us
                // search for id --> 0(not owned) is a plus
                // we expand our territory
                if (colorID == 5) {
                    score -= 1;
                }
                if (colorID == 0) {
                    score += 1;
                }
            }
        }

        return score;
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
