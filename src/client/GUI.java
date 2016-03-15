package client;

import javax.swing.*;
import java.util.HashMap;

public class GUI extends JFrame {

    // Fields
    private Client client;
    private LoginScreen loginScreen;
    private LobbyScreen lobbyScreen;
    private Board board;
    private HashMap<String, String> requestMessageMap;
    private JPanel currentScreen;
    private final static String BASE_WINDOW_TITLE = "BoardGameServer";
    private String username;

    // Methods
    public GUI(Client client) {
        super(BASE_WINDOW_TITLE);
        this.client = client;
        this.loginScreen = new LoginScreen(this);
        this.lobbyScreen = new LobbyScreen(this);

        add(loginScreen, "Center");
        loginScreen.setVisible(true);
        currentScreen = loginScreen;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(450, 300);
        setVisible(true);
        setResizable(false);

        requestMessageMap = new HashMap<>();
        requestMessageMap.put("VICTORY", "You win!");
        requestMessageMap.put("DEFEAT", "You lose!");
        requestMessageMap.put("TIE", "You tied!");
        requestMessageMap.put("VALID_MOVE", "It is the opponent's turn");
        requestMessageMap.put("OPPONENT_MOVED", "It is your turn");
    }

    public void appendToTitle(String toAppend) {
        setTitle(getTitle() + " - " + toAppend);
    }

    public void handleRequest(String request) {
        Request r = new Request(request);
        handleRequest(r);
    }

    public void handleRequest(Request request) {
        String[] tokens = request.getTokens();
        Command command = request.getCommand();

        switch(command) {
            case MOVE: case JOIN: case LOGGING_IN: case GOTO_LOBBY:
                client.handleRequest(request);
                break;
            case LOGIN_SUCCESS:
                username = tokens[1];
                appendToTitle(username);
                lobbyScreen.setUsername(username);
                lobbyScreen.requestWaitlist();
                changePanel(lobbyScreen);
                loginScreen.clearFields();
                break;
            case WELCOME:
                board = new Board(this, 3);
                appendToTitle(tokens[2]);
                board.setTurnLabel("Player X starts first");
                board.handleRequest(request.getRequest()); // TODO
                changePanel(board);
                break;
            case VICTORY: case DEFEAT: case TIE:
                String message = requestMessageMap.get(request.getRequest()); // TODO
                board.setTurnLabel(message);
                board.handleRequest(request.getRequest()); // TODO
                JOptionPane.showMessageDialog(this, message);
                lobbyScreen.requestWaitlist();
                changePanel(lobbyScreen);
                setTitle(BASE_WINDOW_TITLE);
                appendToTitle(username);
                break;
            case VALID_MOVE: case OPPONENT_MOVED:
                board.setTurnLabel(requestMessageMap.get(command.toString())); // TODO
                board.handleRequest(request.getRequest()); // TODO
                break;
            case LOBBY:
                lobbyScreen.addAllToWaitList(request.getRequest());
                break;
            case LOGOUT:
                client.handleRequest(new Request("LOGOUT"));  // TODO
                setTitle(BASE_WINDOW_TITLE);
                break;
            case DISCONNECTED:
                changePanel(loginScreen);
                break;
            default:
                break;
        }
    }

    private void changePanel(JPanel nextPanel) {
        add(nextPanel);
        nextPanel.setVisible(true);
        currentScreen.setVisible(false);
        remove(currentScreen);
        validate();
        repaint();
        currentScreen = nextPanel;
    }

}
