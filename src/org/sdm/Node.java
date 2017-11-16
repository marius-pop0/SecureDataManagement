package org.sdm;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.sdm.concurrent.ListenForNodesTask;
import org.sdm.crypto.Signature;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mathew : 11/11/2017.
 */
public class Node {

	private final ExecutorService pool;
	private final ConcurrentHashMap<String, NodeSocket> nodes;
	private AtomicBoolean isListening;

	private ECPublicKeyParameters pubKey;
	private ECPrivateKeyParameters privKey;

	public Node() {
		this.nodes = new ConcurrentHashMap<>();
		this.pool = Executors.newCachedThreadPool();
		this.isListening = new AtomicBoolean(true);

		generateKeys();

		listenForNodes();
	}

	private void generateKeys() {
		X9ECParameters ecp = SECNamedCurves.getByName("secp256k1");
		ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(),
				ecp.getSeed());
		// Generate a private key and a public key
		AsymmetricCipherKeyPair keyPair;
		ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
		ECKeyPairGenerator generator = new ECKeyPairGenerator();
		generator.init(keyGenParams);
		keyPair = generator.generateKeyPair();

		ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
		ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();

		this.pubKey = publicKey;
		this.privKey = privateKey;
	}

	private void listenForNodes() {
		pool.execute(() -> new ListenForNodesTask(this, pool, nodes));
	}

	private void listenForTransactions() {

	}

	public void receiveTransaction(Transaction transaction) {
		Block latestBlock = Blockchain.getInstance().getLatestBlock();
		byte[] hash = new BigInteger(latestBlock.getHash(), 16).toByteArray();
		byte[] signature = new Signature().generateSignature(privKey, hash);
		double hit = -1;

		try {
			byte[] val = MessageDigest.getInstance("SHA-256").digest(signature);
			byte[] first8 = Arrays.copyOfRange(val, 0, 8);
			hit = ByteBuffer.wrap(first8).getDouble();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		//TODO: calculate target

		double target;
		do {
			target = Double.MAX_VALUE;
		} while (hit >= target);

		for (NodeSocket node : nodes.values()) {

		}
	}

	public boolean isListening() {
		return this.isListening.get();
	}

}
