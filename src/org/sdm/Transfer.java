package org.sdm;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;


public class Transfer{

    private ECPrivateKeyParameters privKey;
    public DiamondSpec diamondSpec;
    public byte[] destinationAddress;

    public Transfer(ECPrivateKeyParameters privKey,DiamondSpec diamondSpec, byte[] destinationAddress){
        this.privKey=privKey;
        this.diamondSpec=diamondSpec;
        this.destinationAddress=destinationAddress;
    }

     public void signAndSend(DataOutputStream outputStream) throws IOException {

         ByteArrayOutputStream outputStreamMisc = new ByteArrayOutputStream();

         //TODO: Get Originating Address of each diamond the owner is transferring
         //TODO: Seperate the different sections by a new line?
         outputStreamMisc.write("originatingAddress".getBytes("utf-8"));
         outputStreamMisc.write(diamondSpec.getDiamondBytes());
         outputStreamMisc.write(destinationAddress);

         byte [] transfer= outputStreamMisc.toByteArray();

         ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
         signer.init(true,privKey);
         BigInteger[] components = signer.generateSignature(transfer);

         System.out.println(components[0]);
         System.out.println(components[1]);

         ECDSASignature sig = new ECDSASignature(components[0],components[1]);

         outputStream.write(transfer);
         outputStream.write(sig.toByteArray());
     }


     public boolean checkSignedMessage(byte[] message){



        return false;
     }

    //Signature Wrapper
    public static class ECDSASignature {
        /**
         * The two components of the signature.
         */
        public final BigInteger r, s;


        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        @Override
        /**
         *Check that both parts of the signature are the same
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ECDSASignature other = (ECDSASignature) o;
            return r.equals(other.r) &&
                    s.equals(other.s);
        }


        /**
         * Get hash of  both parts of the Signature
         */
        @Override
        public int hashCode() {
            int result = r.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }

        public byte[] toByteArray() throws IOException {
            byte[] sigR = r.toByteArray();
            byte[] sigS = s.toByteArray();

            System.out.println(sigR.length+" "+sigS.length);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(sigR);
            outputStream.write(sigS);

            return (outputStream.toByteArray());
        }

        public BigInteger[] parseSig(byte[] sigBytes){
            byte[] rBytes = Arrays.copyOfRange(sigBytes,0,32);
            byte[] sBytes = Arrays.copyOfRange(sigBytes,32,65);
            BigInteger r = new BigInteger(rBytes);
            BigInteger s = new BigInteger(sBytes);
            return new BigInteger[] { r, s };
        }
    }
}

