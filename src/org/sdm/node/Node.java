package org.sdm.node;

import org.sdm.blockchain.Block;
import org.sdm.blockchain.Blockchain;
import org.sdm.blockchain.DiamondSpec;
import org.sdm.blockchain.Transaction;
import org.sdm.concurrent.ConcurrentArrayList;
import org.sdm.concurrent.ForgeTask;
import org.sdm.concurrent.ListenForMessagesTask;
import org.sdm.concurrent.ListenForNodesTask;
import org.sdm.crypto.Encryption;
import org.sdm.crypto.Signer;
import org.sdm.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mathew : 11/11/2017.
 */
public class Node {

	private final ExecutorService pool;
	private final Map<String, NodeSocket> nodes;
	private AtomicBoolean isListening;
	private Deque<Transaction> pendingTransactions;

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private byte[] serverToken;
	private PublicKey serverPublicKey;

	private byte[] address;
	private Wallet wallet;

	private Blockchain blockchain;
	private ConcurrentArrayList<Transaction> unspent;

	private NodeSocket server;

	private final Object lock = new Object();

	public Node(int port) {
		this.nodes = new ConcurrentHashMap<>();
		this.pool = Executors.newCachedThreadPool();
		this.isListening = new AtomicBoolean(true);
		this.pendingTransactions = new ConcurrentLinkedDeque<>();
		this.blockchain = new Blockchain();
		this.unspent = new ConcurrentArrayList<>();

		generateKeys();
		generateAddress();
		connectToServer();


		listenForNodes(port);
		monitorTransactions();

		this.wallet = new Wallet(this, this.publicKey, this.privateKey);

		Runtime.getRuntime().addShutdownHook(new Thread(pool::shutdown));
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

	private void connectToServer() {
		try {
			Socket s = new Socket("localhost", 9999);
			this.server = new NodeSocket(s);

			ObjectInputStream inputs = this.server.getObjectInputStream();
			ObjectOutputStream outputs = this.server.getObjectOutputStream();

			Message msg = new Message("id", this.address);
			outputs.writeObject(msg);

			msg = new Message("register", this.publicKey);
			outputs.writeObject(msg);

			msg = (Message) inputs.readObject();
			ArrayList<Object> list = (ArrayList<Object>) msg.getObject();
			this.serverPublicKey = (PublicKey) list.get(0);
			this.serverToken = (byte[]) list.get(1);
			System.out.println("Got server token");

			new Thread(new ListenForMessagesTask(this, server)).start();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void generateAddress() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			this.address = digest.digest(publicKey.getEncoded());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public void connectToNode(int port) {
		try {
			Socket socket = new Socket("localhost", port);
			NodeSocket nodeSocket = new NodeSocket(socket);
			int id = nodes.keySet().size() + 1;
			nodes.put(String.valueOf(id), nodeSocket);
			Message msg = new Message("id", Integer.toString(id));
			nodeSocket.getObjectOutputStream().writeObject(msg);
			System.out.println("connected to port " + port);
			pool.execute(new ListenForMessagesTask(this, nodeSocket));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createDiamond(DiamondSpec diamond) {
		if (!validDiamond(diamond)) return;
		ArrayList<Object> list = new ArrayList<>(2);
		list.add(Base64.getEncoder().encodeToString(this.address));
		list.add(diamond);
		Message msg = new Message("diamond", list);
		ObjectOutputStream oos = this.server.getObjectOutputStream();
		try {
			oos.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean validDiamond(DiamondSpec diamond) {
		for (Block block : blockchain.getChain()) {
			DiamondSpec d = Transaction.deserialize(block.getData()).getDiamond();
			if (diamond.equals(d)) return false;
		}
		return true;
	}

	public void sendDiamond(Transaction t, byte[] address) {
		Transaction transaction = new Transaction(t.getDiamond(), address, this.publicKey, this.serverToken, t.getPreviousTransaction());
		transaction.sign(this.privateKey);
		processNewTransaction(t);
		broadcastTransaction(transaction);
	}

	private void broadcastTransaction(Transaction transaction) {
		Message msg = new Message("tx", transaction);
		for (NodeSocket socket : nodes.values()) {
			try {
				socket.getObjectOutputStream().writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void broadcastChain(List<Block> chain) {
		Message msg = new Message("chain", chain);
		for (NodeSocket socket : nodes.values()) {
			try {
				socket.getObjectOutputStream().writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void broadcastBlock(Block block) {
		ArrayList<Block> list = new ArrayList<>(1);
		list.add(block);
		broadcastChain(list);
	}

	public void queryBlockchain() {
		Message msg = new Message("query", null);
		for (NodeSocket socket : nodes.values()) {
			try {
				socket.getObjectOutputStream().writeObject(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void listenForNodes(int port) {
		pool.execute(new ListenForNodesTask(this, port, pool, nodes));
	}

	private void monitorTransactions() {
		pool.execute(new ForgeTask(this, pendingTransactions));
	}

	public void processNewTransaction(Transaction t) {
		Signer signer = new Signer();
		if (signer.verifySignature(this.serverPublicKey, t.getPublicKey().getEncoded(), t.getServerToken())) {
			unspent.add(t);
			pendingTransactions.add(t);
//			broadcastTransaction(t);
			addToWallet(t);
			return;
		}

		if (signer.verifySignature(serverPublicKey, t.getPublicKey().getEncoded(), t.getServerToken())) {
			return;
		}

		for (Transaction tx : pendingTransactions) {
			if (tx.equals(t)) return;    //check if tx is already pending
			if (tx.getDiamond().equals(t.getDiamond()))
				return;    //check if diamond is already in pending tx (tx not confirmed yet)
			if (Arrays.equals(tx.getPreviousTransaction(), t.getPreviousTransaction())) return;
		}

		//check if tx already exists in blockchain
		for (Block block : blockchain.getChain()) {
			Transaction tr = Transaction.deserialize(block.getData());
			if (tr.equals(t)) return;
			if (Arrays.equals(tr.getPreviousTransaction(), t.getPreviousTransaction())) return;
		}

		//check if tx has already been spent
		boolean unspent = false;
		Iterator<Transaction> i = this.unspent.iterator();
		while (i.hasNext()) {
			if (i.next().equals(t)) {
				unspent = true;
			}
		}

		if (!unspent) return;

		pendingTransactions.add(t);
		addToWallet(t);
//		broadcastTransaction(t);
	}

	public void processNewChain(List<Block> chain) {
		synchronized (lock) {
			if (chain.size() < 1) throw new IllegalArgumentException("list must not be empty");
			Block latestBlockReceived = chain.get(chain.size() - 1);
			Block latestBlock = blockchain.getLatestBlock();

			if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {

				if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {

					if (!validHit(latestBlockReceived)) return;

					blockchain.addBlock(latestBlockReceived);
					Transaction t = Transaction.deserialize(latestBlockReceived.getData());
					pendingTransactions.remove(t);

					ArrayList<Block> latest = new ArrayList<>(1);
					latest.add(latestBlockReceived);
					addToWallet(t);
					broadcastChain(latest);
				} else if (chain.size() == 1) {
					queryBlockchain();
				} else {
					blockchain.replaceChain(chain);
					wallet.computeOwnedTransactions();
				}

			}
		}
	}

	public boolean validHit(Block block) {
		long balance = blockchain.getBalanceByPublicKey(block.getPublicKey());
		long prevTimestamp = blockchain.getLatestBlock().getTimestamp();
		long currentSeconds = Instant.now().getEpochSecond();
		long timeSinceLastBlock = currentSeconds - prevTimestamp;
		long estimatedTarget = balance * timeSinceLastBlock * 1000;    //TODO: improve calculation???
		long hit = calculateHit(block);

		return hit < estimatedTarget - 6000;    //TODO: how much extra time to approve??
	}

	public long calculateHit(Block block) {
		byte[] hash = new BigInteger(block.getHash(), 16).toByteArray();
		byte[] signature = new Encryption().encrypt(this.getPublicKey(), hash);
		long hit = -1;

		try {
			byte[] val = MessageDigest.getInstance("SHA-256").digest(signature);
			byte[] first8 = Arrays.copyOfRange(val, 0, 8);
			ByteBuffer buffer = ByteBuffer.wrap(first8);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			hit = buffer.getLong();
			hit = (long) (hit % Math.pow(10, 6));
			hit = Math.abs(hit);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hit;
	}

	private void addToWallet(Transaction t) {
		//add to wallet if mine and not yet in wallet
		if (Arrays.equals(this.address, t.getDestinationAddress()))
			if (!wallet.contains(t))
				wallet.getOwnedTransactions().add(t);
	}

	public boolean isListening() {
		return this.isListening.get();
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public byte[] getAddress() {
		return address;
	}

	public String getAddressBase64() {
		return Base64.getEncoder().encodeToString(address);
	}

	public Wallet getWallet() {
		return wallet;
	}

	public Blockchain getBlockchain() {
		return blockchain;
	}

	public byte[] getServerToken() {
		return this.serverToken;
	}

	public ConcurrentArrayList<Transaction> getUnspent() {
		return unspent;
	}

	public void shutdown() {
		isListening.set(false);
		pool.shutdown();
		for (NodeSocket socket : nodes.values()) {
			try {
				socket.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public NodeSocket getServer() {
		return server;
	}

}
