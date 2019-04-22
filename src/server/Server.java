package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(8080)) {
            System.out.println("Сервер запущен");
            for(;;) {
                Socket socket = server.accept();
                try {
                    new ServerThread(socket);
                } catch (IOException e) {
                    socket.close();
                }
            }
        }
    }
}
