package iota;

import java.util.ArrayList;
import java.util.HashMap;

import static iota.NPhysicalRegister.*;

/**
 * An abstract low-level intermediate representation (LIR) of an HIR instruction.
 */
abstract class NLirInstruction {
    /**
     * Maps LIR mnemonic to the corresponding Marvin mnemonic. For example, maps "push" to "pushr".
     */
    protected static HashMap<String, String> lir2Marvin;

    // Create and populate lir2Marvin.
    static {
        lir2Marvin = new HashMap<>();
        lir2Marvin.put("call", "calln");
        lir2Marvin.put("jeq", "jeqn");
        lir2Marvin.put("jge", "jgen");
        lir2Marvin.put("jgt", "jgtn");
        lir2Marvin.put("jle", "jlen");
        lir2Marvin.put("jlt", "jltn");
        lir2Marvin.put("jne", "jnen");
        lir2Marvin.put("jump", "jumpn");
        lir2Marvin.put("load", "loadn");
        lir2Marvin.put("pop", "popr");
        lir2Marvin.put("push", "pushr");
        lir2Marvin.put("return", "jumpn");
        lir2Marvin.put("store", "storen");
    }

    /**
     * Enclosing basic block.
     */
    public NBasicBlock block;

    /**
     * Instruction id.
     */
    public int id;

    /**
     * Instruction mnemonic.
     */
    public String mnemonic;

    /**
     * Input registers (if any).
     */
    public ArrayList<NRegister> reads;

    /**
     * Output register (if any).
     */
    public NRegister write;

    /**
     * Constructs an NLirInstruction object.
     *
     * @param block    enclosing block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     */
    protected NLirInstruction(NBasicBlock block, int id, String mnemonic) {
        this.block = block;
        this.id = id;
        this.mnemonic = mnemonic;
        reads = new ArrayList<>();
        write = null;
    }

    /**
     * Returns the id of this instruction as a string.
     *
     * @return Returns the id of this instruction as a string.
     */
    public String id() {
        return id + "";
    }

    /**
     * Converts this instruction to the corresponding Marvin instruction.
     */
    public void toMarvin() {
        // Nothing here.
    }

    /**
     * Returns a string representation of this instruction.
     *
     * @return a string representation of this instruction.
     */
    public String toString() {
        return id();
    }
}

/**
 * Representation of an arithmetic instruction.
 */
class NLirArithmetic extends NLirInstruction {
    /**
     * Constructs an NLirArithmetic object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     * @param lhs      lhs instruction.
     * @param rhs      rhs instruction.
     */
    public NLirArithmetic(NBasicBlock block, int id, String mnemonic, NLirInstruction lhs, NLirInstruction rhs) {
        super(block, id, mnemonic);
        reads.add(lhs.write);
        reads.add(rhs.write);
        write = new NVirtualRegister(NControlFlowGraph.regId++);
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinArithmetic(mnemonic, toPhysicalRegister(write),
                toPhysicalRegister(reads.get(0)), toPhysicalRegister(reads.get(1)));
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + write + " " + reads.get(0) + " " + reads.get(1);
    }
}

/**
 * Representation of a method call instruction.
 */
class NLirCall extends NLirInstruction {
    /**
     * Method name.
     */
    public String name;

    /**
     * Method descriptor.
     */
    public String descriptor;

    /**
     * Constructs an NLirCall object.
     *
     * @param block      enclosing basic block.
     * @param id         instruction id.
     * @param mnemonic   instruction mnemonic.
     * @param name       method name.
     * @param descriptor method descriptor.
     * @param result     physical register that will store the return value (if any).
     */
    public NLirCall(NBasicBlock block, int id, String mnemonic, String name, String descriptor,
                    NPhysicalRegister result) {
        super(block, id, mnemonic);
        this.name = name;
        this.descriptor = descriptor;
        if (result != null) {
            write = result;
            block.cfg.registers.set(RV, result);
        }
        block.cfg.registers.set(RA, regInfo[RA]);
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinCall(name, descriptor, regInfo[RA], -1);
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String s = id + ": " + (write != null ? write + " " : "") + mnemonic + " " + name + descriptor;
        return s;
    }
}

/**
 * Representation of an instruction for copying one register into another register.
 */
class NLirCopy extends NLirInstruction {
    /**
     * Constructs an NLirCopy object.
     *
     * @param block enclosing block.
     * @param id    instruction id.
     * @param to    register (virtual or physical) to copy to.
     * @param from  register (virtual or physical) to copy from.
     */
    public NLirCopy(NBasicBlock block, int id, NRegister to, NRegister from) {
        super(block, id, "copy");
        reads.add(from);
        write = to;
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinCopy(toPhysicalRegister(write), toPhysicalRegister(reads.get(0)));
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + write + " " + reads.get(0);
    }
}

/**
 * Representation of an instruction for loading an integer.
 */
class NLirIConst extends NLirInstruction {
    /**
     * The integer.
     */
    public int N;

    /**
     * Constructs an NLirIConst object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     * @param N        the integer.
     */
    public NLirIConst(NBasicBlock block, int id, String mnemonic, int N) {
        super(block, id, mnemonic);
        this.N = N;
        write = new NVirtualRegister(NControlFlowGraph.regId++);
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinIConst(toPhysicalRegister(write), N);
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + write + " " + N;
    }
}

/**
 * Representation of an instruction for incrementing (or decrementing) a register by a constant value.
 */
class NLirInc extends NLirInstruction {
    /**
     * Increment (or decrement) value.
     */
    public int N;

    /**
     * Constructs an NLirInc object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param register the register to increment (or decrement).
     * @param N        increment (or decrement) value.
     */
    public NLirInc(NBasicBlock block, int id, NRegister register, int N) {
        super(block, id, "inc");
        write = register;
        this.N = N;
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinInc(toPhysicalRegister(write), N);
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + write + " " + N;
    }
}

/**
 * Representation of a jump (conditional or unconditional) jump instruction.
 */
class NLirJump extends NLirInstruction {
    /**
     * Block to jump to on true (null for return from a method).
     */
    public NBasicBlock trueBlock;

    /**
     * Block to jump to on false (null for an unconditional jump and return from a method).
     */
    public NBasicBlock falseBlock;

    /**
     * Whether the jump (if unconditional) is to return from a method.
     */
    public boolean returnFromMethod;

    /**
     * Constructs an NLirJump object for a conditional jump.
     *
     * @param block            enclosing block.
     * @param id               instruction id.
     * @param mnemonic         instruction mnemonic.
     * @param lhs              lhs instruction (null for return from a method).
     * @param rhs              rhs instruction (null for return from a method).
     * @param trueBlock        block to jump to on true.
     * @param falseBlock       block to jump to on false.
     * @param returnFromMethod whether the jump is to return from a method.
     */
    public NLirJump(NBasicBlock block, int id, String mnemonic, NLirInstruction lhs, NLirInstruction rhs,
                    NBasicBlock trueBlock, NBasicBlock falseBlock, boolean returnFromMethod) {
        super(block, id, mnemonic);
        if (lhs != null) {
            reads.add(lhs.write);
        }
        if (rhs != null) {
            reads.add(rhs.write);
        }
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
        this.returnFromMethod = returnFromMethod;
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins;
        if (trueBlock == null && falseBlock == null) {
            // Return from a method.
            ins = new NMarvinJump(lir2Marvin.get(mnemonic), null, null, null, null, 
                                  true);
        } else if (falseBlock == null) {
            // Unconditional jump.
            ins = new NMarvinJump(lir2Marvin.get(mnemonic), null, null, trueBlock, null, 
                                  false);
        } else {
            // Conditional jump.
            ins = new NMarvinJump(lir2Marvin.get(mnemonic), toPhysicalRegister(reads.get(0)),
                    toPhysicalRegister(reads.get(1)), trueBlock, falseBlock, false);
        }
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (trueBlock == null && falseBlock == null) {
            return id() + ": " + mnemonic;
        } else if (falseBlock == null) {
            return id() + ": " + mnemonic + " " + trueBlock.id();
        } else {
            return id() + ": " + mnemonic + " " + reads.get(0) + " " + reads.get(1) + " " + trueBlock.id() +
                    " " + falseBlock.id();
        }
    }
}

/**
 * Representation of a load (from memory to register) instruction.
 */
class NLirLoad extends NLirInstruction {
    /**
     * Offset from the base memory address to load from.
     */
    public int N;

    /**
     * Constructs an NLirLoad object.
     *
     * @param block    enclosing block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     * @param to       register (virtual or physical) to store into.
     * @param from     base memory address.
     * @param N        offset from base memory address to load from.
     */
    public NLirLoad(NBasicBlock block, int id, String mnemonic, NRegister to, NRegister from, int N) {
        super(block, id, mnemonic);
        reads.add(from);
        write = to;
        this.N = N;
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinLoad(lir2Marvin.get(mnemonic), toPhysicalRegister(write),
                toPhysicalRegister(reads.get(0)), N);
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (mnemonic.equals("pop")) {
            return id() + ": " + mnemonic + " " + write + " " + reads.get(0);
        }
        return id() + ": " + mnemonic + " " + write + " " + reads.get(0) + " " + N;
    }
}

/**
 * Representation of a phi function.
 */
class NLirPhiFunction extends NLirInstruction {
    /**
     * Constructs an NLirPhiFunction object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     */
    public NLirPhiFunction(NBasicBlock block, int id, String mnemonic) {
        super(block, id, mnemonic);
        write = new NVirtualRegister(NControlFlowGraph.regId++);
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }
}

/**
 * Representation of an instruction for reading an integer from standard input into a register.
 */
class NLirRead extends NLirInstruction {
    /**
     * Constructs an NLirRead object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param register register to read into.
     */
    public NLirRead(NBasicBlock block, int id, NRegister register) {
        super(block, id, "read");
        write = register;
        if (write instanceof NPhysicalRegister) {
            block.cfg.registers.set(write.number, write);
        } else if (!block.cfg.registers.contains(write)) {
            block.cfg.registers.add(write);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinRead(toPhysicalRegister(write));
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + write;
    }
}

/**
 * Representation of a store (from register to memory) instruction.
 */
class NLirStore extends NLirInstruction {
    /**
     * Offset from the base memory address to store at.
     */
    public int N;

    /**
     * Constructs an NLirStore object.
     *
     * @param block    enclosing block.
     * @param id       instruction id.
     * @param mnemonic instruction mnemonic.
     * @param from     register (virtual or physical) to store from.
     * @param to       base memory address.
     * @param N        offset from the base memory address to store at.
     */
    public NLirStore(NBasicBlock block, int id, String mnemonic, NRegister from, NRegister to, int N) {
        super(block, id, mnemonic);
        reads.add(from);
        reads.add(to);
        this.N = N;
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinStore(lir2Marvin.get(mnemonic), toPhysicalRegister(reads.get(0)),
                toPhysicalRegister(reads.get(1)), N);
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (mnemonic.equals("push")) {
            return id() + ": " + mnemonic + " " + reads.get(0) + " " + reads.get(1);
        }
        return id() + ": " + mnemonic + " " + reads.get(0) + " " + reads.get(1) + " " + N;
    }
}

/**
 * Representation of an instruction for writing an integer in a register to standard output.
 */
class NLirWrite extends NLirInstruction {
    /**
     * Constructs an NLirWrite object.
     *
     * @param block    enclosing basic block.
     * @param id       instruction id.
     * @param register the register to write.
     */
    public NLirWrite(NBasicBlock block, int id, NRegister register) {
        super(block, id, "write");
        reads.add(register);
    }

    /**
     * {@inheritDoc}
     */
    public void toMarvin() {
        NMarvinInstruction ins = new NMarvinWrite(toPhysicalRegister(reads.get(0)));
        block.marvin.add(ins);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return id() + ": " + mnemonic + " " + reads.get(0);
    }
}
