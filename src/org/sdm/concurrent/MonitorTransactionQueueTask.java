package org.sdm.concurrent;

import org.sdm.Block;
import org.sdm.Node;
import org.sdm.Transaction;
import org.sdm.crypto.Encryption;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Deque;

/**
 * @author Mathew : 19/11/2017.
 */
public class MonitorTransactionQueueTask implements Runnable {

	private Node node;
	private Deque<Transaction> transactions;

	public MonitorTransactionQueueTask(Node node, Deque<Transaction> transactions) {
		this.node = node;
		this.transactions = transactions;
	}

	@Override
	public void run() {
		while (node.isListening()) {
			if (!transactions.isEmpty()) {
				Transaction t = transactions.poll();

				long hit = calculateHit();
				long target;
				long balance = node.getWallet().getOwnedDiamonds().size();

				while (node.isListening() && transactions.contains(t)) {
					long prevTimestamp = node.getBlockchain().getLatestBlock().getTimestamp();
					long currentSeconds = Instant.now().getEpochSecond();
					long timeSinceLastBlock = currentSeconds - prevTimestamp;

					target = balance * timeSinceLastBlock * 1000;    //TODO: improve calculation?

					if (hit < target) break;

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (!node.isListening()) break;
				if (!transactions.contains(t)) continue;    //TODO: does this work?

				Block b = node.getBlockchain().generateNewBlock(t);
				node.getBlockchain().addBlock(b);
				node.broadcastBlock(b);
				transactions.remove(t);
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private long calculateHit() {
		Block latestBlock = node.getBlockchain().getLatestBlock();
		byte[] hash = new BigInteger(latestBlock.getHash(), 16).toByteArray();
		byte[] signature = new Encryption().encrypt(node.getPublicKey(), hash);
		long hit = -1;

		try {
			byte[] val = MessageDigest.getInstance("SHA-256").digest(signature);
			byte[] first4 = Arrays.copyOfRange(val, 0, 4);
			double value = ByteBuffer.wrap(first4).getDouble();
			hit = new Double(value).longValue();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hit;
	}
}
