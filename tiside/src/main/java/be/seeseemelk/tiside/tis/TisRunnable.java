package be.seeseemelk.tiside.tis;

@FunctionalInterface
public interface TisRunnable
{
	void run() throws TisException, InterruptedException;
}
