import java.lang.Integer;
import java.lang.System;

public class Seq {
    public static void main(String[] args) {

        // Input Variables
        int start = Integer.parseInt(args[0]);
        int step = Integer.parseInt(args[1]);
        int stop = Integer.parseInt(args[2]);
        
        // As long as we havent passed our upper bound:
        while (stop > start) {
            System.out.println(start);
            start = start + step; // Increment the pointer
        }
    }
}