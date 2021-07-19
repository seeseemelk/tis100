package be.seeseemelk.tiside.tis;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TisInstructionTest
{

	@Test
	void testDecodeMovUpDown()
	{
		TisInstruction instr = opcode("010011111000000000");
		assertEquals("MOV UP DOWN", instr.asAssembly());
	}

	@Test
	void testMov73Down() throws TisException
	{
		TisInstruction instr = decode("MOV 73 DOWN");
		assertEquals("010111100001001001", instr.asBinary());
	}

	@Test
	public void testAdd1() throws TisException
	{
		TisInstruction instr = decode("ADD 1");
		assertEquals("000100000000000001", instr.asBinary());
	}

	private static TisInstruction opcode(String opcode)
	{
		return new TisInstruction(Integer.parseInt(opcode, 2));
	}

	private static TisInstruction decode(String opcode) throws TisException
	{
		return TisInstruction.compile(opcode);
	}
}
