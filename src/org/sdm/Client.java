package org.sdm;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

public class Client {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("User is up");

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
		byte[] privateKeyBytes = privateKey.getD().toByteArray();

		// First print our generated private key and public key
		System.out.println("Private key: " + bytesToHex(privateKeyBytes));
		System.out.println("Public key: " + bytesToHex(publicKey.getQ().getEncoded(true)));

		// Then calculate the public key only using domainParams.getG() and
		// private key
		//ECPoint Q = domainParams.getG().multiply(new BigInteger(privateKeyBytes));
		//System.out.println("Calculated public key: " + bytesToHex(Q.getEncoded(true)));

		
		try {
			byte[] ph = new byte[20];
			byte[] sha256 = MessageDigest.getInstance ("SHA-256").digest (publicKey.getQ().getEncoded(true));
			RIPEMD160Digest digest = new RIPEMD160Digest ();
			digest.update (sha256, 0, sha256.length);
			digest.doFinal (ph, 0);
			
			System.out.println("BC Address: " + bytesToHex(ph));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public String generate_privateKey() {
		return null;
	}

}
