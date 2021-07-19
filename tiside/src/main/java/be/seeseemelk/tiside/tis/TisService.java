package be.seeseemelk.tiside.tis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TisService
{
	private final TisTap tap;
	private final TisHandler handler;
	private final Map<Integer, NodeHandler> nodeHandlers = new HashMap<>();
//	private final Set<Integer> nodesToUpdate = new HashSet<>();
	private boolean firstRun = true;
	private boolean running = false;
	private boolean stopThread = false;
	private Optional<Thread> thread = Optional.empty();
	private LinkedBlockingQueue<TisRunnable> queue = new LinkedBlockingQueue<TisRunnable>();
	private LinkedBlockingQueue<TisRunnable> stoppedQueue = new LinkedBlockingQueue<TisRunnable>();
	private AtomicInteger stepsRequired = new AtomicInteger(0);
	private AtomicInteger stepsExecuted = new AtomicInteger(0);

	public TisService(TisTap tap, TisHandler handler)
	{
		this.tap = tap;
		this.handler = handler;
	}

	public TisHandler getHandler()
	{
		return handler;
	}

	public void start()
	{
		if (thread.isEmpty())
		{
			stopThread = false;
			Thread thread = new Thread(this::main, "Tis");
			this.thread = Optional.of(thread);
			thread.start();
		}
	}

	/**
	 * Registers a handler for a specific core.
	 *
	 * @param core The core for which to listen.
	 * @param handler The handler for the core.
	 */
	public void registerNodeHandler(int core, NodeHandler handler)
	{
		nodeHandlers.put(core, handler);
		refreshNodeFully(core);
	}

	private void main()
	{
		while (!stopThread)
		{
			try
			{
				synchronized (tap)
				{
					if (stepsRequired.get() > 0)
					{
						handler.progressStart();
						handler.progressSet(0, stepsRequired.get());
					}

					TisRunnable runnable;
					while ((runnable = queue.poll()) != null)
						runnable.run();

					boolean running = tap.isRunning();
					if (running != this.running || firstRun)
					{
						this.running = running;
						if (running)
							onStarted();
						else
							onStopped();
					}

					if (!running)
					{
						while ((runnable = stoppedQueue.poll()) != null)
							runnable.run();
					}

					if (tap.hasOutput())
					{
						handler.onOutput(tap.getOutput());
					}

					firstRun = false;

					if (stepsRequired.get() > 0 && stepsExecuted.get() >= stepsRequired.get())
					{
						stepsRequired.set(0);
						stepsExecuted.set(0);
						handler.progressStop();
					}
				}
			}
			catch (InterruptedException e)
			{
			}
			catch (Exception e)
			{
				System.err.println("An exception occured:");
				e.printStackTrace();
				handler.onException(e);
			}
		}
	}

	public void stop()
	{
		if (thread.isPresent())
		{
			stopThread = true;
			thread.ifPresent(thread ->
			{
				try
				{
					thread.interrupt();
					thread.join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * Lets the TIS continue.
	 */
	public void run()
	{
		stepsRequired.addAndGet(1);
		queue.add(() ->
		{
			tap.run();
			executedStep();
		});
	}

	/**
	 * Pauses the TIS.
	 */
	public void pause()
	{
		stepsRequired.addAndGet(1);
		queue.add(() ->
		{
			tap.pause();
			executedStep();
		});
	}

	/**
	 * Executed a single clock cycle.
	 */
	public void step()
	{
		stepsRequired.addAndGet(1);
		queue.add(() ->
		{
			tap.step();
			executedStep();
		});
		refreshNodes();
	}

	/**
	 * Writes a set of instructions to memory.
	 *
	 * @param core The core to write to.
	 * @param instructions The instructions to write.
	 */
	public void writeInstructions(int core, List<TisInstruction> instructions)
	{
		stepsRequired.addAndGet(16+2);
		stoppedQueue.add(() ->
		{
			System.out.format("Updating ROM of %d%n", core);
			int ip = tap.nodetapReadIP(core);
			executedStep();
			for (int i = 0; i < instructions.size(); i++)
			{
				tap.nodetapWriteIP(core, i);
				tap.nodetapWriteInstruction(core, instructions.get(i).getWord());
				System.out.format("[%02d] %s%n", i, instructions.get(i).asBinary());
				executedStep();
			}
			tap.nodetapWriteIP(core, ip);
			executedStep();
		});
		refreshNodeFully(core);
	}

	/**
	 * Executed when the system has halted.
	 */
	private void onStarted()
	{
		handler.onStarted();
	}

	/**
	 * Executed when the system has started running again.
	 * @throws InterruptedException
	 * @throws TisException
	 */
	private void onStopped() throws TisException, InterruptedException
	{
		handler.onStopped();
	}

	/**
	 * Refreshes a node.
	 *
	 * @param node The node to refresh.
	 *
	 * @throws TisException
	 * @throws InterruptedException
	 */
	public void refreshNode(int node)
	{
		stepsRequired.addAndGet(3);
		stoppedQueue.add(() ->
		{
			NodeHandler handler = nodeHandlers.get(node);
			int ip = tap.nodetapReadIP(node);
			executedStep();
			handler.onIPRead(ip);

			int acc = tap.nodetapReadACC(node);
			executedStep();
			handler.onACCRead(acc);

			NodeState state = tap.nodetapState(node);
			executedStep();
			handler.onState(state);
		});
	}

	/**
	 * Refreshe all nodes.
	 */
	public void refreshNodes()
	{
		for (int node : nodeHandlers.keySet())
		{
			refreshNode(node);
		}
	}

	/**
	 * Refreshes a node fully.
	 *
	 * @param node The node to refresh.
	 */
	public void refreshNodeFully(int node)
	{
		stepsRequired.addAndGet(1+1+16);

		refreshNode(node);
		stoppedQueue.add(() ->
		{
			NodeHandler handler = nodeHandlers.get(node);
			int ip = tap.nodetapReadIP(node);
			executedStep();
			handler.onIPRead(ip);

			List<TisInstruction> instructions = new ArrayList<>(16);
			for (int i = 0; i < 16; i++)
			{
				tap.nodetapWriteIP(node, i);
				int word = tap.nodetapReadInstruction(node);
				TisInstruction instruction = new TisInstruction(word);
				instructions.add(instruction);
				executedStep();
			}
			tap.nodetapWriteIP(node, ip);
			executedStep();

			handler.onRomRead(instructions);
		});
	}

	/**
	 * Refreshes all nodes fully.
	 */
	public void refreshNodesFully()
	{
		for (int node : nodeHandlers.keySet())
		{
			refreshNodeFully(node);
		}
	}

	/**
	 * Executed a step.
	 */
	private void executedStep()
	{
		int executed = stepsExecuted.incrementAndGet();
		this.handler.progressSet(executed, stepsRequired.get());
	}
}
