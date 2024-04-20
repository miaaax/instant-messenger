package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shared.Message;

class ServerInitializationException extends Exception {
    public ServerInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

public class Server {
    public static final int SERVER_USER_ID = 0;
    private static final int DEFAULT_PORT = 3000;
    private static final int MAX_THREADS = 20;

    private ServerSocket socket;
    private Map<String, Integer> usernames;
    private Map<Integer, ServerUser> users;
    private Set<Integer> activeUsers;

    public Server(int port) throws ServerInitializationException {
        try {
	    socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ServerInitializationException("Error creating the ServerSocket.", e);
        }

        usernames = new HashMap<>();
	users = new HashMap<>();
	activeUsers = new HashSet<>();
        
        try {
            init_users();
        } catch (FileNotFoundException e) {
            throw new ServerInitializationException("Could not find users.txt file. No users could be loaded.", e);
        }

	ExecutorService tp = Executors.newFixedThreadPool(MAX_THREADS);
	tp.execute(new MessageQueueWriter());

	System.out.println("Now listening on port " + port + "...");
	while (true) {
            try {
	        Socket clientSocket = socket.accept();
	        tp.execute(new Session(clientSocket, this));
            } catch (IOException e) {
                System.err.println("Error accepting a new connection.");
                e.printStackTrace();
            }
	}
    }

    private void init_users() throws FileNotFoundException {
        File user_db = new File("users.txt");
        Scanner scanner = new Scanner(user_db);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher matcher = Pattern.compile("(\\w+)\\s+(\\w+)").matcher(line);
            
            if (matcher.find()) {
                String username = matcher.group(1);
                String password = matcher.group(2);

                ServerUser user = new ServerUser(username, password);

                int userId = user.getUserId();

                usernames.put(username, userId);
                users.put(userId, user);
            } else {
                System.out.println("Rejecting username/password " + line + "...");
            }
        }

        scanner.close();
    }

    public synchronized ServerUser login(String username, String password) {
	if (usernames.containsKey(username)) {
            int id = usernames.get(username);
	    ServerUser user = users.get(id);
	    if (user.authenticate(password)) {
		activeUsers.add(user.getUserId());
		System.out.println("Successfully logged in user " + username);
                return user;
	    } else {
		System.out.println("Authentication failure for user " + username);
                return null;
	    }
	} else {
            System.out.println("User " + username + " does not exist on the system.");
            return null;
        }
    }

    public synchronized void logout(ServerUser user) {
        if (user == null) {
            System.out.println("Cannot log out null user.");
            return;
        }

	if (activeUsers.contains(user.getUserId())) {
	    user.logout();
	    activeUsers.remove(user.getUserId());
	} else {
	    System.out.println("Failed to log out user " + user.getUsername() + ". Already signed out.");
	}
    }

    public void forward(Message msg) {
        System.out.println("Forwarded message received by server."); 
        // TODO: Read the message recepients, write to each recipients message queue
        log(msg);
    }

    private void log(Message msg) {
        // TODO: Implement logging
    }

    public static void main(String[] args) {
        try {
	    Server s = new Server(DEFAULT_PORT);
        } catch (ServerInitializationException e) {
            e.printStackTrace(); 
            System.exit(1);
        }
    }

    private static class MessageQueueWriter implements Runnable {
	@Override
	public void run() {
	    // TODO: Repeatedly check each active user's message queue; 
            // write one message per users queue per iteration
	    System.out.println("Hello from the MessageQueueWriter!");
	}
    }
}
