package org.sdm;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class Blockchain {

	private List<Block> blockchain;

	public Blockchain() {
		this.blockchain = new ArrayList<>();
		blockchain.add(createGenesysBlock());
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

	private static Block createGenesysBlock(){
		long now = Instant.now().getEpochSecond();
		return new Block(0,"Start",now,"GenesisBlock".getBytes());
	}

	public boolean addBlock(Block candidate){
		if(isValidBlock(candidate)){
			blockchain.add(candidate);
			return true;
		}
		else {return false;}
	}

	private boolean isValidBlock(Block candidate){
		if (getLatestBlock().getIndex()+1 != candidate.getIndex()){
			return false;
		}
		else if (!getLatestBlock().getHash().equals(candidate.getPreviousHash())){
			return false;
		}
		return true;
	}

}
