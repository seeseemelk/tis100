package be.seeseemelk.tiside.tis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TisNode
{
	private final int coreId;
	private final List<TisInstruction> instructions = new ArrayList<>();
	private final Map<Integer, TisInstruction> updatedInstructions = new HashMap<>();

	public TisNode(int core)
	{
		this.coreId = core;
	}

	public int getCoreId()
	{
		return coreId;
	}

	public List<TisInstruction> getInstructions()
	{
		return instructions;
	}

	public void updateInstruction(int index, TisInstruction instruction)
	{
		instructions.add(instruction);
		updatedInstructions.put(index, instruction);
	}

	public Map<Integer, TisInstruction> getUpdatedInstructions()
	{
		return updatedInstructions;
	}

	public void clearUpdates()
	{
		updatedInstructions.clear();
	}
}
