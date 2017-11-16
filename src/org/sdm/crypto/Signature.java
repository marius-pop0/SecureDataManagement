package org.sdm.crypto;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;

/**
 * @author Mathew : 11/11/2017.
 */
public class Signature {

	private RSADigestSigner signer;

	public Signature() {
		this.signer = new RSADigestSigner(new SHA256Digest());
	}

	public byte[] generateSignature(ECPrivateKeyParameters priv, byte[] input) {
		signer.init(true, priv);
		signer.update(input, 0, input.length);
		byte[] signature = null;
		try {
			signature = signer.generateSignature();
		} catch (CryptoException e) {
			e.printStackTrace();
		}

		return signature;
	}

	public boolean verifySignature(ECPublicKeyParameters pub, byte[] input, byte[] signature) {
		signer.init(false, pub);
		signer.update(input, 0, input.length);
		return signer.verifySignature(signature);
	}

}
