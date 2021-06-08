import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

public class Server extends JFrame {
    JTextField txt;
    JFrame frame;
    JLabel title;
    Font font;

    Server() {
        frame = new JFrame("JFrameWindowListener");
        frame.addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent event) {
            }

            public void windowClosed(WindowEvent event) {
            }

            public void windowDeactivated(WindowEvent event) {
            }

            public void windowDeiconified(WindowEvent event) {
            }

            public void windowIconified(WindowEvent event) {
            }

            public void windowOpened(WindowEvent event) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        font = new Font("Roboto", Font.BOLD, 60);
        title = new JLabel("Server");
        title.setFont(font);
        title.setBounds(300, 50, 400, 50);

        txt = new JTextField();
        txt.setBounds(400, 450, 500, 50);
        frame.add(txt);

        frame.getContentPane().add(title);
        frame.setPreferredSize(new Dimension(900, 800));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new Server();
        ServerSocket welcomeSocket = new ServerSocket(3333);
        System.out.println("Server waiting for connection...");
        int count = 0;
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("Client №" + (++count) + " accepts.");
            run(connectionSocket, args[0]);
        }
    }

    public static void run(Socket connectionSocket, String dirName) {
        String name;
        String filename;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            OutputStream output = connectionSocket.getOutputStream();

            ObjectOutputStream oout = new ObjectOutputStream(output);
            oout.writeObject("Server says Hi!");
            oout.writeObject(dirName);

            File ff = new File(dirName);
            ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
            int count = 0;
            for (String name_n : names) {
                File file = new File(dirName + name_n);
                if (file.isFile() && !name_n.substring(name_n.length() - 3, name_n.length() - 1).equals("rar")) count++;
            }
            oout.writeObject(String.valueOf(count));

            for (String name_n : names) {
                File file = new File(dirName + name_n);
                if (file.isFile() && !name_n.substring(name_n.length() - 3, name_n.length() - 1).equals("rar"))
                    oout.writeObject(name_n);
            }

            name = in.readLine();
            filename = name;
            FileInputStream file = null;
            ZipInputStream zos;
            BufferedInputStream bis = null;
            boolean fileExists = true;

            //System.out.println(in.readLine() + " is downloaded from server");
            //System.out.println("Request to download file " + filename + " recieved from " + connectionSocket.getInetAddress().getHostName() + "...");

            filename = dirName + filename;
            try {

                file = new FileInputStream(filename);
                if (name.substring(name.length() - 3, name.length() - 1).equals("zip")) {
                    zos = new ZipInputStream(file);
                    bis = new BufferedInputStream(zos);

                } else {
                    bis = new BufferedInputStream(file);
                }
            } catch (FileNotFoundException excep) {
                fileExists = false;
                System.out.println("SERVERFileNotFoundException:" + excep.getMessage());
            }
            //oout = new ObjectOutputStream(output);
            if (fileExists) {
                oout.writeObject("Success");
                //System.out.println("Download begins");
                writeFile(bis, output);
                //System.out.println("Completed");

            } else {
                oout.writeObject("File Not Found");
            }
            assert bis != null;
            bis.close();
            file.close();
            oout.close();
            output.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void writeFile(BufferedInputStream in, OutputStream out) throws Exception {
        int size = 9022386;
        byte[] data = new byte[size];
        int c = in.read(data, 0, data.length);
        out.write(data, 0, c);
        out.flush();
    }
}