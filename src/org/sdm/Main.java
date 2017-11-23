package org.sdm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		DiamondSpec d = new DiamondSpec(Instant.now().getEpochSecond(),
				1,
				"round",
				1.22,
				0.23,
				.55,
				1.22,
				.98,
				2,
				2,
				2,
				4,
				2,
				4,
				"First Diamond",
				"Canada,",
				true);

		Server server = new Server(9999);
		Node a = new Node(12345);
		Node b = new Node(12346);
		a.connectToNode(12346);

		a.createDiamond(d);

		while(b.getBlockchain().getChain().size() == 1);
		printChain(b.getBlockchain().getChain());
	}

	private static void printChain(List<Block> chain) {
		for(Block block : chain) {
			System.out.println(block.getHash());
			System.out.println("------------------------------------------------------------------------");
		}
	}

}
