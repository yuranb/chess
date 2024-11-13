package ui;

import Facade.ServerFacade;
import exception.ResponseException;
import java.util.Scanner;

import static java.awt.Color.RED;

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
        System.out.println("\u265F Welcome to Chess Client! Please sign in to continue.");

        boolean running = true;
        while (running) {
            switch (state) {
                case PRE_LOGIN:
                    running = handlePreLogin();
                    break;
                case POST_LOGIN:
                    break;
            }
        }
    }

    private boolean handlePreLogin() {
        System.out.print("[PRE-LOGIN] >>> ");
        String[] input = scanner.nextLine().split(" ");
        if (input.length == 0) return true;

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
                System.out.println(RED + "Unknown command. Type 'help' for available commands.");
        }
        return true;
    }

    private boolean register(String[] input) {
        return false;
    }

    private void printPreLoginHelp() {
    }

    private boolean login(String[] input) {
        if (input.length < 3) {
            System.out.println("Usage: login <username> <password>");
            return true;
        }
        try {
            server.login(input[1], input[2]);
            System.out.println("Login successful.");
            state = ReplState.POST_LOGIN;
            return true;
        } catch (ResponseException e) {
            System.out.println("Login failed: " + e.getMessage());
            return true;
        }
    }
}
