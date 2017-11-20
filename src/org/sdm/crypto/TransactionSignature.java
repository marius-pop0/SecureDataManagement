package org.sdm.crypto;

import java.security.*;

/**
 * @author Mathew : 11/11/2017.
 */
public class TransactionSignature {

	private Signature signer;

	public TransactionSignature() {
		try {
			this.signer = Signature.getInstance("SHA256withECDSA", "BC");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	public byte[] generateSignature(PrivateKey key, byte[] input) {
		byte[] signature = null;
		try {
			signer.initSign(key);
			signer.update(input);
			signature = signer.sign();
		} catch (InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		return signature;
	}

	public boolean verifySignature(PublicKey pub, byte[] input, byte[] signature) {
		boolean result = false;
		try {
			signer.initVerify(pub);
			signer.update(input);
			result = signer.verify(signature);
		} catch (InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}

		return result;
	}

}
