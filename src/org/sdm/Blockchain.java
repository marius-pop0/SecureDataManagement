package org.sdm;

import org.bouncycastle.jce.spec.ECKeySpec;
import org.sdm.crypto.Signer;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


public class Blockchain {

	private List<Block> blockchain;

	public Blockchain() {
		this.blockchain = new ArrayList<>();
		blockchain.add(createGenesisBlock());
	}

	public synchronized Block generateNewBlock(Node node, Transaction t) {
		Block latest = getLatestBlock();
		int nextIndex = latest.getIndex() + 1;
		long nextTimestamp = Instant.now().getEpochSecond();
		byte[] data = t.getTransactionBytes();
		return new Block(nextIndex, latest.getHash(), nextTimestamp, data, node.getWallet().getPublicKey());
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

		PublicKey publicKey = null;
		try {
			KeyFactory kf = KeyFactory.getInstance("ECDSA", "BC");
			byte[] bytes = Base64.getDecoder().decode("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEmW4ACKX92n3qrytNKI2jya4G0rigW/HCdkf8SqFsFmqEocY8+6FWgT+3ukTDRIq7Gje5U2dXEUOsTUNk+OSS4Q==");
			publicKey = kf.generatePublic(new X509EncodedKeySpec(bytes));
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		Transaction t = new Transaction(d, new byte[]{0}, publicKey, new byte[]{0}, null);

		byte[] bytes = t.getTransactionBytes();

		return new Block(0, previousHash.toString(), genesis, bytes, publicKey);    //TODO: public key of server
	}

	public synchronized int getBalanceByPublicKey(PublicKey publicKey) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ignored) {
		}
		byte[] address = digest.digest(publicKey.getEncoded());
		Signer signer = new Signer();
		List<DiamondSpec> owned = new ArrayList<>();
		List<DiamondSpec> sent = new ArrayList<>();

		for (int i = blockchain.size() - 1; i >= 0; i--) {
			Block block = blockchain.get(i);
			Transaction t = Transaction.deserialize(block.getData());

			byte[] sig = t.getSignature();
			boolean sigMatch = signer.verifySignature(publicKey, t.getData(), sig);
			if (sigMatch) {
				sent.add(t.getDiamond());
			}

			if (Arrays.equals(address, t.getDestinationAddress()) &&
					!sent.contains(t.getDiamond())) {
				owned.add(t.getDiamond());
			}
		}

		return owned.size();
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
