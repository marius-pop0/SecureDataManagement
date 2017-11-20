package org.sdm.concurrent;

import org.sdm.Node;
import org.sdm.NodeSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Mathew : 11/11/2017.
 */
public class ListenForNodesTask implements Runnable {

	private Node node;
	private ServerSocket serverSocket;
	private Map<String, NodeSocket> nodes;
	private ExecutorService pool;

	public ListenForNodesTask(Node node, int port, ExecutorService pool, Map<String, NodeSocket> nodes) {
		this.node = node;
		this.pool = pool;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		this.nodes = nodes;
	}

	@Override
	public void run() {
		Socket socket;
		InputStream in;
		BufferedReader reader;
		while (this.node.isListening()) {
			try {
				socket = serverSocket.accept();
				NodeSocket nodeSocket = new NodeSocket(socket);
				in = socket.getInputStream();
				reader = new BufferedReader(new InputStreamReader(in));

				String id = reader.readLine();
				nodes.put(id, nodeSocket);
				System.out.println(id + " CONNECTED");

				pool.execute(new ListenForMessagesTask(node, nodeSocket));
			} catch (SocketTimeoutException e) {
				System.out.println("timeout");
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}

		for (NodeSocket node : nodes.values()) {
			try {
				node.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
