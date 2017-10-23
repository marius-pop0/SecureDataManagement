package org.sdm;


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
                clientMessage=inStream.readUTF();
                System.out.println("From Client-: Message is :"+clientMessage);
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

