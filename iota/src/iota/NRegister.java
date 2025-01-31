package iota;

/**
 * Abstract representation of a register.
 */
abstract class NRegister {
    /**
     * Register number.
     */
    public int number;

    /**
     * Register name.
     */
    public String name;

    /**
     * Constructs an NRegister object.
     *
     * @param number register number.
     * @param name   register name.
     */
    protected NRegister(int number, String name) {
        this.number = number;
        this.name = name;
    }

    /**
     * Returns a string representation of this register.
     *
     * @return a string representation of this register.
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the physical reg associated with the given reg.
     *
     * @param reg the reg.
     * @return the physical reg associated with the given reg.
     */
    public static NPhysicalRegister toPhysicalRegister(NRegister reg) {
        return reg instanceof NPhysicalRegister ? (NPhysicalRegister) reg : ((NVirtualRegister) reg).pReg;
    }
}

/**
 * Representation of a virtual register.
 */
class NVirtualRegister extends NRegister {
    /**
     * The physical register assigned to this virtual register.
     */
    public NPhysicalRegister pReg;

    /**
     * Whether the physical register must be spilled.
     */
    public boolean spill;

    /**
     * The offset (from stack pointer, SP) where the register will be spilled.
     */
    public int offset;

    /**
     * Constructs an NVirtualRegister object.
     *
     * @param number register number.
     */
    public NVirtualRegister(int number) {
        super(number, "v" + number);
        spill = false;
        offset = -1;
    }
}

/**
 * Representation of a physical register in Marvin.
 */
class NPhysicalRegister extends NRegister {
    /**
     * Maximum number of temporary registers available for allocation.
     */
    public static final int MAX_COUNT = 12;

    /**
     * Temporary register, R0.
     */
    public static final int R0 = 0;

    /**
     * Temporary register, R1.
     */
    public static final int R1 = 1;

    /**
     * Temporary register, R2.
     */
    public static final int R2 = 2;

    /**
     * Temporary register, R3.
     */
    public static final int R3 = 3;

    /**
     * Temporary register, R4.
     */
    public static final int R4 = 4;

    /**
     * Temporary register, R5.
     */
    public static final int R5 = 5;

    /**
     * Temporary register, R6.
     */
    public static final int R6 = 6;

    /**
     * Temporary register, R7.
     */
    public static final int R7 = 7;

    /**
     * Temporary register, R8.
     */
    public static final int R8 = 8;

    /**
     * Temporary register, R9.
     */
    public static final int R9 = 9;

    /**
     * Temporary register, R10.
     */
    public static final int R10 = 10;

    /**
     * Temporary register, R11.
     */
    public static final int R11 = 11;

    /**
     * Return address register, ra.
     */
    public static final int RA = 12;

    /**
     * Return value register, rv.
     */
    public static final int RV = 13;

    /**
     * Frame pointer register, fp.
     */
    public static final int FP = 14;

    /**
     * Stack pointer register, sp.
     */
    public static final int SP = 15;

    /**
     * Maps register number to the register's representation.
     */
    public static final NPhysicalRegister[] regInfo = {new NPhysicalRegister(R0, "r0"), 
            new NPhysicalRegister(R1, "r1"), new NPhysicalRegister(R2, "r2"), 
            new NPhysicalRegister(R3, "r3"), new NPhysicalRegister(R4, "r4"), 
            new NPhysicalRegister(R5, "r5"), new NPhysicalRegister(R6, "r6"), 
            new NPhysicalRegister(R7, "r7"), new NPhysicalRegister(R8, "r8"), 
            new NPhysicalRegister(R9, "r9"), new NPhysicalRegister(R10, "r10"), 
            new NPhysicalRegister(R11, "r11"), new NPhysicalRegister(RA, "r12"), 
            new NPhysicalRegister(RV, "r13"), new NPhysicalRegister(FP, "r14"), 
            new NPhysicalRegister(SP, "r15")};

    /**
     * Constructs an NPhysicalRegister object.
     *
     * @param number register number.
     * @param name   register name.
     */
    public NPhysicalRegister(int number, String name) {
        super(number, name);
    }
}
