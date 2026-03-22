# TC179x Split Flash Binary Loader

Ghidra extension that loads raw TC1791/TC1793/TC1798 firmware binaries into the correct
non-contiguous flash banks:

- file offset `0x000000` -> `PFLASH0` at `0x80000000`
- file offset `0x200000` -> `PFLASH1` at `0x80800000`

Supported raw binary sizes:
- `0x300000` (3 MB) -> TC1791 `384`
- `0x400000` (4 MB) -> TC1791 `512`, TC1793, TC1798

Build:

```sh
cd TC179xSplitFlashLoader
gradle buildExtension
```

Install the generated zip from `dist/` using `File -> Install Extensions` in Ghidra.
