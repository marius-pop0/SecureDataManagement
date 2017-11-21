package org.sdm;

import org.sdm.concurrent.ListenForNodesTask;
import org.sdm.concurrent.MonitorTransactionQueueTask;
import org.sdm.message.Message;

import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
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

	private byte[] address;
	private Wallet wallet;

	private Blockchain blockchain;

	private final Object lock = new Object();

	public Node(int port) {
		this.nodes = new ConcurrentHashMap<>();
		this.pool = Executors.newCachedThreadPool();
		this.isListening = new AtomicBoolean(true);
		this.pendingTransactions = new ConcurrentLinkedDeque<>();
		this.blockchain = new Blockchain();

		generateKeys();
		generateAddress();

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

		//TODO: announce public key to server
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
			//TODO: ADDRESS!!!!! ALSO WALLET!!!
			nodes.put("address", nodeSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDiamond(DiamondSpec diamond, byte[] address) {
		Transaction transaction = new Transaction(diamond, address);
		broadcastTransaction(transaction);
	}

	private void broadcastTransaction(Transaction transaction) {
		transaction.sign(this.privateKey);
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

	private void queryBlockchain() {
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
		pool.execute(() -> new ListenForNodesTask(this, port, pool, nodes));
	}

	private void monitorTransactions() {
		pool.execute(() -> new MonitorTransactionQueueTask(this, pendingTransactions));
	}

	public void processNewTransaction(Transaction t) {
		for (Transaction tx : pendingTransactions) {
			if (tx.equals(t)) return;
			if (tx.getDiamond().equals(t.getDiamond())) return;
			//TODO: For each input, if the referenced output exists in any other tx in the pool, reject this transaction
			//TODO: rest of transaction checks
		}

		pendingTransactions.add(t);
//		broadcastTransaction(t);	//TODO: necessary?
	}

	public void processNewChain(List<Block> chain) {
		synchronized (lock) {
			if (chain.size() < 1) throw new IllegalArgumentException("list must not be empty");
			Block latestBlockReceived = chain.get(chain.size() - 1);
			Block latestBlock = blockchain.getLatestBlock();

			if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {

				if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
					blockchain.addBlock(latestBlockReceived);
					Transaction t = Transaction.deserialize(latestBlockReceived.getData());
					pendingTransactions.remove(t);

					ArrayList<Block> latest = new ArrayList<>(1);
					latest.add(latestBlockReceived);
					broadcastChain(latest);
				} else if (chain.size() == 1) {
					queryBlockchain();
				} else {
					blockchain.replaceChain(chain);
				}

			}
		}
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

}
