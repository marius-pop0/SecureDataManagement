package org.sdm;

import java.io.IOException;
import java.time.Instant;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Blockchain blockchain= new Blockchain();

        //DiamondSpec d = getDiamondInfo();
        DiamondSpec d = new DiamondSpec(Instant.now().getEpochSecond(),
                1,
                "round",
                1.22,
                0.23,
                .55,
                1.22,
                .98,
                2,
                2,
                2,
                4,
                2,
                4,
                "First Diamond",
                "Canada,",
                true);

        try {
            boolean valid =  false;
            while(!valid){
                Block b = blockchain.generateNewBlock(d);
                valid = blockchain.addBlock(b);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
/*
    private static DiamondSpec getDiamondInfo(){

        DiamondSpec diamond =  null;

        long date= Instant.now().getEpochSecond();




        //diamond = new DiamondSpec(date,);
        return diamond;
    }

*/

}
