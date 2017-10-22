package org.sdm;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Block {

	private int index;
	private String previousHash;
	private long timestamp;
	private byte[] data;
	private String hash;

	public Block(int index, String previousHash, long timestamp, byte[] data) {
		this.index = index;
		this.previousHash = previousHash;
		this.timestamp = timestamp;
		this.data = data;
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

		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(index);
			outputStream.write(previousHash.getBytes(StandardCharsets.UTF_8));
			outputStream.write(timestamp);
			outputStream.write(data);
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

	public String getHash() {
		return hash;
	}


}
