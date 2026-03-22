// TC179xMapFlash.java
// Ghidra script to set up the TC179x/TC1798 flash memory map after importing
// a raw binary firmware image.
//
// Usage:
//   1. Import the firmware file as Raw Binary:
//        File -> Import File -> select firmware -> format "Raw Binary"
//        Language: tricore:LE:32:tc179x  (or tc1798)
//        Base address: 0x80000000
//   2. Open the imported program.
//   3. Run this script (Script Manager -> TC179xMapFlash).
//
// What this script does:
//   - Renames the initial block (loaded at 0x80000000) to PFLASH0
//   - Creates PFLASH1 at 0x80800000 from file offset 0x200000 (2 MB)
//   - Creates non-cached mirror overlays for PFLASH0_NC and PFLASH1_NC
//   - Reports what it did
//
// Assumptions:
//   - The raw binary is a full 4 MB dual-bank flash image (PMU0 + PMU1)
//   - TC1791 "384" variants only have 1 MB on PMU1; adjust PFLASH1_SIZE below
//
// @author OpenCode
// @category Memory
// @keybinding
// @menupath
// @toolbar

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.mem.*;

public class TC179xMapFlash extends GhidraScript {

    // Adjust these if your binary differs
    private static final long PFLASH0_ADDR    = 0x80000000L;
    private static final long PFLASH1_ADDR    = 0x80800000L;
    private static final long PFLASH0_NC_ADDR = 0xA0000000L;
    private static final long PFLASH1_NC_ADDR = 0xA0800000L;
    private static final long PFLASH_SIZE     = 0x200000L;  // 2 MB per bank

    // File offsets within the binary
    private static final long PFLASH0_FILE_OFFSET = 0x000000L;
    private static final long PFLASH1_FILE_OFFSET = 0x200000L;

    @Override
    public void run() throws Exception {
        Memory mem = currentProgram.getMemory();
        AddressFactory af = currentProgram.getAddressFactory();

        // --- Step 1: Find and rename the initial block ---
        Address pflash0Start = af.getDefaultAddressSpace().getAddress(PFLASH0_ADDR);
        MemoryBlock initialBlock = mem.getBlock(pflash0Start);

        if (initialBlock == null) {
            printerr("ERROR: No memory block found at 0x80000000.");
            printerr("Make sure you imported with base address 0x80000000.");
            return;
        }

        if (!initialBlock.getName().equals("PFLASH0")) {
            initialBlock.setName("PFLASH0");
            println("Renamed initial block to PFLASH0 at 0x80000000");
        } else {
            println("PFLASH0 already named correctly at 0x80000000");
        }

        // --- Step 2: Create PFLASH1 from file bytes ---
        Address pflash1Start = af.getDefaultAddressSpace().getAddress(PFLASH1_ADDR);
        if (mem.getBlock(pflash1Start) != null) {
            println("PFLASH1 block already exists at 0x80800000, skipping.");
        } else {
            // Check the binary is large enough
            long fileSize = initialBlock.getSize();
            if (fileSize < PFLASH1_FILE_OFFSET + PFLASH_SIZE) {
                println("WARNING: Binary is only " + fileSize + " bytes.");
                println("  Expected at least " + (PFLASH1_FILE_OFFSET + PFLASH_SIZE) +
                        " bytes for a full dual-bank image.");
                println("  TC1791 '384' variants only have 1 MB on PMU1 — adjust PFLASH1_SIZE if needed.");
                long available = fileSize - PFLASH1_FILE_OFFSET;
                if (available <= 0) {
                    printerr("No bytes available for PFLASH1. Skipping.");
                    return;
                }
                println("  Creating PFLASH1 with available size: " + available + " bytes.");
                createFlashBlock(mem, af, "PFLASH1", PFLASH1_ADDR, available,
                        initialBlock, PFLASH1_FILE_OFFSET);
            } else {
                createFlashBlock(mem, af, "PFLASH1", PFLASH1_ADDR, PFLASH_SIZE,
                        initialBlock, PFLASH1_FILE_OFFSET);
                println("Created PFLASH1 at 0x80800000 (2 MB, file offset 0x200000)");
            }
        }

        // --- Step 3: Create non-cached overlay mirrors ---
        createOverlay(mem, af, "PFLASH0_NC", PFLASH0_ADDR, PFLASH0_NC_ADDR, PFLASH_SIZE);
        createOverlay(mem, af, "PFLASH1_NC", PFLASH1_ADDR, PFLASH1_NC_ADDR, PFLASH_SIZE);

        println("");
        println("TC179x flash memory map complete.");
        println("  PFLASH0    0x80000000  2 MB  (cached,     file offset 0x000000)");
        println("  PFLASH1    0x80800000  2 MB  (cached,     file offset 0x200000)");
        println("  PFLASH0_NC 0xA0000000  2 MB  (non-cached, overlay of PFLASH0)");
        println("  PFLASH1_NC 0xA0800000  2 MB  (non-cached, overlay of PFLASH1)");
    }

    private void createFlashBlock(Memory mem, AddressFactory af, String name,
            long addr, long size, MemoryBlock sourceBlock, long fileOffset)
            throws Exception {
        Address start = af.getDefaultAddressSpace().getAddress(addr);
        // Split the source block to carve out the bytes at fileOffset
        // Ghidra doesn't let us create a new initialized block from a file offset
        // of an existing block directly, so we copy bytes via a byte array.
        byte[] bytes = new byte[(int) size];
        sourceBlock.getBytes(
                af.getDefaultAddressSpace().getAddress(PFLASH0_ADDR + fileOffset),
                bytes);
        MemoryBlock newBlock = mem.createInitializedBlock(name, start, size, (byte) 0xFF, monitor, false);
        newBlock.putBytes(start, bytes);
        newBlock.setRead(true);
        newBlock.setWrite(false);
        newBlock.setExecute(true);
        newBlock.setComment("TC179x PMU1 Program Flash (cached)");
    }

    private void createOverlay(Memory mem, AddressFactory af, String name,
            long sourceAddr, long targetAddr, long size) throws Exception {
        Address target = af.getDefaultAddressSpace().getAddress(targetAddr);
        if (mem.getBlock(target) != null) {
            println(name + " block already exists at 0x" +
                    Long.toHexString(targetAddr) + ", skipping.");
            return;
        }
        // Non-cached mirror: create as uninitialized alias
        // (Ghidra doesn't have true aliased blocks, so we mark it uninitialized
        //  to indicate it's the same physical flash, just a different bus path)
        MemoryBlock block = mem.createUninitializedBlock(name, target, size, false);
        block.setRead(true);
        block.setWrite(false);
        block.setExecute(true);
        block.setComment("TC179x non-cached mirror of " +
                (name.contains("0") ? "PFLASH0 (0x80000000)" : "PFLASH1 (0x80800000)"));
        println("Created " + name + " at 0x" + Long.toHexString(targetAddr) +
                " (non-cached mirror, uninitialized)");
    }
}
