import java.util.ArrayList;

import jminusminus.CLEmitter;

import static jminusminus.CLConstants.*;

//  Creates the Class for the following java program using CLEmitter:
// public class Seq {
    //     public static void main(String[] args) {
    
    //         // Input Variables
    //         int start = Integer.parseInt(args[0]);
    //         int step = Integer.parseInt(args[1]);
    //         int stop = Integer.parseInt(args[2]);
            
    //         // As long as we havent passed our upper bound:
    //         while (stop > start) {
    //             System.out.println(start);
    //             start = start + step; // Increment the pointer
    //         }
    //     }
    // }
    
/**
 * This class generates bytecode for a sequence generator that prints numbers
 * from start to stop with a given step size.
 */
public class GenSeq {
    public static void main(String[] args) {
        // Create a CLEmitter instance to write the class file
        CLEmitter e = new CLEmitter(true);
        ArrayList<String> modifiers = new ArrayList<>();

        // public class GenSeq (changed from Seq)
        modifiers.add("public");
        e.addClass(modifiers, "GenSeq", "java/lang/Object", null, true);

        // public static void main(String[] args) 
        modifiers.clear();
        modifiers.add("public");
        modifiers.add("static");
        e.addMethod(modifiers, "main", "([Ljava/lang/String;)V", null, true);
        
        // Parse command line args
        // start = Integer.parseInt(args[0])
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_0);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_1);

        // step = Integer.parseInt(args[1])
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_1);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_2);

        // stop = Integer.parseInt(args[2])
        e.addNoArgInstruction(ALOAD_0);
        e.addNoArgInstruction(ICONST_2);
        e.addNoArgInstruction(AALOAD);
        e.addMemberAccessInstruction(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");
        e.addNoArgInstruction(ISTORE_3);

        // Create loop labels
        String loopStart = e.createLabel();
        String loopEnd = e.createLabel();

        // Loop start
        e.addLabel(loopStart);

        // if (stop <= start) goto loopEnd (end the looping)
        e.addNoArgInstruction(ILOAD_3);  // stop
        e.addNoArgInstruction(ILOAD_1);  // start
        e.addBranchInstruction(IF_ICMPLE, loopEnd);

        // Print value at current iteration
        e.addMemberAccessInstruction(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        e.addNoArgInstruction(ILOAD_1);
        e.addMemberAccessInstruction(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");

        // start += step
        e.addNoArgInstruction(ILOAD_1);
        e.addNoArgInstruction(ILOAD_2);
        e.addNoArgInstruction(IADD);
        e.addNoArgInstruction(ISTORE_1);

        // goto loopStart
        e.addBranchInstruction(GOTO, loopStart);

        // Loop end
        e.addLabel(loopEnd);

        // return
        e.addNoArgInstruction(RETURN);

        // Write class file for Gen Seq
        e.write();
    }
}