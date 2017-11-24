package org.sdm;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sdm.blockchain.Block;
import org.sdm.blockchain.DiamondSpec;
import org.sdm.node.Server;
import org.sdm.blockchain.Transaction;
import org.sdm.node.Node;

import java.security.Security;
import java.time.Instant;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());

		// create two different diamonds
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

		// start the server and create a node in the network
		// the node automatically registers with the server, and obtains an access token signed with the server's private key
		Server server = new Server(9999);
		Node a = new Node(8888);

		// request the server to add the two diamonds to the blockchain
		// therefore, the size of the blockchain will be 3 (including the genesis block)
		a.createDiamond(d);
		a.createDiamond(e);

		// we wait until the node obtains the rights to forge all blocks, and the size of the blockchain grows to 3
		while (a.getBlockchain().getChain().size() != 3) {
			sleep(10);
		}
		// print the blockchain hashes (previous hash & current hash)
		printChain(a.getBlockchain().getChain());
		System.out.println();

		// attempt to create the first diamond again
		// since duplicate diamonds are not possible, this will have no effect on the blockchain
		a.createDiamond(d);
		sleep(1000);
		System.out.println("size of blockchain still 3? " + (a.getBlockchain().getChain().size() == 3));


		// create a new node and send a diamond from node A to node B
		// this creates a new transaction in the blockchain
		// every node keeps track of the blockchain's unspent transactions
		Node b = new Node(7777);
		Transaction t = Transaction.deserialize(a.getBlockchain().getChain().get(1).getData());
		a.sendDiamond(t, b.getAddress());

		// we wait until the block is created and added to the blockchain and print the resulting blockchain hashes
		while (a.getBlockchain().getChain().size() != 4) {
			sleep(10);
		}
		printChain(a.getBlockchain().getChain());
		System.out.println();

		// we check whether the sent diamond actually matches the diamond from the originating transaction
		// if it does, the diamond was successfully sent
		DiamondSpec d1 = Transaction.deserialize(a.getBlockchain().getChain().get(1).getData()).getDiamond();
		DiamondSpec d2 = Transaction.deserialize(a.getBlockchain().getChain().get(3).getData()).getDiamond();
		System.out.println("same diamonds? -> " + d1.equals(d2));

		// we try sending the diamond once again, but this time, the blockchain will remain unaffected,
		// 		since node A does not own the diamond anymore
		// (verifying this requires some creative use of breakpoints...)
		a.sendDiamond(t, b.getAddress());
		sleep(30000);	// sleep in order to successfully create the block (might not be enough time)
		printChain(a.getBlockchain().getChain());
	}

	private static void printChain(List<Block> chain) {
		System.out.println("----------------------------------------------------------------");
		for (Block block : chain) {
			System.out.println(block.getPreviousHash());
			System.out.println(block.getHash());
			System.out.println("----------------------------------------------------------------");
		}
	}

	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
