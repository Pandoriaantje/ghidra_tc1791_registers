# Ghidra TC179x Processor Spec

Adds support for the **Infineon TC179x** (AUDO family) to Ghidra's TriCore processor module.

The TC179x family comprises three members: **TC1791**, **TC1793**, and **TC1798**. The processor
spec (`tc179x.pspec`) was developed and tested against the **TC1791**. The core SFR addresses and
memory layout are largely shared across the family, but the TC1793 and TC1798 include additional
peripherals (FlexRay, FCE, FPU, additional ADC/SSC/GPTA, larger Flash) that are not covered by
this spec. It will still be useful for those variants but should be treated as incomplete for them.

## Contents

| File | Description |
|------|-------------|
| `tc179x.pspec` | Processor spec: memory map, default memory blocks, and SFR symbols for the TC1791 |
| `patches/tc179x.ldefs.patch` | Patch to add the TC179x language entry to Ghidra's `tricore.ldefs` |

## Installation

### 1. Copy the processor spec

```sh
sudo cp tc179x.pspec /opt/ghidra/Ghidra/Processors/tricore/data/languages/
```

### 2. Patch the language definitions

```sh
cd /opt/ghidra/Ghidra/Processors/tricore/data/languages
sudo patch -p1 < /path/to/patches/tc179x.ldefs.patch
```

Restart Ghidra. The variant **"Infineon Tricore Embedded Processor TC179x"** (`tricore:LE:32:tc179x`) will then be available when importing a binary.

## Notes

- The patch only adds the TC179x entry; it does not touch any existing language definitions.
- If the patch fails after a Ghidra upgrade, inspect `tricore.ldefs` for context changes around the `</language_definitions>` closing tag and regenerate the patch accordingly.
- Tested against Ghidra 11.x (`tricore.ldefs` version 1.7).
