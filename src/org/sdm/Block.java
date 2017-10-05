package org.sdm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;

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

    public String calculateHash(){

        byte[] index = Integer.toString(this.index).getBytes(StandardCharsets.UTF_8);
        byte[] timestamp = Long.toString(Instant.now().getEpochSecond()).getBytes(StandardCharsets.UTF_8);


        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        try {
            outputStream.write(index);
            outputStream.write(previousHash.getBytes(StandardCharsets.UTF_8));
            outputStream.write(timestamp);
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte concatBytes [] = outputStream.toByteArray();

        String hash = digest.digest(concatBytes).toString();

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
