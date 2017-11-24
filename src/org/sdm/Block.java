package org.sdm;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

public class Block implements Serializable {

	private int index;
	private String previousHash;
	private long timestamp;
	private byte[] data;
	private String hash;
	private PublicKey publicKey;

	public Block(int index, String previousHash, long timestamp, byte[] data, PublicKey publicKey) {
		this.index = index;
		this.previousHash = previousHash;
		this.timestamp = timestamp;
		this.data = data;
		this.publicKey = publicKey;
		this.hash = calculateHash();
	}

	public String calculateHash() {
		byte[] index = Integer.toString(this.index).getBytes(StandardCharsets.UTF_8);
		byte[] timestamp = Long.toString(this.timestamp).getBytes(StandardCharsets.UTF_8);

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.exit(1);
		}

		String hash;
		byte[] concatBytes = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(index);
			outputStream.write(previousHash.getBytes(StandardCharsets.UTF_8));
			outputStream.write(timestamp);
			outputStream.write(data);
			outputStream.write(publicKey.getEncoded());
			concatBytes = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}

		hash = DatatypeConverter.printHexBinary(digest.digest(concatBytes));
		return hash;
	}

	public int getIndex() {
		return index;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public byte[] getData() {
		return data;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public String getHash() {
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Block block = (Block) o;

		if (index != block.index) return false;
		if (timestamp != block.timestamp) return false;
		if (!previousHash.equals(block.previousHash)) return false;
		Transaction a = Transaction.deserialize(this.data);
		Transaction b = Transaction.deserialize(block.data);
		if (!a.equals(b)) return false;
		return hash.equals(block.hash);
	}

	@Override
	public int hashCode() {
		int result = index;
		result = 31 * result + previousHash.hashCode();
		result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
		result = 31 * result + Arrays.hashCode(data);
		result = 31 * result + hash.hashCode();
		return result;
	}
}
