package be.seeseemelk.tiside.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import be.seeseemelk.tiside.tis.TisHandler;
import be.seeseemelk.tiside.tis.TisService;
import be.seeseemelk.tiside.tis.TisTap;

public class TisIDE extends JFrame implements TisHandler
{
	private static final long serialVersionUID = 7177739623731358997L;
	private final TisTap tap;
	private final TisService service;
	private final TisSystemView system;
	private JButton buttonContinue;
	private JButton buttonPause;
	private JTextArea outputText;
	private JProgressBar progressBar;

	public TisIDE(TisTap tap)
	{
		super("TisIDE");
		this.tap = tap;
		this.service = new TisService(tap, this);
		this.system = new TisSystemView(service);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setJMenuBar(createJMenuBar());
		setLayout(new BorderLayout());
		add(createToolBar(), BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		panel.add(this.system);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		panel.add(createConsole(), c);

		add(panel, BorderLayout.CENTER);
		service.start();
	}

	@Override
	public void dispose()
	{
		try
		{
			service.stop();
			tap.close();
			super.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}

	private JMenuBar createJMenuBar()
	{
		JMenuBar bar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.add("Exit").addActionListener(e -> this.dispose());
		bar.add(file);

		return bar;
	}

	private JToolBar createToolBar()
	{
		JToolBar bar = new JToolBar();

		buttonContinue = new JButton("Continue");
		buttonContinue.setEnabled(false);
		buttonContinue.addActionListener(e -> service.run());
		bar.add(buttonContinue);

		buttonPause = new JButton("Pause");
		buttonPause.setEnabled(false);
		buttonPause.addActionListener(e -> service.pause());
		bar.add(buttonPause);

		JButton buttonStep = new JButton("Step");
		buttonStep.setEnabled(true);
		buttonStep.addActionListener(e -> service.step());
		bar.add(buttonStep);

		progressBar = new JProgressBar(0, 1);
		progressBar.setEnabled(false);
		progressBar.setValue(0);
		bar.add(progressBar);

		return bar;
	}

	private JComponent createConsole()
	{
		outputText = new JTextArea();
		outputText.setEditable(false);
		JScrollPane pane = new JScrollPane(outputText);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return pane;
	}

	@Override
	public void onStarted()
	{
		SwingUtilities.invokeLater(() ->
		{
			buttonContinue.setEnabled(false);
			buttonPause.setEnabled(true);
		});
	}

	@Override
	public void onStopped()
	{
		SwingUtilities.invokeLater(() ->
		{
			buttonContinue.setEnabled(true);
			buttonPause.setEnabled(false);
		});
	}

	@Override
	public void onException(Exception e)
	{
		try
		{
			SwingUtilities.invokeAndWait(() ->
			{
				JOptionPane.showMessageDialog(this, "An exception occured: " + e.getMessage());
			});
		}
		catch (InvocationTargetException | InterruptedException e2)
		{
			e2.printStackTrace();
		}
	}

	@Override
	public void onOutput(char output)
	{
		SwingUtilities.invokeLater(() ->
		{
			char[] arr = new char[1];
			arr[0] = output;
			outputText.append(new String(arr));
		});
	}

	@Override
	public void progressStart()
	{
		SwingUtilities.invokeLater(() ->
		{
			progressBar.setEnabled(true);
			progressBar.setValue(0);
		});
	}

	@Override
	public void progressStop()
	{
		SwingUtilities.invokeLater(() ->
		{
			progressBar.setEnabled(false);
			progressBar.setValue(0);
		});
	}

	@Override
	public void progressSet(int step, int max)
	{
		SwingUtilities.invokeLater(() ->
		{
			progressBar.setMaximum(max);
			progressBar.setValue(step);
		});
	}
}
