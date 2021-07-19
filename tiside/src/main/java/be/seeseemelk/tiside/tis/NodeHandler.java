package be.seeseemelk.tiside.tis;

import java.util.List;

/**
 * Handles events for a single node.
 */
public interface NodeHandler
{
	/**
	 * Executed when the state of the node was read.
	 *
	 * @param state The node of the state.
	 */
	void onState(NodeState state);

	/**
	 * Executed when the IP register was read.
	 *
	 * @param ip The value of the IP register.
	 */
	void onIPRead(int ip);

	/**
	 * Executed when the accumulator register was read.
	 *
	 * @param acc The value of the accumulator register.
	 */
	void onACCRead(int acc);

	/**
	 * Executed when the ROM has been read.
	 *
	 * @param instructions The instructions stored in the rom.
	 */
	void onRomRead(List<TisInstruction> instructions);
}
