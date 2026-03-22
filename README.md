# Ghidra TC179x / TC1798 Processor Spec

Adds support for the **Infineon TC179x** and **TC1798** (AUDO MAX family) to Ghidra's
TriCore processor module.

Three processor spec files are provided:

| File | Use for | Flash layout | ADC kernels |
|------|---------|--------------|-------------|
| `tc1791_384.pspec` | TC1791 `384` variants only | `PFLASH0 = 2 MB`, `PFLASH1 = 1 MB` | ADC0, ADC1, ADC2 |
| `tc179x.pspec` | TC1791 `512` variants and all TC1793 | `PFLASH0 = 2 MB`, `PFLASH1 = 2 MB` | ADC0, ADC1, ADC2 |
| `tc1798.pspec` | TC1798 | `PFLASH0 = 2 MB`, `PFLASH1 = 2 MB` | ADC0, ADC1, ADC2, ADC3 |

The `tc1791_384.pspec` is for the TC1791 3 MB devices where `PFLASH0 = 2 MB` and
`PFLASH1 = 1 MB`. The `tc179x.pspec` covers all 4 MB TC1791 / TC1793 devices with
`PFLASH0 = 2 MB` and `PFLASH1 = 2 MB`. The TC1798 adds a fourth ADC kernel (ADC3 at
`0xF010_1C00`) and uses `tc1798.pspec`. Raw binaries should be imported at base address
`0x80000000`.

**Note:** SHE (Secure Hardware Extension) registers appear in all three processor spec
files. SHE is present on TC1798 and on some TC1791/TC1793 variants. The public
documentation names only a small number of SHE registers and directs readers to contact
Infineon for further SHE details.

## TC179x Family Comparison

| Feature         | TC1791 384      | TC1791 512      | TC1793          | TC1798          |
|-----------------|-----------------|-----------------|-----------------|-----------------|
| CPU speed range | 200 MHz         | 200-240 MHz     | 200-270 MHz     | 300 MHz         |
| Total PFLASH    | 3 MB            | 4 MB            | 4 MB            | 4 MB            |
| PFLASH0         | 2 MB            | 2 MB            | 2 MB            | 2 MB            |
| PFLASH1         | 1 MB            | 2 MB            | 2 MB            | 2 MB            |
| DFLASH          | 192 KB          | 192 KB          | 192 KB          | 192 KB          |
| DSPR            | 128 KB          | 128 KB          | 128 KB          | 128 KB          |
| LMU SRAM        | 128 KB          | 128 KB          | 128 KB          | 128 KB          |
| PSPR            | 32 KB           | 32 KB           | 32 KB           | 32 KB           |
| FPU             | Yes             | Yes             | Yes             | Yes             |
| DMA channels    | 16              | 16              | 16              | 16              |
| Safe DMA (SDMA) | 8               | 8               | 8               | 8               |
| FlexRay (E-Ray) | Some variants   | Some variants   | Some variants   | Some variants   |
| CAN nodes       | 4               | 4               | 4               | 4               |
| SSC channels    | 4               | 4               | 4               | 4               |
| ASC channels    | 2               | 2               | 2               | 2               |
| MLI interfaces  | 2               | 2               | 2               | 2               |
| ADC inputs      | 44 (3 kernels)  | 44 or 48 (3 kernels) | 44 (3 kernels)  | 64 (4 kernels)  |
| FADC inputs     | 4               | 4               | 4               | 4               |
| SENT channels   | 8               | 8               | 8               | 8               |
| FCE             | Yes             | Yes             | Yes             | Yes             |
| SHE             | Some variants   | Some variants   | Some variants   | Yes             |
| Package         | BGA292          | BGA292          | BGA416          | BGA516          |

## Derivative Split

| Ghidra language | Chip(s) | Flash layout |
|-----------------|---------|--------------|
| `tricore:LE:32:tc1791_384` | TC1791 `384` variants only | `PFLASH0 = 2 MB`, `PFLASH1 = 1 MB` |
| `tricore:LE:32:tc179x` | TC1791 `512` variants, all TC1793 | `PFLASH0 = 2 MB`, `PFLASH1 = 2 MB` |
| `tricore:LE:32:tc1798` | TC1798 | `PFLASH0 = 2 MB`, `PFLASH1 = 2 MB`, plus ADC3 |

TC1791 `384` parts confirmed from the datasheet:

- `SAK-TC1791F-384F200EL`
- `SAK-TC1791F-384F200EP`
- `SAK-TC1791S-384F200EP`
- `SAK-TC1791N-384F200EP`

## Contents

| File | Description |
|------|-------------|
| `tc1791_384.pspec` | Processor spec for TC1791 `384` variants (ADC0-ADC2, `PFLASH1 = 1 MB`) |
| `tc179x.pspec` | Processor spec for TC1791 `512` variants / TC1793 (ADC0-ADC2, `PFLASH1 = 2 MB`) |
| `tc1798.pspec` | Processor spec for TC1798 (ADC0–ADC3) |
| `patches/tc179x.ldefs.patch` | Patch adding TC179x, TC1791_384, and TC1798 language entries to Ghidra's `tricore.ldefs` |
| `TC179xSplitFlashLoader/` | Ghidra extension that loads raw split-flash binaries into `PFLASH0` / `PFLASH1` automatically |

## Installation

### 1. Copy the processor specs

```sh
sudo cp tc1791_384.pspec tc179x.pspec tc1798.pspec /opt/ghidra/Ghidra/Processors/tricore/data/languages/
```

### 2. Patch the language definitions

```sh
cd /opt/ghidra/Ghidra/Processors/tricore/data/languages
sudo patch -p1 < /path/to/patches/tc179x.ldefs.patch
```

Restart Ghidra. Three new variants will be available when importing a binary:

- **"Infineon Tricore Embedded Processor TC1791 (384 variant, 3MB flash)"** (`tricore:LE:32:tc1791_384`) — for TC1791 `384`
- **"Infineon Tricore Embedded Processor TC1791 / TC1793 (512 variant, 4MB flash)"** (`tricore:LE:32:tc179x`) — for TC1791 `512` / TC1793
- **"Infineon Tricore Embedded Processor TC1798 (512 variant, 4MB flash)"** (`tricore:LE:32:tc1798`) — for TC1798

### 3. Optional: install the split-flash loader extension

The included loader extension recognizes 3 MB and 4 MB TC179x raw binaries and maps them
directly into `PFLASH0` / `PFLASH1` during import. This avoids the manual Memory Map steps
described below.

Build the extension:

```sh
cd TC179xSplitFlashLoader
gradle buildExtension
```

This creates a zip file in `TC179xSplitFlashLoader/dist/`.

Install it in Ghidra:

- open `File -> Install Extensions`
- click the `+` button
- select the generated `ghidra_12.2_DEV_..._TC179xSplitFlashLoader.zip`
- restart Ghidra

After installation, choose **TC179x Split Flash Binary Loader** when importing a raw `.bin`.
The loader offers the same three language choices (`tc1791_384`, `tc179x`, `tc1798`) and
creates the flash blocks from the correct file offsets automatically.

## Loading Raw Flash Binaries

If you use the loader extension above, Ghidra maps the flash banks automatically.

If you do not use the loader extension, the pspec files create the correct memory blocks and
addresses, but they do not attach file offsets automatically. After importing a raw `.bin`,
you still need to map the file bytes to the existing `PFLASH0` / `PFLASH1` blocks in Ghidra.

### 1. Pick the correct language when importing

- Use `tricore:LE:32:tc1791_384` for TC1791 `384` images
- Use `tricore:LE:32:tc179x` for TC1791 `512` and all TC1793 images
- Use `tricore:LE:32:tc1798` for TC1798 images

Import as **Raw Binary** and set the **base address to `0x80000000`**. If the file is
loaded at `0x00000000`, Ghidra will create a generic `ram` block at the wrong address and
leave `PFLASH0` / `PFLASH1` uninitialized.

### 2. Open the Memory Map

- In Ghidra, open `Window -> Memory Map`
- You should already see `PFLASH0` and `PFLASH1` from the pspec
- Do not add new blocks at the same addresses; edit the existing ones

### 3. Map file bytes to the flash blocks

For 3 MB devices (`tc1791_384`):

| Block | Start address | Length | File offset |
|------|---------------|--------|-------------|
| `PFLASH0` | `0x80000000` | `0x200000` | `0x000000` |
| `PFLASH1` | `0x80800000` | `0x100000` | `0x200000` |

For 4 MB devices (`tc179x`, `tc1798`):

| Block | Start address | Length | File offset |
|------|---------------|--------|-------------|
| `PFLASH0` | `0x80000000` | `0x200000` | `0x000000` |
| `PFLASH1` | `0x80800000` | `0x200000` | `0x200000` |

When editing each block:

- select the existing `PFLASH0` or `PFLASH1` row
- choose **File Bytes**
- select your imported binary in the `File Bytes` dropdown
- set the correct `File Offset`
- keep flash blocks `Read = on`, `Execute = on`, `Write = off`

If you try to add a new block at `0x80000000` or `0x80800000`, Ghidra will report a block
address conflict because those blocks already exist.

## Notes

- The patch only adds the TC179x, TC1791_384, and TC1798 entries; it does not touch any
  existing language definitions.
- The initial imported pspec had an inherited off-by-one bug in the `PSPR`, `LDRAM`, and
  `LMURAM` block lengths (`0x7fff` / `0x1ffff` instead of `0x8000` / `0x20000`). This was
  caused by using the inclusive end offset as the XML `length` value.
- If the patch fails after a Ghidra upgrade, inspect `tricore.ldefs` for context changes around
  the `</language_definitions>` closing tag and regenerate the patch accordingly.
- Tested against Ghidra 11.x (`tricore.ldefs` version 1.7).
- All SFR addresses cross-referenced against the TC1798 User Manual V1.2 and TC1791/TC1793/TC1798
  datasheets.
