package brap.player;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class StandAlonePlayer extends Player {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger.getLogger(StandAlonePlayer.class.getName());
	public static int port = 8888;
	private static ServerSocket serverSocket;
	private static WebDriver driver;
	private int playerDelayInSeconds=0;

	public StandAlonePlayer(WebDriver driver, int port, int playerDelayInSeconds) {
		super(driver);
		StandAlonePlayer.driver = driver;
		StandAlonePlayer.port = port;
		this.playerDelayInSeconds=playerDelayInSeconds;
	}

	public static void closeServer() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

	public void run() {
		try {
			if (driver == null) {
				driver = new FirefoxDriver();
			}

			String url = null;
			serverSocket = new ServerSocket(port);
			LOGGER.info("Waiting for playing requests on port: " + port);
			boolean stop = false;
			while (!stop) {
				try {
					if (serverSocket.isClosed()) {
						LOGGER.info("Socket is closed. Not going to record events anymore");
						break;
					}
					Socket s = serverSocket.accept(); // Wait for a client to
														// connect
					try {
						// Open connections to the socket
						BufferedReader request = new BufferedReader(
								new InputStreamReader(s.getInputStream()));
						String input = request.readLine();
						input = URLDecoder.decode(input, "UTF-8");

						String[] lines = input.split("\n");
						List<String> intLines = new ArrayList<String>();
						// don't include first and last lines. rest are exact
						// content of interaction files
						url = null;
						for (int i = 1; i < lines.length - 1; i++) {
							if (url == null) {
								url = lines[2].split("\t")[0]; // 2 because
																// first line in
																// interaction
																// file is its
																// header
							}
							intLines.add(lines[i]);
						}

						String output = playActions(intLines, url,this.playerDelayInSeconds);
						PrintStream response = new PrintStream(
								new BufferedOutputStream(s.getOutputStream()));
						response.println("HTTP/1.1 200 OK\r\n"
								+ "Content-type: text/html\r\n\r\n" + output
								+ "\n");
						response.close();
					} catch (IOException x) {
						LOGGER.severe("Couldn't play the interaction!");
					}

				} catch (Exception x) {
					LOGGER.info("Socket is closed. Not going to play interactions anymore"
							+ x);
					stop = true;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeServer();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StandAlonePlayer player = new StandAlonePlayer(null, 8888,0);
		player.run();

	}

}
