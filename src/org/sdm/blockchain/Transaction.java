package org.sdm.blockchain;

import org.sdm.crypto.Signer;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;

/**
 * @author Mathew : 11/11/2017.
 */
public class Transaction implements Serializable {

	private PublicKey publicKey;
	private DiamondSpec diamond;
	private byte[] destinationAddress;
	private byte[] signature;
	private long timestamp;
	private byte[] serverToken;
	private byte[] previousTransaction;
	//TODO: originating transaction

	public Transaction(DiamondSpec diamond, byte[] destinationAddress, PublicKey publicKey, byte[] serverToken, byte[] previousTransaction) {
		this.diamond = diamond;
		this.destinationAddress = destinationAddress;
		this.timestamp = Instant.now().getEpochSecond();
		this.serverToken = serverToken;
		this.publicKey = publicKey;
		this.previousTransaction = previousTransaction;
	}

	public void sign(PrivateKey privateKey) {
		Signer ts = new Signer();
		byte[] concat = getData();
		signature = ts.generateSignature(privateKey, concat);
	}

	public byte[] getData() {
		byte[] diamondBytes = new byte[0];
		try {
			diamondBytes = diamond.getDiamondBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] timestamp = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(this.timestamp).array();
		byte[] concat = new byte[destinationAddress.length + diamondBytes.length + timestamp.length + serverToken.length];
		System.arraycopy(destinationAddress, 0, concat, 0, destinationAddress.length);
		System.arraycopy(diamondBytes, 0, concat, destinationAddress.length, diamondBytes.length);
		System.arraycopy(timestamp, 0, concat, destinationAddress.length + diamondBytes.length, timestamp.length);
		System.arraycopy(serverToken, 0, concat, destinationAddress.length + diamondBytes.length + timestamp.length, serverToken.length);
		return concat;
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

	public byte[] getServerToken() {
		return serverToken;
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

	public byte[] getDestinationAddress() {
		return destinationAddress;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Transaction that = (Transaction) o;

		if (timestamp != that.timestamp) return false;
		if (!diamond.equals(that.diamond)) return false;
		if (!Arrays.equals(destinationAddress, that.destinationAddress)) return false;
		return Arrays.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		int result = diamond.hashCode();
		result = 31 * result + Arrays.hashCode(destinationAddress);
		result = 31 * result + Arrays.hashCode(signature);
		result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public byte[] getPreviousTransaction() {
		return previousTransaction;
	}
}
