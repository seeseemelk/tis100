package be.seeseemelk.tiside.tis;

import be.seeseemelk.tiside.spi.SPI;
import be.seeseemelk.tiside.spi.SPIException;

/**
 * Manages a TisTap debugger.
 */
public class TisTap implements AutoCloseable
{
	private SPI spi;

	public TisTap(SPI spi)
	{
		this.spi = spi;
	}

	/**
	 * Gets the status register of the TIS.
	 *
	 * @return The value of the status register.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public int getStatus() throws TisException, InterruptedException
	{
		transfer(0x01);
		return transfer(0x00);
	}

	/**
	 * Gets whether the TIS is currently running or not.
	 *
	 * @return {@code true} if the TIS is running, {@code false} if it is paused.
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public boolean isRunning() throws TisException, InterruptedException
	{
		return (getStatus() & 0x01) != 0;
	}

	/**
	 * Tests whether there is output available for reading.
	 *
	 * @return {@code true} if there is output for reading, {@code false} if there
	 * is no output available.
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public boolean hasOutput() throws TisException, InterruptedException
	{
		return (getStatus() & 0x02) != 0;
	}

	/**
	 * Gets a character of output.
	 * This is only guaranteed to return one valid character if hasOutput()
	 * returned {@code true}
	 *
	 * @return The read character.
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public char getOutput() throws TisException, InterruptedException
	{
		transfer(0x04);
		int result = transfer(0x00);
		return (char) result;
	}

	/**
	 * Lets the TIS continue execution.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void run() throws TisException, InterruptedException
	{
		transfer(0x03);
	}

	/**
	 * Pauses execution of the TIS.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void pause() throws TisException, InterruptedException
	{
		transfer(0x02);
	}

	/**
	 * Steps executed of the TIS.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void step() throws TisException, InterruptedException
	{
		transfer(0x60);
	}

	/**
	 * Sets the NodeTap command to execute.
	 *
	 * @param command The NodeTap command to execute.
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapSetCommand(int command) throws TisException, InterruptedException
	{
		transfer(0x20 | (command & 0x0F));
	}

	/**
	 * Executes a NodeTap command.
	 *
	 * @param core The ID of the core to send the command to.
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapExec(int core) throws TisException, InterruptedException
	{
		transfer(0x10 | (core & 0x0F));
	}

	/**
	 * Finished a NodeTap command.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapStop() throws TisException, InterruptedException
	{
		transfer(0x1F);
	}

	/**
	 * Reads a word from a NodeTap.
	 *
	 * @return The word read from the NodeTap.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public int nodetapRead() throws TisException, InterruptedException
	{
		int value = 0;
		transfer(0x30);
		value |= transfer(0x30);
		value |= transfer(0x30) << 8;
		value |= (transfer(0x00) & 0x3) << 16;
		return value;
	}

	/**
	 * Writes a instruction to the NodeTap.
	 *
	 * @param word The instruction to write to the NodeTap.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapWriteInstructionWord(int word) throws TisException, InterruptedException
	{
		transfer(0x40 | ((word >> 0) & 0x0F));
		transfer(0x40 | ((word >> 4) & 0x0F));
		transfer(0x40 | ((word >> 8) & 0x0F));
		transfer(0x50 | ((word >> 12) & 0x0F));
		transfer(0x50 | ((word >> 16) & 0x03));
	}

	/**
	 * Writes a nibble to the NodeTap.
	 *
	 * @param nibble The nibble to write.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapWriteNibble(int nibble) throws TisException, InterruptedException
	{
		transfer(0x40 | (nibble & 0x0F));
	}

	/**
	 * Gets the state of a node.
	 *
	 * @return The state of a node.
	 */
	public NodeState nodetapState(int node) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x3);
		nodetapExec(node);
		int result = nodetapRead();
		nodetapStop();

		for (NodeState state : NodeState.values())
		{
			if (state.value == result)
				return state;
		}
		return NodeState.INVALID;
	}

	/**
	 * Reads the IP register of a core.
	 *
	 * @param core The core to read the IP register of.
	 *
	 * @return The value of the IP register.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public int nodetapReadIP(int core) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x4);
		nodetapExec(core);
		int result = nodetapRead();
		nodetapStop();
		return result;
	}

	/**
	 * Sets the IP register of a core.
	 *
	 * @param core The core to change.
	 * @param ip The value to set the IP register to.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapWriteIP(int core, int ip) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x5);
		nodetapWriteNibble(ip);
		nodetapExec(core);
		nodetapStop();
	}

	/**
	 * Reads the accumulator register of a core.
	 *
	 * @param core The core to read the accumulator register of.
	 *
	 * @return The value of the accumulator register.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public int nodetapReadACC(int core) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x6);
		nodetapExec(core);
		int result = nodetapRead();
		nodetapStop();
		return result;
	}

	/**
	 * Sets the accumulator register of a core.
	 *
	 * @param core The core to change.
	 * @param acc The value to set the accumulator register to.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapWriteACC(int core, int acc) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x7);
		nodetapWriteNibble(acc);
		nodetapExec(core);
		nodetapStop();
	}

	/**
	 * Reads the current instruction.
	 *
	 * @param core The core of which to read the instruction.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public int nodetapReadInstruction(int core) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x1);
		nodetapExec(core);
		int result = nodetapRead();
		nodetapStop();
		return result;
	}

	/**
	 * Sets the value of the current instruction.
	 *
	 * @param core The core of which to change an instruction.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void nodetapWriteInstruction(int core, int instruction) throws TisException, InterruptedException
	{
		nodetapSetCommand(0x2);
		nodetapWriteInstructionWord(instruction);
		nodetapExec(core);
		nodetapStop();
	}

	@Override
	public void close() throws Exception
	{
		spi.close();
	}

	/**
	 * Transfers a byte of data to the device.
	 *
	 * @param data The data to send.
	 * @return The data that was received.
	 *
	 * @throws TisException if the data could not be transferred.
	 * @throws InterruptedException
	 */
	private int transfer(int data) throws TisException, InterruptedException
	{
		try
		{
			return spi.transfer(data);
		}
		catch (SPIException e)
		{
			throw new TisException(e);
		}
	}
}
