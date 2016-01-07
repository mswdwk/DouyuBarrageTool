package controller;

import domain.ServerConfig;
import service.RoomInformation;
import service.SendMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * createdTime 2016/1/6
 *
 * @author ndrlslz
 */
public class GroupIdThread implements Runnable {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private RoomInformation roomInformation;

    public GroupIdThread(RoomInformation roomInformation) throws IOException {
        socket = new Socket(roomInformation.getServerConfig().getIp(), Integer.parseInt(roomInformation.getServerConfig().getPort()));
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        this.roomInformation = roomInformation;
    }

    @Override
    public void run() {
        SendMessage sendMessage = new SendMessage(outputStream);
        try {
            sendMessage.sendReq();

            receive(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void receive(InputStream inputStream) throws IOException {
        int i;
        byte[] bytes = new byte[1024];
        while (socket != null && socket.isConnected() && (i = inputStream.read(bytes)) != -1) {
            String REGEX = "type@=setmsggroup/.*/gid@=(.*?)/";
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(new String(bytes, 0, i));

            if (matcher.find()) {
                new RoomInformation().get(matcher.group(1));
                roomInformation.setGid(matcher.group(1));
                break;
            }
        }
    }

}
