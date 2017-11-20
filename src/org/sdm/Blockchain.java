package org.sdm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class Blockchain {

	private List<Block> blockchain;

	public Blockchain() {
		this.blockchain = new ArrayList<>();
		blockchain.add(createGenesisBlock());
	}

	public synchronized Block generateNewBlock(Transaction t) {
		Block latest = getLatestBlock();
		int nextIndex = blockchain.size();
		long nextTimestamp = Instant.now().getEpochSecond();
		byte[] data = t.getTransactionBytes();
		return new Block(nextIndex, latest.getHash(), nextTimestamp, data);
	}

	public synchronized Block getLatestBlock() {
		return blockchain.get(blockchain.size() - 1);
	}

	private synchronized static Block createGenesisBlock() {
		long genesis = 1510398671L; //11-11-2017 11:11:11 GMT
		StringBuilder previousHash = new StringBuilder();
		for (int i = 0; i < 64; i++) {
			previousHash.append("0");
		}

		DiamondSpec d = new DiamondSpec(Instant.now().getEpochSecond(),
				-1,
				"",
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				-1,
				"",
				"",
				false);

		Transaction t = new Transaction(d, new byte[]{0});

		byte[] bytes = t.getTransactionBytes();

		return new Block(0, previousHash.toString(), genesis, bytes);
	}

	public synchronized boolean addBlock(Block candidate) {
		if (isValidBlock(candidate)) {
			blockchain.add(candidate);
			return true;
		} else {
			return false;
		}
	}

	public List<Block> getChain() {
		return blockchain;
	}

	public synchronized void replaceChain(List<Block> newChain) {
		if (!isValidChain(newChain)) throw new IllegalArgumentException("new chain is invalid.");
		if (blockchain.size() >= newChain.size()) throw new IllegalArgumentException("new chain not longer.");

		blockchain.clear();
		blockchain.addAll(newChain);
	}

	private boolean isValidChain(List<Block> chain) {
		Block genesisBlock = createGenesisBlock();
		Block firstBlock = chain.get(0);
		if (!firstBlock.equals(genesisBlock)) return false;

		for (int i = 1; i < chain.size(); i++) {
			Block prev = chain.get(i - 1);
			Block curr = chain.get(i);
			if (!isValidBlock(prev, curr)) return false;
		}

		return true;
	}

	private synchronized boolean isValidBlock(Block latest, Block candidate) {
		if (latest.getIndex() + 1 != candidate.getIndex()) {
			return false;
		} else if (!latest.getHash().equals(candidate.getPreviousHash())) {
			return false;
		} else if (!candidate.calculateHash().equals(candidate.getHash())) {
			return false;
		}

		return true;
	}

	private synchronized boolean isValidBlock(Block candidate) {
		return isValidBlock(getLatestBlock(), candidate);
	}

}
