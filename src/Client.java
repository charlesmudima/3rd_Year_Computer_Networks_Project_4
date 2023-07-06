import javax.sound.sampled.*;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.Font;
import java.io.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;
import java.awt.Component;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

public class Client {

    final static int ServerPort = 4454;
    private DataOutputStream dataoutput;
    private DataInputStream datainput;
    private Socket socket;
    static final long RECORD_TIME = 10000; // 1 minute
    static File wavFile = new File("RecordAudio.wav1");
    private static final int BUFFER_SIZE = 4096;
    static AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    static TargetDataLine line;

    static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    public Client(Socket socket) throws IOException {

        this.dataoutput = new DataOutputStream(socket.getOutputStream());
        this.datainput = new DataInputStream(socket.getInputStream());
        this.socket = socket;

    }

    /**
     * @param host
     * @param port
     */
    private static void sendTCP(InetAddress host, int port, Socket sock) {
        try {
            // Socket sock = new Socket(host, port);
            String fileName;
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jfc.isMultiSelectionEnabled()) {
                jfc.setMultiSelectionEnabled(false);
            }
            int r = jfc.showOpenDialog(null);

            if (r == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                fileName = file.getName();
                int FileSize = (int) file.length();
                PrintWriter pr = new PrintWriter(sock.getOutputStream(), true);
                Scanner in = new Scanner(sock.getInputStream());

                pr.println(fileName);
                pr.println(FileSize);

                int count;
                byte[] buffer = new byte[FileSize];
                OutputStream out = sock.getOutputStream();
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
                // printProgress();

                while ((count = input.read(buffer)) > 0) {

                    out.write(buffer, 0, count);
                    out.flush();

                }
                input.close();
                sock.close();
                in.close();

            }

        } catch (Exception e) {
            System.out.println("IOException Error on SendTCP ");
        }

    }

    private static void choosefiletosend(int port, InetAddress host, Socket sock) {
        JFrame jframe = new JFrame("FILE SENDER");
        jframe.setSize(450, 450);
        jframe.setLayout(new BoxLayout(jframe.getContentPane(), BoxLayout.Y_AXIS));
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel jtitle = new JLabel("Options");
        jtitle.setFont(new Font("Arial", Font.BOLD, 25));
        jtitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel jbutton = new JPanel();

        JButton jbsendtpc = new JButton("TPC");
        jbsendtpc.setPreferredSize(new Dimension(250, 75));
        jbsendtpc.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jbsendvc = new JButton("RECORD");
        jbsendvc.setPreferredSize(new Dimension(250, 75));
        jbsendvc.setFont(new Font("Arial", Font.BOLD, 20));

        JButton jclose = new JButton("EXIT");
        jclose.setPreferredSize(new Dimension(150, 75));
        jclose.setFont(new Font("Arial", Font.BOLD, 20));

        jbutton.add(jbsendtpc);
        jbutton.add(jclose);
        jbutton.add(jbsendvc);
        jclose.addActionListener((event) -> System.exit(0));

        jclose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("You have exited!");
            }
        });

        jbsendtpc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                sendTCP(host, port, sock);
            }
        });

        jbsendvc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                start();
            }
        });

        jframe.add(jtitle);

        jframe.add(jbutton);
        jframe.setVisible(true);
    }

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

    public void start_connection(String serverAddress, int ServerPort) {

        try {

            Scanner scn = new Scanner(System.in);
            String user_ID = "";
            System.out.print("Enter your name : ");
            user_ID = scn.nextLine();
            dataoutput.writeUTF(user_ID);
            System.out.println("Welcome " + user_ID + ", You may start typing your text now!!");

            Thread message_sent = new Thread(new Runnable() {
                @Override
                public void run() {

                    String message_to_server = "";
                    while (!message_to_server.equals("exit")) {

                        message_to_server = scn.nextLine();

                        if (message_to_server.equals("record")) {

                            Thread stopper = new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(RECORD_TIME);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                    finish();
                                }
                            });

                            stopper.start();

                            start();

                        }

                        if (message_to_server.equals("play")) {
                            int num = 0;
                            num++;
                            String audioFilePath = "RecordAudio.wav" + Integer.toString(num);
                            play(audioFilePath);

                        }

                        try {

                            dataoutput.writeUTF(message_to_server);

                        } catch (IOException e) {
                            System.out.println("Message is equal to exit, could not write");

                            try {
                                dataoutput.close();
                                scn.close();

                            } catch (Exception s) {
                                s.printStackTrace();
                            }

                        }

                    }
                }
            });

            Thread message_received = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        try {
                            String message_from_server;

                            message_from_server = datainput.readUTF();

                            if (message_from_server.contains("*")) {

                                play(message_from_server);

                            }

                            if (message_from_server.equals("exit")) {
                                System.out.println("A client has left the chat");

                                try {
                                    datainput.close();
                                    scn.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                break;
                            }
                            System.out.println(message_from_server);

                        } catch (IOException e) {
                            System.out.println("You have left the chat!");

                            break;
                        }
                    }
                }
            });
            message_sent.start();
            message_received.start();

        } catch (Exception e) {
            System.out.println("Error in connection to the server");

        }

    }

    /**
     * @return
     */
    public static String start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");

            AudioSystem.write(ais, fileType, wavFile);

            Path path = wavFile.toPath();
            String filestring = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            String header = "*";
            String finalstring = header + filestring;
            return finalstring;

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;

    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    void finish() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }

    /**
     * @param audioFilePath
     */
    void play(String audioFilePath) {

        File audioFile = new File(audioFilePath);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            AudioFormat format = audioStream.getFormat();

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);

            audioLine.open(format);

            audioLine.start();

            System.out.println("Playback started.");

            byte[] bytesBuffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;

            while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
                audioLine.write(bytesBuffer, 0, bytesRead);
            }

            audioLine.drain();
            audioLine.close();
            audioStream.close();

            System.out.println("Playback completed.");

        } catch (UnsupportedAudioFileException ex) {
            System.out.println("The specified audio file is not supported.");
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            System.out.println("Audio line for playing back is unavailable.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Error playing the audio file.");
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) throws UnknownHostException, IOException

    {
        InetAddress ip = InetAddress.getByName("localhost");
        Socket socket = new Socket(ip, ServerPort);
        Client c = new Client(socket);
        c.start_connection("localhost", Client.ServerPort);

        Client.choosefiletosend(Client.ServerPort, ip, socket);

    }
}
