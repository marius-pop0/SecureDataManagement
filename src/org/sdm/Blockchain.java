package org.sdm;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mathew : 5/10/2017.
 */
public class Blockchain {

	private List<Block> blockchain;

	public Blockchain() {
		this.blockchain = new ArrayList<>();
	}

	public Block generateNewBlock(DiamondSpec d) throws IOException {
		Block latest = getLatestBlock();
		int nextIndex = blockchain.size();
		long nextTimestamp = Instant.now().getEpochSecond();
		byte[] data = d.getDiamondBytes();
		Block next = new Block(nextIndex, latest.getHash(), nextTimestamp, data);
		return next;
	}

	private Block getLatestBlock() {
		return blockchain.get(blockchain.size() - 1);
	}

}
