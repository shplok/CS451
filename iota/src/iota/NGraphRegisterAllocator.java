package iota;

/**
 * A register allocator that uses the graph coloring algorithm to allocate physical registers to virtual registers.
 */
class NGraphRegisterAllocator extends NRegisterAllocator {
    /**
     * Constructs an NGraphRegisterAllocator object.
     *
     * @param cfg control flow graph for the method.
     */
    public NGraphRegisterAllocator(NControlFlowGraph cfg) {
        super(cfg);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        // TBD
    }
}
