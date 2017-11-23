package org.sdm.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

/**
 * @author Mathew : 19/11/2017.
 */
public class Encryption {

	public byte[] encrypt(Key key, byte[] input) {
		byte[] output = null;
		try {
			Cipher c = Cipher.getInstance("ECIES", "BC");
			c.init(Cipher.ENCRYPT_MODE, key);
			output = c.doFinal(input);
		} catch (NoSuchAlgorithmException
				| NoSuchProviderException
				| NoSuchPaddingException
				| BadPaddingException
				| IllegalBlockSizeException
				| InvalidKeyException e) {

			e.printStackTrace();
		}

		return output;
	}

}
