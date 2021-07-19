package be.seeseemelk.tiside.tis;

/**
 * Handles events coming from the TisService.
 */
public interface TisHandler
{
	/**
	 * Executed when the TIS is started.
	 */
	void onStarted();

	/**
	 * Executed when the TIS is stopped.
	 */
	void onStopped();

	/**
	 * Executed when an exception is thrown.
	 *
	 * @param e The exception that was thrown.
	 */
	void onException(Exception e);

	/**
	 * Executed when there is output available.
	 *
	 * @param output The output that was read.
	 */
	void onOutput(char output);

	void progressStart();

	void progressStop();

	void progressSet(int step, int max);
}
