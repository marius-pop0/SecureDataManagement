package org.sdm;


import org.sdm.crypto.Signer;
import org.sdm.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Server {

	private ServerSocket serverSocket;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private ConcurrentHashMap<byte[], NodeSocket> nodes;

	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.nodes = new ConcurrentHashMap<>();

		generateKeys();
		listenForNodes();
	}

	private void generateKeys() {
		ECGenParameterSpec paramSpec = new ECGenParameterSpec("secp256k1");
		KeyPairGenerator keygen = null;
		try {
			keygen = KeyPairGenerator.getInstance("ECDSA", "BC");
			keygen.initialize(paramSpec, new SecureRandom());
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
			e.printStackTrace();
		}

		assert keygen != null;
		KeyPair pair = keygen.generateKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	private void listenForNodes() {
		new Thread(() -> {
			Socket socket;
			ObjectInputStream in;
			while (true) {
				try {
					socket = serverSocket.accept();
					NodeSocket nodeSocket = new NodeSocket(socket);
					in = nodeSocket.getObjectInputStream();
					Message msg = (Message) in.readObject();
					byte[] id = (byte[]) msg.getObject();
					nodes.put(id, nodeSocket);
					System.out.println("SERVER: NODE CONNECTED");
					new Thread(new ListenForMessages(nodeSocket)).start();
				} catch (SocketTimeoutException e) {
					System.out.println("timeout");
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (IOException | ClassNotFoundException e) {
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
		}).start();
	}

	private class ListenForMessages implements Runnable {
		private NodeSocket socket;

		public ListenForMessages(NodeSocket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				ObjectInputStream inStream = socket.getObjectInputStream();
				ObjectOutputStream outStream = socket.getObjectOutputStream();
				Message msg;

				label:
				while (true) {
					msg = (Message) inStream.readObject();
					ArrayList<Object> list;
					switch (msg.getType()) {
						case "diamond":
							list = (ArrayList<Object>) msg.getObject();
							byte[] address = Base64.getDecoder().decode((String) list.get(0));
							DiamondSpec diamond = (DiamondSpec) list.get(1);
							broadcastDiamond(diamond, address);
							System.out.println("SERVER: adding diamond");
							break;
						case "register":
							PublicKey userkey = (PublicKey) msg.getObject();
							Signer signer = new Signer();
							byte[] token = signer.generateSignature(privateKey, userkey.getEncoded());
							list = new ArrayList<>(2);
							list.add(publicKey);
							list.add(token);
							Message server_message = new Message("token", list);
							outStream.writeObject(server_message);
							break;
						case "bye":
							break label;
						default:
							break;
					}
					//serverMessage="From Server to Client-" + clientMessage;
					//outStream.writeUTF(serverMessage);
					//outStream.flush();
				}

				inStream.close();
				outStream.close();
				serverSocket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				System.out.println("SERVER - exit!");
			}
		}
	}

	private void broadcastDiamond(DiamondSpec diamond, byte[] address) {
		Signer signer = new Signer();
		byte[] token = signer.generateSignature(this.privateKey, this.publicKey.getEncoded());
		Transaction t = new Transaction(diamond, address, this.publicKey, token, null);
		try {
			for (Map.Entry entry : nodes.entrySet()) {
				Message msg = new Message("tx", t);
				NodeSocket node = (NodeSocket) entry.getValue();
				node.getObjectOutputStream().writeObject(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
