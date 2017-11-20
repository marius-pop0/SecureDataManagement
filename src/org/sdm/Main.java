package org.sdm;

import com.sun.security.ntlm.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class Main {

	public static void main(String[] args) {
		Blockchain blockchain = new Blockchain();

		DiamondSpec d = new DiamondSpec(Instant.now().getEpochSecond(),
				1,
				"round",
				1.22,
				0.23,
				.55,
				1.22,
				.98,
				2,
				2,
				2,
				4,
				2,
				4,
				"First Diamond",
				"Canada,",
				true);

		Transaction t = new Transaction(d, null);

		boolean valid = false;
		while (!valid) {
			Block b = blockchain.generateNewBlock(t);
			valid = blockchain.addBlock(b);
		}

		try{
			ServerSocket server = new ServerSocket(9999);
			while (true){

				Socket serverClient = server.accept();
				ServerClientThread connection = new ServerClientThread(serverClient);
				connection.start();

			}

		}catch (Exception e){
			System.err.println(e);
		}


	}

}
