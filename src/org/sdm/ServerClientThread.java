package org.sdm;


import javax.xml.bind.DatatypeConverter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

class ServerClientThread extends Thread {
    Socket serverClient;

    ServerClientThread(Socket inSocket){
        serverClient = inSocket;
    }
    public void run(){
        try{
            DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
            DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
            String clientMessage="", serverMessage="";
            while(!clientMessage.equals("bye")){
                clientMessage = inStream.readUTF();
                if(clientMessage.equals("d")) {
                    int size = inStream.readInt();
                    byte[] buffer = new byte[size];
                    int res = inStream.read(buffer);
                    DiamondSpec d = DiamondSpec.deserialize(buffer);

                    System.out.println("From Client-: Message is :"+ DatatypeConverter.printHexBinary(buffer));
                } else {
                    System.out.println("From Client-: Message is :" + clientMessage);
                }
                //serverMessage="From Server to Client-" + clientMessage;
                //outStream.writeUTF(serverMessage);
                //outStream.flush();
            }
            inStream.close();
            outStream.close();
            serverClient.close();
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            System.out.println("Client - exit!! ");
        }
    }
}

