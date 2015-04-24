package player;

import scotlandyard.Move;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;

import java.util.Random;
import java.util.Set;

/**
 * The RandomPlayer class is an example of a very simple AI that
 * makes a random move from the given set of moves. Since the
 * RandomPlayer implements Player, the only required method is
 * notify(), which takes the location of the player and the
 * list of valid moves. The return value is the desired move,
 * which must be one from the list.
 */
public class GeneHunt implements Player {
  
    public GeneHunt(ScotlandYardView view, String graphFilename) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
    }

    @Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...       
        int choice = new Random().nextInt(moves.size());
        for (Move move : moves) {
            if (choice == 0) {
                return move;
            }
            choice--;
        }

        return null;
    }
    
    /**
     * Returns the current score of the board.
     * Factors:
     *     - Distance from detectives
     *     - PageRank of current node
     *     - Overall position (i.e. close to corner)
     *     - PageRank of detectives nodes
     *     - Maybe convex hull
     */
     private void scoreMrX() {
        //TODO: Implement the board score heuristic here.
     }
     
     private void scoreDetectives() {
        //TODO: Implement the board score heuristic here.
     }

}
