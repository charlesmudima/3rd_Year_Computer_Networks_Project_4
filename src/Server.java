
import java.io.*;
import java.net.*;
import java.util.*;

import javax.sound.sampled.AudioInputStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Server {

	static Vector<ClientHandler> clients = new Vector<>();
	private static int port = 4454;
	private static ServerSocket serverSocket;
	private static DataInputStream input;
	private static DataOutputStream output;
	static int clientsConnected = 0;


	/**
	 * @param port
	 * @throws IOException
	 */
	public static void receiveTCP(int port) throws IOException {

		ServerSocket serverSocket = new ServerSocket(port);
		Socket socket = serverSocket.accept();

		Scanner in = new Scanner(socket.getInputStream());
		PrintWriter printWrite = new PrintWriter(socket.getOutputStream(), true);
		String FileName = in.nextLine();
		int FileSize = in.nextInt();

		FileOutputStream fileoutput = new FileOutputStream(FileName);
		BufferedOutputStream out = new BufferedOutputStream(fileoutput);
		byte[] buffer = new byte[FileSize];
		int count;
		InputStream is = socket.getInputStream();
		while ((count = is.read(buffer, 0, FileSize)) > 0) {

			fileoutput.write(buffer, 0, count);
		}

		receiveFileGUI(FileName);
		closeeverything(fileoutput, socket, out, serverSocket, printWrite);

	}

	private static void closeeverything(FileOutputStream fileoutput, Socket socket, BufferedOutputStream out,
			ServerSocket serverSocket, PrintWriter printWrite) throws IOException {
		fileoutput.close();
		socket.close();
		serverSocket.close();
		out.close();
		printWrite.close();
	}

	private static void receiveFileGUI(String name) {
		JOptionPane.showMessageDialog(null, name, "FILE RECEIVED", JOptionPane.INFORMATION_MESSAGE);
	}

	private static void connectClients(ServerSocket clientSocket) throws IOException {

		Socket s = clientSocket.accept();

		System.out.println("A new client has connected to this [server]");

		input = new DataInputStream(s.getInputStream());
		output = new DataOutputStream(s.getOutputStream());
		ClientHandler mtch = new ClientHandler(s, "clients", input, output);

		clienthandlermethod(mtch);

	}

	private static void clienthandlermethod(ClientHandler mtch) {

		Thread thread = new Thread(mtch);

		clients.add(mtch);

		thread.start();

		clientsConnected++;

		System.out.println("Currently there are: " + clientsConnected + " clients connected");
		System.out.println("Updated list of people online");
		for (ClientHandler mcc : clients) {
			System.out.println(mcc.name);

		}
	}

	public static void main(String[] args) throws IOException {

		serverSocket = new ServerSocket(port);

		String message = "READY FOR CLIENTS TO START RECORDING" + port;
		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.INFORMATION_MESSAGE);

		while (true) {

			connectClients(serverSocket);


		}

	}

}
