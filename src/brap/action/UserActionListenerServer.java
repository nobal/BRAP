package brap.action;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.logging.Logger;

import brap.tool.Utils;

import main.BrapMain;


import test.EventAttacherTest;

// A Web Server waits for clients to connect, then starts a separate
// thread to handle the request.
public class UserActionListenerServer extends Thread {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger.getLogger(UserActionListenerServer.class
			.getName());

	public static int ServerPort = 4444;
	private static ServerSocket serverSocket;
	private UserAction uaLogger;
	public UserActionListenerServer(int port,UserAction listener) {
		ServerPort = port;
		this.uaLogger=listener;
		start();
	}
	
	public static void closeServer(){
		if(serverSocket!=null){
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(ServerPort);
			LOGGER.info("Waiting for browser events");
			boolean stop=false;
			while (!stop) {
				try {
					if(serverSocket.isClosed()){
						break;
					}
					Socket s = serverSocket.accept(); // Wait for a client to
														// connect
					new ClientHandler(s,uaLogger); // Handle the client in a separate
											// thread
				} catch (Exception x) {
					LOGGER.info("Socket is closed. Not going to record events anymore");
					stop=true;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

// A ClientHandler reads an HTTP request and responds
class ClientHandler extends Thread {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger.getLogger(ClientHandler.class
			.getName());
	private Socket socket; // The accepted socket from the Webserver
	UserAction uaLogger;
	// Start the thread in the constructor
	public ClientHandler(Socket s,UserAction ual) {
		socket = s;
		uaLogger=ual;
		start();
	}

	// Read the HTTP request, respond, and close the connection
	public void run() {
		try {
			// Open connections to the socket
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintStream out = new PrintStream(new BufferedOutputStream(
					socket.getOutputStream()));

			String s = in.readLine();
			System.out.println("The S is:'"+s+"'");
			String interaction=s.substring(s.indexOf("/")+1,s.indexOf("HTTP")).trim();
			interaction=Utils.squeezeMultipleWhiteSpaceToOne(interaction);
			interaction=interaction.replaceAll("TAB","\t");
			interaction=URLDecoder.decode(interaction, "UTF-8");
			LOGGER.info("Action from user:"+interaction);
			uaLogger.logAction(interaction);

			out.println("HTTP/1.1 200 OK\r\n"
					+ "Content-type: text/html\r\n\r\n"
					+ "<html><head></head><body>recorded</body></html>\n");
			out.close();

		} catch (IOException x) {
			LOGGER.severe("Couldn't record the interaction!");
		}
	}
}
