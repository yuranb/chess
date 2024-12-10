import chess.*;
import ui.Repl;
import facade.ServerFacade;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ Welcome to 240 Chess Client: " + piece);

        var baseUrl = "http://localhost:8080";
        if (args.length == 1) {
            baseUrl = args[0];
        }

        ServerFacade server = new ServerFacade(baseUrl);
        Repl repl = new Repl(server, baseUrl);
        repl.run();
    }
}