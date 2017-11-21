package org.sdm;

import org.sdm.crypto.Signer;

import java.io.*;
import java.security.PrivateKey;
import java.util.Arrays;

/**
 * @author Mathew : 11/11/2017.
 */
public class Transaction implements Serializable {

	private DiamondSpec diamond;
	private byte[] destinationAddress;
	private byte[] signature;

	public Transaction(DiamondSpec diamond, byte[] destinationAddress) {
		this.diamond = diamond;
		this.destinationAddress = destinationAddress;
	}

	public void sign(PrivateKey privateKey) {
		Signer ts = new Signer();
		byte[] diamondBytes = new byte[0];
		try {
			diamondBytes = diamond.getDiamondBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] concat = new byte[destinationAddress.length + diamondBytes.length];
		System.arraycopy(destinationAddress, 0, concat, 0, destinationAddress.length);
		System.arraycopy(diamondBytes, 0, concat, destinationAddress.length, diamondBytes.length);

		signature = ts.generateSignature(privateKey, concat);
	}

	public byte[] getSignature() {
		return signature;
	}

	public byte[] getTransactionBytes() {
		byte[] bytes = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 ObjectOutputStream o = new ObjectOutputStream(bos)) {
			o.writeObject(this);
			o.flush();
			bytes = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static Transaction deserialize(byte[] bytes) {
		Transaction t = null;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			 ObjectInputStream in = new ObjectInputStream(bis)) {
			t = (Transaction) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return t;
	}

	public DiamondSpec getDiamond() {
		return diamond;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Transaction that = (Transaction) o;

		return Arrays.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(signature);
	}
}
