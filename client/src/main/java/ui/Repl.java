package ui;

import facade.ServerFacade;
import exception.ResponseException;
import model.GameData;

import java.util.List;
import java.util.Scanner;

public class Repl {

    private enum ReplState {
        PRE_LOGIN,
        POST_LOGIN
    }

    private final ServerFacade server;
    private ReplState state;
    private final Scanner scanner;

    public Repl(ServerFacade server) {
        this.server = server;
        this.state = ReplState.PRE_LOGIN;
        this.scanner = new Scanner(System.in);
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
            System.out.println("Successfully joined game " + gameID + " as observer");
            new ChessBoardUI().display();
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID format");
            return true;
        }
    }

    private boolean playGame(String[] input) {
        if (input.length < 2) {
            System.out.println("Usage: play <gameID> [color]");
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

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }
}
