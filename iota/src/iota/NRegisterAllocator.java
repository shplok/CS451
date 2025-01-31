package iota;

/**
 * An abstract representation of a register allocator.
 */
abstract class NRegisterAllocator {
    /**
     * The control flow graph for a method.
     */
    protected NControlFlowGraph cfg;

    /**
     * Constructs an NRegisterAllocator object.
     *
     * @param cfg control flow graph for the method.
     */
    protected NRegisterAllocator(NControlFlowGraph cfg) {
        this.cfg = cfg;
        cfg.computeLivenessIntervals();
    }

    /**
     * Allocates physical registers to virtual registers.
     */
    public abstract void run();
}
