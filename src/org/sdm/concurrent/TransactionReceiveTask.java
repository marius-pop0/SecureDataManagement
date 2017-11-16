package org.sdm.concurrent;

import org.sdm.Node;
import org.sdm.NodeSocket;
import org.sdm.Transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;

/**
 * @author Mathew : 11/11/2017.
 */
public class TransactionReceiveTask implements Runnable {

	private Node node;
	private NodeSocket socket;

	public TransactionReceiveTask(Node node, NodeSocket socket) {
		this.node = node;
		this.socket = socket;
	}

	@Override
	public void run() {
		ObjectInputStream ois = socket.getObjectInputStream();
		while (true) {
			try {
				Transaction transaction = (Transaction) ois.readObject();
				node.receiveTransaction(transaction);
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
