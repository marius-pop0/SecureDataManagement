package org.sdm.concurrent;

import org.sdm.Block;
import org.sdm.Node;
import org.sdm.Transaction;

import java.time.Instant;
import java.util.Deque;

/**
 * @author Mathew : 19/11/2017.
 */
public class ForgeTask implements Runnable {

	private Node node;
	private Deque<Transaction> transactions;

	public ForgeTask(Node node, Deque<Transaction> transactions) {
		this.node = node;
		this.transactions = transactions;
	}

	@Override
	public void run() {
		while (node.isListening()) {
			if (!transactions.isEmpty()) {
				Transaction t = transactions.peek();

				long hit = node.calculateHit(node.getBlockchain().getLatestBlock());
				long target;
				long balance = node.getWallet().getOwnedTransactions().size();

				while (node.isListening() && transactions.contains(t)) {
					long prevTimestamp = node.getBlockchain().getLatestBlock().getTimestamp();
					long currentSeconds = Instant.now().getEpochSecond();
					long timeSinceLastBlock = currentSeconds - prevTimestamp;

					target = (balance + 1) * timeSinceLastBlock * 5000;    //TODO: improve calculation?

					System.out.println(hit + "  --  " + target);
					if (hit < target) break;

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (!node.isListening()) break;
				if (!transactions.contains(t)) continue;    //TODO: does this work?

				Block b = node.getBlockchain().generateNewBlock(node, t);
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

}
