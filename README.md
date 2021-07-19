# FPGA TIS-100
This is an FPGA implementation of the TIS-100. The repository includes an Java
IDE which can visualise the state of the TIS-100.
The IDE uses an FT-232 in bitbang mode in order to talk to the TIS-100.

# Architecture
The system contains 12 T21 nodes connected in a 4x3 grid.

## Debugging
The TIS-100 contains a TisTap. The TisTap is a debugging tool which allows a
host to read and write the state of the TIS-100 over an SPI bus. Each node
contains a NodeTap which allows access to a single node. The TisTap is
connected to the NodeTaps, allowing a host access to each individual NodeTap.

# Instruction Set
As the official TIS-100 ISA does not contain any bytecode, a custom bytecode
format had to be developed.
Each instruction comes in three formats:

|----|----------------------|
|Type| Format               |
|----|----------------------|
| A  | `IIIIDDDSSSxxxxxxxx` |
| B  | `IIIIDDDLLLLLLLLLLL` |
| C  | `IIIIxxxAAAAAAAAAAA` |
|----|----------------------|

|-----------|----------------------|
| Character | Meaning              |
|-----------|----------------------|
| `I`       | Instruction opcode   |
| `D`       | Destination Register |
| `S`       | Source Register      |
| `A`       | Address              |
| `L`       | Literal              |
| `x`       | Unused (don't care)  |
|-----------|----------------------|

## Registers
Each node contains 8 addressable registers, namely:

|-------|--------------|
| Index | Name         |
| `000` | NIL          |
| `001` | ACC          |
| `010` | ANY          |
| `011` | LAST         |
| `100` | WEST / LEFT  |
| `101` | EAST / RIGHT |
| `110` | NORTH / UP   |
| `111` | SOUTH / DOWN |
|-------|--------------|

## Opcodes
There are a total of 16 opcodes:

|--------|-------------------|
| Opcode | Assembly          |
| `0000` | `ADD <SRC>`       |
| `0001` | `ADD <LIT>`       |
| `0010` | `SUB <SRC>`       |
| `0011` | `SUB <LIT>`       |
| `0100` | `MOV <SRC> <DST>` |
| `0101` | `MOV <LIT> <DST>` |
| `0110` | `JRO <SRC>`       |
| `0111` | `JRO <LIT>`       |
| `1000` | `SWP`             |
| `1001` | `SAV`             |
| `1010` | `NEG`             |
| `1011` | `JMP <ADDR>`      |
| `1100` | `JEZ <ADDR>`      |
| `1101` | `JNZ <ADDR>`      |
| `1110` | `JGZ <ADDR>`      |
| `1111` | `JLZ <ADDR>`      |
|--------|-------------------|
