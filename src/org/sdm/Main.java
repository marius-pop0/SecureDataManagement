package org.sdm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.time.Instant;
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
		DiamondSpec e = new DiamondSpec(Instant.now().getEpochSecond(),
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
				"Second Diamond",
				"Canada,",
				true);

		Server server = new Server(9999);
		Node a = new Node(12345);

		a.createDiamond(d);
		a.createDiamond(d);
		a.createDiamond(d);
		a.createDiamond(e);
		a.createDiamond(d);

		while (a.getBlockchain().getChain().size() != 3) {
			sleep();
		}
		printChain(a.getBlockchain().getChain());
		System.out.println();
	}

	private static void printChain(List<Block> chain) {
		System.out.println("----------------------------------------------------------------");
		for (Block block : chain) {
			System.out.println(block.getPreviousHash());
			System.out.println(block.getHash());
			System.out.println("----------------------------------------------------------------");
		}
	}

	private static void sleep() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
