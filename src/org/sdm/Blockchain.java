package org.sdm;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class Blockchain {

	private List<Block> blockchain;
	private final static Blockchain INSTANCE = new Blockchain();

	private Blockchain() {
		this.blockchain = new ArrayList<>();
		blockchain.add(createGenesisBlock());
	}

	public static Blockchain getInstance() {
		return INSTANCE;
	}

	public Block generateNewBlock(DiamondSpec d) throws IOException {
		Block latest = getLatestBlock();
		int nextIndex = blockchain.size();
		long nextTimestamp = Instant.now().getEpochSecond();
		byte[] data = d.getDiamondBytes();
		Block next = new Block(nextIndex, latest.getHash(), nextTimestamp, data);
		return next;
	}

	public Block getLatestBlock() {
		return blockchain.get(blockchain.size() - 1);
	}

	private static Block createGenesisBlock() {
		long now = Instant.now().getEpochSecond();
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

		byte[] bytes = null;

		try {
			bytes = d.getDiamondBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Block(0, previousHash.toString(), now, bytes);
	}

	public boolean addBlock(Block candidate) {
		if (isValidBlock(candidate)) {
			blockchain.add(candidate);
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidBlock(Block candidate) {
		if (getLatestBlock().getIndex() + 1 != candidate.getIndex()) {
			return false;
		} else if (!getLatestBlock().getHash().equals(candidate.getPreviousHash())) {
			return false;
		} else if (!candidate.calculateHash().equals(candidate.getHash())) {
			return false;
		}

		return true;
	}

}
