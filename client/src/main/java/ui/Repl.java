package ui;

import chess.ChessMove;
import chess.ChessPosition;
import facade.ServerFacade;
import exception.ResponseException;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.List;
import java.util.Scanner;

public class Repl implements ServerMessageObserver {

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION:
                NotificationMessage notif = (NotificationMessage) message;
                System.out.println("Notice : " + notif.getContent());
                break;
            case ERROR:
                ErrorMessage err = (ErrorMessage) message;
                System.out.println("Error : " + err.getErrorMessage());
                break;
            case LOAD_GAME:
                LoadGameMessage load = (LoadGameMessage) message;
                System.out.println("Load : " + load.getGame());
                break;
            default:
                System.out.println("Unknown massage type");
        }
    }

    private enum ReplState {
        PRE_LOGIN,
        POST_LOGIN,
        IN_GAME
    }

    private final ServerFacade server;
    private final String baseUrl;
    private ReplState state;
    private final Scanner scanner;
    private String currentGameID;
    private String currentPlayerColor;

    public Repl(ServerFacade server, String baseUrl) {
        this.server = server;
        this.baseUrl = baseUrl;
        this.state = ReplState.PRE_LOGIN;
        this.scanner = new Scanner(System.in);
        this.currentGameID = null;
        this.currentPlayerColor = null;
    }

    // Repl loop
    public void run() {
        System.out.println("Please log your account, Type 'help' to see available commands.");

        boolean running = true;
        while (running) {
            switch (state) {
                case PRE_LOGIN:
                    running = handlePreLogin();
                    break;
                case POST_LOGIN:
                    running = handlePostLogin();
                    break;
                case IN_GAME:
                    running = handleInGame();
                    break;
            }
        }
        System.out.println("Goodbye!");
    }

    private boolean handlePreLogin() {
        System.out.print("[PRE-LOGIN] >>> ");
        String[] input = scanner.nextLine().split(" ");
        if (input.length == 0) {
            return true;
        }

        String command = input[0].toLowerCase();

        switch (command) {
            case "login":
                return login(input);
            case "register":
                return register(input);
            case "quit":
                return false;
            case "help":
                printPreLoginHelp();
                break;
            default:
                System.out.println( "Unknown command. Type 'help' to see available commands.");
        }
        return true;
    }

    private boolean register(String[] input) {
        if (input.length < 4) {
            System.out.println("Usage: register <username> <password> <email>");
            return true;
        }
        try {
            server.register(input[1], input[2], input[3]);
            server.connectWebSocket(baseUrl, this);
            System.out.println("Registration successful. You are now logged in.");
            state = ReplState.POST_LOGIN; // Move to Post login state
            return true;
        } catch (ResponseException e) {
            System.out.println("This username is already taken. Please choose a different one.");
            return true;
        }
    }

    private boolean login(String[] input) {
        if (input.length < 3) {
            System.out.println("Usage: login <username> <password>");
            return true;
        }
        try {
            server.login(input[1], input[2]);
            System.out.println("Login successful. You are now logged in.");
            server.connectWebSocket(baseUrl, this);
            state = ReplState.POST_LOGIN;
            return true;
        } catch (ResponseException e) {
            System.out.println("Incorrect username or password. Please try again.");
            return true;
        }
    }

    private void printPreLoginHelp() {
        System.out.println("register <username> <password> <email> - Register a new user");
        System.out.println("login <username> <password> - Log in with an existing account");
        System.out.println("help - Show this help page");
        System.out.println("quit - Exit the client");
    }

    private boolean handlePostLogin() {
        System.out.print("[POST-LOGIN] >>> ");
        String[] input = scanner.nextLine().split(" ");
        if (input.length == 0) {
            return true;
        }

        String command = input[0].toLowerCase();

        switch (command) {
            case "logout":
                return logout();
            case "help":
                printPostLoginHelp();
                break;
            case "create":
                return createGame(input);
            case "list":
                return listGames();
            case "play":
                return playGame(input);
            case "observe":
                return observeGame(input);
            case "quit":
                return false;
            default:
                System.out.println("Unknown command. Type 'help' to see available commands.");
        }
        return true;
    }

    private boolean observeGame(String[] input) {
        if (input.length != 2) {
            System.out.println("Please provide a game ID");
            System.out.println("observe <ID> - observe a game");
            return true;
        }

        try {
            int gameID = Integer.parseInt(input[1]);
            server.playGame(gameID, null);  // observer
            System.out.println("Successfully joined game " + gameID + " as observer");
            state = ReplState.IN_GAME;
            currentGameID = String.valueOf(gameID);  // Initialize currentGameID
            currentPlayerColor = null; // Initialize currentPlayerColor
            new ChessBoardUI().display();
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID format");
            return true;
        } catch (ResponseException e) {
            System.out.println("Failed to observe game: " + e.getMessage());
            return true;
        }
    }

    private boolean playGame(String[] input) {
        if (input.length < 2) {
            System.out.println("Usage: play <gameID> [WHITE|BLACK]");
            return true;
        }

        int gameID;
        try {
            gameID = Integer.parseInt(input[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid gameID.");
            return true;
        }

        String color = input[2];
        try {
            server.playGame(gameID, color);
            System.out.println("Successfully joined game " + gameID + " as " + color);
            state = ReplState.IN_GAME;
            System.out.println("Type 'help' to see available commands.");
            currentGameID = String.valueOf(gameID);  // Initialize currentGameID
            currentPlayerColor = color; // Initialize currentPlayerColor
            new ChessBoardUI().display();
            return true;
        } catch (ResponseException e) {
            if (e.getMessage().contains("400")) {
                if (input[1].startsWith("-") || !isNumeric(input[1])) {
                    System.out.println("Please check your game ID.");
                } else {
                    System.out.println("Color must be WHITE or BLACK.");
                }
            } else if (e.getMessage().contains("403")) {
                System.out.println("This position is already taken.");
            } else {
                System.out.println("Unable to join the game.");
            }
            return true;
        }
    }

    private boolean listGames() {
        try {
            List<GameData> games = server.listGames();
            if (games.isEmpty()) {
                System.out.println("No games available.");
            } else {
                System.out.println("Available games:");
                for (GameData game : games) {
                    System.out.printf("GameID: %-6d | GameName: %-20s | WHITE: %-10s | BLCAK: %-10s%n",
                            game.gameID(),
                            game.gameName(),
                            game.whiteUsername() != null ? game.whiteUsername() : "<->",
                            game.blackUsername() != null ? game.blackUsername() : "<->");
                }
            }
            return true;
        } catch (ResponseException e) {
            System.out.println("Unable to get games list. Please make sure you're logged in.");
            return true;
        }
    }

    private boolean createGame(String[] input) {
        if (input.length < 2) {
            System.out.println("Usage: create <gameName>");
            return true;
        }
        String gameName = input[1];
        try {
            GameData game = server.createGame(gameName);
            System.out.println("Game created with ID: " + game.gameID());
            return true;
        } catch (ResponseException e) {
            System.out.println("Unable to create game. Please try a different name.");
            return true;
        }
    }

    private void printPostLoginHelp() {
        System.out.println("create <gameName> - Create a new game");
        System.out.println("list - List available games");
        System.out.println("play <gameID> [WHITE|BLACK] - Join a game as a player");
        System.out.println("observe <gameID> - Watch a game as a observer");
        System.out.println("logout - Log out of your account");
        System.out.println("help - Show this help page");
        System.out.println("quit - Exit the client");
    }

    private boolean logout() {
        try {
            server.logout();
            System.out.println("Logged out successfully.");
            state = ReplState.PRE_LOGIN;
            return true;
        } catch (ResponseException e) {
            System.out.println("Unable to logout. Please try again.");
            return true;
        }
    }

    private boolean handleInGame() {
        System.out.print("[IN-GAME] >>> ");
        String[] input = scanner.nextLine().split(" ");
        if (input.length == 0) {
            return true;
        }

        String command = input[0].toLowerCase();
        switch (command) {
            case "help":
                printInGameHelp();
                break;
            case "move":
                return makeMove(input);
            case "resign":
                return resignGame();
            case "leave":
                state = ReplState.POST_LOGIN;
                System.out.println("Left the game");
                currentGameID = null;  // Reset currentGameID
                currentPlayerColor = null; // Reset currentPlayerColor
                return true;
            case "redraw":
                new ChessBoardUI().display();
                break;
            case "highlight":
                return highlightMoves(input);
            case "quit":
                return false;
            default:
                System.out.println("Unknown command. Type 'help' to see available commands.");
        }
        return true;
    }

    private void printInGameHelp() {
        System.out.println("Available commands:");
        System.out.println("  help                    - Show this help message");
        System.out.println("  move <from> <to>        - Make a move");
        System.out.println("  highlight <position>     - Highlight legal moves for a piece (e.g., 'highlight e2')");
        System.out.println("  redraw                  - Redraw the chess board");
        System.out.println("  resign                  - Resign from the current game");
        System.out.println("  leave                   - Leave the current game");
        System.out.println("  quit                    - Exit the program");
    }

    private boolean makeMove(String[] input) {
        if (input.length < 3) {
            System.out.println("Usage: move <from> <to>");
            return true;
        }

        String fromStr = input[1].toLowerCase();
        String toStr = input[2].toLowerCase();

        try {
            ChessPosition fromPos = parseChessPosition(fromStr);
            ChessPosition toPos = parseChessPosition(toStr);
            ChessMove move = new ChessMove(fromPos, toPos, null);

            server.makeMove(Integer.parseInt(currentGameID), move);
            System.out.println("Move sent: " + fromStr + " -> " + toStr);


        } catch (Exception e) {
            System.out.println("Invalid move or failed to send move: " + e.getMessage());
        }

        return true;
    }

    private ChessPosition parseChessPosition(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Position must be in format like 'e2'");
        }

        char fileChar = algebraic.charAt(0); // e.g. 'e'
        char rankChar = algebraic.charAt(1); // e.g. '2'

        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Invalid chess position: " + algebraic);
        }

        int col = fileChar - 'a' + 1; // 'a'->1
        int row = rankChar - '1' + 1; // '1'->1
        return new ChessPosition(row, col);
    }

    private boolean resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes")) {
            server.resignGame(Integer.parseInt(currentGameID));
            System.out.println("You have resigned from the game.");
            currentGameID = null;
            currentPlayerColor = null; 
            state = ReplState.POST_LOGIN;
        } else {
            System.out.println("Resignation canceled.");
        }
        return true;
    }

    private boolean highlightMoves(String[] input) {
        return false;
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}
