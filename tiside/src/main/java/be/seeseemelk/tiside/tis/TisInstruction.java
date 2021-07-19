package be.seeseemelk.tiside.tis;

public class TisInstruction
{
	public static final int OPCODE_ADD_REG = 0x0;
	public static final int OPCODE_ADD_LIT = 0x1;
	public static final int OPCODE_SUB_REG = 0x2;
	public static final int OPCODE_SUB_LIT = 0x3;
	public static final int OPCODE_MOV_REG_REG = 0x4;
	public static final int OPCODE_MOV_LIT_REG = 0x5;
	public static final int OPCODE_JRO_REG = 0x6;
	public static final int OPCODE_JRO_LIT = 0x7;
	public static final int OPCODE_SWP = 0x8;
	public static final int OPCODE_SAV = 0x9;
	public static final int OPCODE_NEG = 0xA;
	public static final int OPCODE_JMP = 0xB;
	public static final int OPCODE_JEZ = 0xC;
	public static final int OPCODE_JNZ = 0xD;
	public static final int OPCODE_JGZ = 0xE;
	public static final int OPCODE_JLZ = 0xF;

	public static final int REG_NIL = 0x0;
	public static final int REG_ACC = 0x1;
	public static final int REG_ANY = 0x2;
	public static final int REG_LAST = 0x3;
	public static final int REG_LEFT = 0x4;
	public static final int REG_RIGHT = 0x5;
	public static final int REG_UP = 0x6;
	public static final int REG_DOWN = 0x7;

	private final int word;

	public TisInstruction(int word)
	{
		this.word = word;
	}

	public static TisInstruction fromOpcode(int opcode)
	{
		return new TisInstruction(opcode << 14);
	}

	public static TisInstruction fromOpcodeReg(int opcode, int src)
	{
		return new TisInstruction((opcode << 14) | (src << 8));
	}

	public static TisInstruction fromOpcodeLit(int opcode, int lit)
	{
		return new TisInstruction((opcode << 14) | (lit << 0));
	}

	public static TisInstruction fromOpcodeRegReg(int opcode, int dest, int src)
	{
		return new TisInstruction((opcode << 14) | (dest << 11) | (src << 8));
	}

	public static TisInstruction fromOpcodeRegLit(int opcode, int dest, int lit)
	{
		return new TisInstruction((opcode << 14) | (dest << 11) | (lit));
	}

	public int getWord()
	{
		return word;
	}

	public int opcode()
	{
		return word >> 14;
	}

	public static String registerName(int register)
	{
		switch (register)
		{
		case 0: return "NIL";
		case 1: return "ACC";
		case 2: return "ANY";
		case 3: return "LAST";
		case 4: return "LEFT";
		case 5: return "RIGHT";
		case 6: return "UP";
		case 7: return "DOWN";
		default: return "(INVALID REGISTER)";
		}
	}

	public String source()
	{
		return registerName((word >> 8) & 7);
	}

	public String destination()
	{
		return registerName((word >> 11) & 7);
	}

	public int literal()
	{
		return word & 0x3FF;
	}

	public String asBinary()
	{
		String bin = Integer.toBinaryString(word);
		return String.format("%18s", bin).replace(' ', '0');
	}

	public String asAssembly()
	{
		switch (opcode())
		{
		case 0x0: return String.format("ADD %s", source());
		case 0x1: return String.format("ADD %d", literal());
		case 0x2: return String.format("SUB %s", source());
		case 0x3: return String.format("SUB %d", literal());
		case 0x4: return String.format("MOV %s %s", source(), destination());
		case 0x5: return String.format("MOV %d %s", literal(), destination());
		case 0x6: return String.format("JRO %s", source());
		case 0x7: return String.format("JRO %d", literal());
		case 0x8: return String.format("SWP");
		case 0x9: return String.format("SAV");
		case 0xA: return String.format("NEG");
		case 0xB: return String.format("JMP %d", literal());
		case 0xC: return String.format("JEZ %d", literal());
		case 0xD: return String.format("JNZ %d", literal());
		case 0xE: return String.format("JGZ %d", literal());
		case 0xF: return String.format("JLZ %d", literal());
		default:
			return "(INVALID OPCODE)";
		}
	}

	public static TisInstruction compile(String instruction) throws TisException
	{
		String[] parts = instruction.strip().toUpperCase().split(" ");
		String opcode = parts[0];
		String arg1 = (parts.length >= 2) ? parts[1] : null;
		String arg2 = (parts.length >= 3) ? parts[2] : null;
		switch (opcode)
		{
		case "ADD":
			if (isReg(arg1))
				return fromOpcodeReg(OPCODE_ADD_REG, getReg(arg1));
			else
				return fromOpcodeLit(OPCODE_ADD_LIT, getLit(arg1));
		case "SUB":
			if (isReg(arg1))
				return fromOpcodeReg(OPCODE_SUB_REG, getReg(arg1));
			else
				return fromOpcodeLit(OPCODE_SUB_REG, getLit(arg1));
		case "MOV":
			if (isReg(arg1))
				return fromOpcodeRegReg(OPCODE_MOV_REG_REG, getReg(arg2), getReg(arg1));
			else
				return fromOpcodeRegLit(OPCODE_MOV_LIT_REG, getReg(arg2), getLit(arg1));
		case "JRO":
			if (isReg(arg1))
				return fromOpcodeReg(OPCODE_JRO_REG, getReg(arg1));
			else
				return fromOpcodeLit(OPCODE_JRO_LIT, getLit(arg1));
		case "SWP":
			return fromOpcode(OPCODE_SWP);
		case "SAV":
			return fromOpcode(OPCODE_SAV);
		case "NEG":
			return fromOpcode(OPCODE_NEG);
		case "JMP":
			return fromOpcodeLit(OPCODE_JMP, getLit(arg1));
		case "JEZ":
			return fromOpcodeLit(OPCODE_JEZ, getLit(arg1));
		case "JNZ":
			return fromOpcodeLit(OPCODE_JNZ, getLit(arg1));
		case "JGZ":
			return fromOpcodeLit(OPCODE_JGZ, getLit(arg1));
		case "JLZ":
			return fromOpcodeLit(OPCODE_JLZ, getLit(arg1));
		default:
			throw new TisException("Invalid opcode: " + opcode);
		}
	}

	public static boolean isReg(String arg)
	{
		return tryGetReg(arg) != -1;
	}

	public static int tryGetReg(String arg)
	{
		switch (arg)
		{
		case "NIL": return REG_NIL;
		case "ACC": return REG_ACC;
		case "ANY": return REG_ANY;
		case "LAST": return REG_LAST;
		case "LEFT": return REG_LEFT;
		case "RIGHT": return REG_RIGHT;
		case "UP": return REG_UP;
		case "DOWN": return REG_DOWN;
		default: return -1;
		}
	}

	public static int getReg(String arg) throws TisException
	{
		int result = tryGetReg(arg);
		if (result == -1)
			throw new TisException("Invalid register: " + arg);
		return result;
	}

	public static int getLit(String arg) throws TisException
	{
		try
		{
			return Integer.parseInt(arg);
		}
		catch (NumberFormatException e)
		{
			throw new TisException("Invalid literal", e);
		}
	}
}
