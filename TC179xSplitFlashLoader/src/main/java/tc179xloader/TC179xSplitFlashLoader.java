package tc179xloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ghidra.app.util.MemoryBlockUtils;
import ghidra.app.util.Option;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractProgramLoader;
import ghidra.app.util.opinion.LoadException;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.app.util.opinion.Loaded;
import ghidra.app.util.opinion.Loader;
import ghidra.app.util.opinion.LoaderTier;
import ghidra.framework.store.LockException;
import ghidra.program.database.mem.FileBytes;
import ghidra.program.model.address.Address;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.util.exception.CancelledException;

public class TC179xSplitFlashLoader extends AbstractProgramLoader {

    private static final String LOADER_NAME = "TC179x Split Flash Binary Loader";

    private static final long SIZE_3MB = 0x300000L;
    private static final long SIZE_4MB = 0x400000L;

    private static final long PFLASH0_ADDR = 0x80000000L;
    private static final long PFLASH1_ADDR = 0x80800000L;
    private static final long PFLASH0_FILE_OFFSET = 0x000000L;
    private static final long PFLASH1_FILE_OFFSET = 0x200000L;
    private static final long PFLASH0_LEN = 0x200000L;
    private static final long PFLASH1_LEN_3MB = 0x100000L;
    private static final long PFLASH1_LEN_4MB = 0x200000L;

    @Override
    public String getName() {
        return LOADER_NAME;
    }

    @Override
    public LoaderTier getTier() {
        return LoaderTier.SPECIALIZED_TARGET_LOADER;
    }

    @Override
    public int getTierPriority() {
        return 100;
    }

    @Override
    public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider) throws IOException {
        List<LoadSpec> loadSpecs = new ArrayList<>();
        long size = provider.length();

        if (size != SIZE_3MB && size != SIZE_4MB) {
            return loadSpecs;
        }

        loadSpecs.add(new LoadSpec(this, PFLASH0_ADDR,
            new LanguageCompilerSpecPair("tricore:LE:32:tc1791_384", "default"),
            size == SIZE_3MB));
        loadSpecs.add(new LoadSpec(this, PFLASH0_ADDR,
            new LanguageCompilerSpecPair("tricore:LE:32:tc179x", "default"),
            size == SIZE_4MB));
        loadSpecs.add(new LoadSpec(this, PFLASH0_ADDR,
            new LanguageCompilerSpecPair("tricore:LE:32:tc1798", "default"),
            false));

        return loadSpecs;
    }

    @Override
    protected List<Loaded<Program>> loadProgram(Loader.ImporterSettings settings)
            throws IOException, LoadException, CancelledException {
        Program program = createProgram(settings);
        loadProgramInto(program, settings);
        List<Loaded<Program>> loadedPrograms = new ArrayList<>();
        loadedPrograms.add(new Loaded<>(program, settings));
        return loadedPrograms;
    }

    @Override
    protected void loadProgramInto(Program program, Loader.ImporterSettings settings)
            throws IOException, LoadException, CancelledException {
        ByteProvider provider = settings.provider();
        MessageLog log = settings.log();
        long size = provider.length();

        long pflash1Len;
        String languageId = settings.loadSpec().getLanguageCompilerSpec().getLanguageID().getIdAsString();
        if ("tricore:LE:32:tc1791_384".equals(languageId)) {
            pflash1Len = PFLASH1_LEN_3MB;
        }
        else {
            pflash1Len = PFLASH1_LEN_4MB;
        }

        if (size < PFLASH0_LEN + pflash1Len) {
            throw new LoadException("Input file too small for selected memory layout");
        }

        int tx = program.startTransaction("Load TC179x split flash");
        boolean commit = false;
        try {
            createDefaultMemoryBlocks(program, settings);

            FileBytes fileBytes = MemoryBlockUtils.createFileBytes(program, provider, settings.monitor());
            Address pflash0 = program.getAddressFactory().getDefaultAddressSpace().getAddress(PFLASH0_ADDR);
            Address pflash1 = program.getAddressFactory().getDefaultAddressSpace().getAddress(PFLASH1_ADDR);

            if (program.getMemory().getBlock(pflash0) != null) {
                program.getMemory().removeBlock(program.getMemory().getBlock(pflash0), settings.monitor());
            }
            if (program.getMemory().getBlock(pflash1) != null) {
                program.getMemory().removeBlock(program.getMemory().getBlock(pflash1), settings.monitor());
            }

            MemoryBlockUtils.createInitializedBlock(program, false, "PFLASH0", pflash0, fileBytes,
                PFLASH0_FILE_OFFSET, PFLASH0_LEN, "", "TC179x PFLASH0", true, false, true, log);
            MemoryBlockUtils.createInitializedBlock(program, false, "PFLASH1", pflash1, fileBytes,
                PFLASH1_FILE_OFFSET, pflash1Len, "", "TC179x PFLASH1", true, false, true, log);
            commit = true;
        }
        catch (LockException | ghidra.program.model.address.AddressOverflowException e) {
            throw new LoadException("Failed to create split flash blocks", e);
        }
        finally {
            program.endTransaction(tx, commit);
        }
    }
}
