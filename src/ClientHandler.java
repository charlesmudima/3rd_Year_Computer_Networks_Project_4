
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler implements Runnable {
	Scanner scn = new Scanner(System.in);
	public String name;
	final DataInputStream input;
	final DataOutputStream output;
	Socket socket;
	boolean isloggedin;
	public static ArrayList<ClientHandler> offline = new ArrayList<ClientHandler>();


	/**
	 * @param socket
	 * @param name
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public ClientHandler(Socket socket, String name,
			DataInputStream input, DataOutputStream output) throws IOException {
		this.input = input;
		this.output = output;
		this.name = input.readUTF();
		this.socket = socket;
		this.isloggedin = true;
	}
	
	@Override
	public void run() {

		String received;

		while (socket.isConnected()) {
			try {
				received = input.readUTF();

				if (received.equals("exit")) {
					System.out.println("Updated list of people offline");
					offline.add(this);
					for (ClientHandler mc : offline) {
						System.out.println(mc.name);
					}

					Server.clients.remove(this);

					for (ClientHandler mc : Server.clients) {

						mc.output.writeUTF(this.name + " : " + "exited the chat");

					}

					this.isloggedin = false;

					break;
				}

				else if (received.contains("**")) {

					String user_id = received.substring(0, received.indexOf("**"));
					String leftover = received.substring(received.indexOf("**") + 2, received.length());

					for (ClientHandler mc : Server.clients) {
						if (user_id.equals(mc.name) & !this.name.equals(mc.name)) {

							mc.output.writeUTF(this.name + " : " + leftover);

						}
					}
				}

				else if (received.contains("*")) {

					String user_id = received.substring(received.indexOf("*"), received.length());
					

					for (ClientHandler mc : Server.clients) {
						if (user_id.equals(mc.name) & !this.name.equals(mc.name)) {

							mc.output.writeUTF(this.name + " : " + user_id + "*");

						}
					}
				}

				else {

					for (ClientHandler mc : Server.clients) {
						if (!this.name.equals(mc.name)) {

							mc.output.writeUTF(this.name + " : " + received);
							
						}

					}

				}

			} catch (IOException e) {

				break;
			}

		}
		closeports(input, output, socket);

	}

	/**
	 * @param input
	 * @param output
	 * @param socket
	 */
	public static void closeports(DataInputStream input, DataOutputStream output, Socket socket) {
		try {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
			socket.close();
		} catch (IOException ioe) {

		}

	}
}
