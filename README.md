# Ghidra TC179x / TC1798 Processor Spec

Adds support for the **Infineon TC179x** and **TC1798** (AUDO MAX family) to Ghidra's
TriCore processor module.

Two processor spec files are provided:

| File | Chip(s) | ADC kernels |
|------|---------|-------------|
| `tc179x.pspec` | TC1791, TC1793 | ADC0, ADC1, ADC2 |
| `tc1798.pspec` | TC1798 | ADC0, ADC1, ADC2, ADC3 |

The `tc179x.pspec` was developed and tested against the **TC1791**. The TC1793 shares
the same peripheral address map and uses the same file. The TC1798 adds a fourth ADC
kernel (ADC3 at `0xF010_1C00`) and uses `tc1798.pspec`.

**Note:** SHE (Secure Hardware Extension) registers appear in both files. SHE is present
in TC1798 and in some TC1793 variants (e.g. SAK-TC1793F-512F270EF), but not in the
base TC1791. Only the three publicly documented SHE registers are listed — the full SHE
register map is NDA-protected.

## TC179x Family Comparison

| Feature         | TC1791          | TC1793          | TC1798          |
|-----------------|-----------------|-----------------|-----------------|
| CPU speed       | 240 MHz         | 270 MHz         | 300 MHz         |
| Program Flash   | 4 MB            | 4 MB            | 4 MB            |
| Data Flash      | 192 KB          | 192 KB          | 192 KB          |
| DSPR            | 128 KB          | 128 KB          | 128 KB          |
| LMU SRAM        | 128 KB          | 128 KB          | 128 KB          |
| PSPR            | 32 KB           | 32 KB           | 32 KB           |
| FPU             | Yes             | Yes             | Yes             |
| DMA channels    | 16              | 16              | 16              |
| Safe DMA (SDMA) | 8               | 8               | 8               |
| FlexRay         | Yes (1 module)  | Yes (1 module)  | Yes (1 module)  |
| CAN nodes       | 4               | 4               | 4               |
| SSC channels    | 4               | 4               | 4               |
| ASC channels    | 2               | 2               | 2               |
| MLI interfaces  | 2               | 2               | 2               |
| ADC channels    | 44 (3 kernels)  | 44 (3 kernels)  | 64 (4 kernels)  |
| FADC channels   | 4               | 4               | 4               |
| SENT channels   | 8               | 8               | 8               |
| FCE             | Yes             | Yes             | Yes             |
| SHE             | Some variants   | Some variants   | Yes             |
| Package         | BGA292          | BGA416          | BGA516          |

## Contents

| File | Description |
|------|-------------|
| `tc179x.pspec` | Processor spec for TC1791 / TC1793 (ADC0–ADC2, no ADC3) |
| `tc1798.pspec` | Processor spec for TC1798 (ADC0–ADC3) |
| `patches/tc179x.ldefs.patch` | Patch adding TC179x and TC1798 language entries to Ghidra's `tricore.ldefs` |

## Installation

### 1. Copy the processor specs

```sh
sudo cp tc179x.pspec tc1798.pspec /opt/ghidra/Ghidra/Processors/tricore/data/languages/
```

### 2. Patch the language definitions

```sh
cd /opt/ghidra/Ghidra/Processors/tricore/data/languages
sudo patch -p1 < /path/to/patches/tc179x.ldefs.patch
```

Restart Ghidra. Two new variants will be available when importing a binary:

- **"Infineon Tricore Embedded Processor TC179x"** (`tricore:LE:32:tc179x`) — for TC1791 / TC1793
- **"Infineon Tricore Embedded Processor TC1798"** (`tricore:LE:32:tc1798`) — for TC1798

## Notes

- The patch only adds the TC179x and TC1798 entries; it does not touch any existing language
  definitions.
- If the patch fails after a Ghidra upgrade, inspect `tricore.ldefs` for context changes around
  the `</language_definitions>` closing tag and regenerate the patch accordingly.
- Tested against Ghidra 11.x (`tricore.ldefs` version 1.7).
- All SFR addresses cross-referenced against the TC1798 User Manual V1.2 and TC1791/TC1793/TC1798
  datasheets.
