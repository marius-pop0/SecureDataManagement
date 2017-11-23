package org.sdm.concurrent;

import org.sdm.Block;
import org.sdm.Node;
import org.sdm.NodeSocket;
import org.sdm.Transaction;
import org.sdm.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * @author Mathew : 19/11/2017.
 */
public class ListenForMessagesTask implements Runnable {

	private Node node;
	private NodeSocket socket;

	public ListenForMessagesTask(Node node, NodeSocket socket) {
		this.node = node;
		this.socket = socket;
	}

	@Override
	public void run() {
		ObjectInputStream ois = socket.getObjectInputStream();
		while (node.isListening()) {
			try {
				Message msg = (Message) ois.readObject();
				String type = msg.getType();
				switch (type) {
					case "tx": //transaction
						System.out.println("received transaction");
						Transaction t = (Transaction) msg.getObject();
						node.processNewTransaction(t);
						break;
					case "chain":
						List<Block> chain = (List<Block>) msg.getObject();
						node.processNewChain(chain);
					case "query":
						List<Block> currentChain = node.getBlockchain().getChain();
						Message msgToSend = new Message("chain", currentChain);
						socket.getObjectOutputStream().writeObject(msgToSend);
						break;
					default:
						break;
				}
			} catch (SocketTimeoutException e) {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				break;
			}
		}
	}

}
