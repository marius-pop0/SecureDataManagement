package org.sdm.node;

import org.sdm.blockchain.Block;
import org.sdm.blockchain.DiamondSpec;
import org.sdm.blockchain.Transaction;
import org.sdm.crypto.Signer;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mathew : 21/11/2017.
 */
public class Wallet {

	private Node node;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private List<Transaction> balance;

	public Wallet(Node node, PublicKey publicKey, PrivateKey privateKey) {
		this.node = node;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.balance = new ArrayList<>();
	}

	public synchronized List<Transaction> computeOwnedTransactions() {
		Signer signer = new Signer();
		List<Block> chain = node.getBlockchain().getChain();
		List<Transaction> owned = new ArrayList<>();
		List<DiamondSpec> sent = new ArrayList<>();
		//traverse chain in reverse order
		//if tx sig matches my private key, tx was mine
		//balance is # of diamonds sent to me, that are not contained in txs signed be me
		for (int i = chain.size() - 1; i >= 0; i--) {
			Block block = chain.get(i);
			Transaction t = Transaction.deserialize(block.getData());
			byte[] sig = t.getSignature();
			boolean sigMatch = signer.verifySignature(publicKey, t.getData(), sig);
			if (sigMatch) {
				sent.add(t.getDiamond());
			}

			if (Arrays.equals(node.getAddress(), t.getDestinationAddress()) &&
					!sent.contains(t.getDiamond())) {
				owned.add(t);
			}
		}

		this.balance = owned;
		return owned;
	}

	public synchronized List<Transaction> getOwnedTransactions() {
		if (balance == null) {
			return computeOwnedTransactions();
		}

		return balance;
	}

	public synchronized void add(Transaction t) {
		balance.add(t);
	}

	public synchronized boolean contains(Transaction t) {
		return balance.contains(t);
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}