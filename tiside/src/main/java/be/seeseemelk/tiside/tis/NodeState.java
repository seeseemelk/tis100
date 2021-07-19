package be.seeseemelk.tiside.tis;

public enum NodeState
{
	FETCH(0b0000),
	FETCH_WAIT(0b0001),
	FETCH_END(0b0010),
	EXECUTE(0b0011),
	COMMIT(0b0100),
	COMMIT_WAIT(0b0101),
	COMMIT_END(0b0110),
	FINISH(0b0111),
	FATAL_FETCH(0b1000),
	FATAL_FETCH_WAIT(0b1001),
	FATAL_FETCH_END(0b1010),
	FATAL(0b1111),
	INVALID(-1);

	public final int value;

	private NodeState(int value)
	{
		this.value = value;
	}
}
