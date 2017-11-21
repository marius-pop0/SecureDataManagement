package org.sdm;

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
	private List<DiamondSpec> balance;

	public Wallet(Node node, PublicKey publicKey, PrivateKey privateKey) {
		this.node = node;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public List<DiamondSpec> computeOwnedDiamonds() {
		Signer signer = new Signer();
		List<Block> chain = node.getBlockchain().getChain();
		List<DiamondSpec> owned = new ArrayList<>();
		List<DiamondSpec> sent = new ArrayList<>();
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
				owned.add(t.getDiamond());
			}
		}

		return owned;
	}

	public List<DiamondSpec> getOwnedDiamonds() {
		if (balance == null) {
			return computeOwnedDiamonds();
		}

		return balance;
	}

}