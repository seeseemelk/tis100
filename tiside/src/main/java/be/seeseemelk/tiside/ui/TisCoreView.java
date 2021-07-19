package be.seeseemelk.tiside.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import be.seeseemelk.tiside.tis.NodeHandler;
import be.seeseemelk.tiside.tis.NodeState;
import be.seeseemelk.tiside.tis.TisException;
import be.seeseemelk.tiside.tis.TisInstruction;
import be.seeseemelk.tiside.tis.TisService;

/**
 * Displays the state of a single core
 */
public class TisCoreView extends JPanel implements NodeHandler
{
	private static final long serialVersionUID = 7633949006656287426L;
	private static final String ROM_DEFAULT =
			"00 <OPCODE>\n" +
			"01 <OPCODE>\n" +
			"02 <OPCODE>\n" +
			"03 <OPCODE>\n" +
			"04 <OPCODE>\n" +
			"05 <OPCODE>\n" +
			"06 <OPCODE>\n" +
			"07 <OPCODE>\n" +
			"08 <OPCODE>\n" +
			"09 <OPCODE>\n" +
			"10 <OPCODE>\n" +
			"11 <OPCODE>\n" +
			"12 <OPCODE>\n" +
			"13 <OPCODE>\n" +
			"14 <OPCODE>\n" +
			"15 <OPCODE>\n"
	;
	private static final String INSTRUCTION_DEFAULT =
			"<INSTRUCTION 0>\n" +
			"<INSTRUCTION 1>\n" +
			"<INSTRUCTION 2>\n" +
			"<INSTRUCTION 3>\n" +
			"<INSTRUCTION 4>\n" +
			"<INSTRUCTION 5>\n" +
			"<INSTRUCTION 6>\n" +
			"<INSTRUCTION 7>\n" +
			"<INSTRUCTION 8>\n" +
			"<INSTRUCTION 9>\n" +
			"<INSTRUCTION 10>\n" +
			"<INSTRUCTION 11>\n" +
			"<INSTRUCTION 12>\n" +
			"<INSTRUCTION 13>\n" +
			"<INSTRUCTION 14>\n" +
			"<INSTRUCTION 15>\n"
		;

	private final TisService service;
	private final int coreId;
	private final JLabel idLabel;
	private final JLabel ipLabel;
	private final JLabel accLabel;
	private final JLabel statusLabel;
	private final JTextArea romArea;
	private final JTextArea instructionArea;

	public TisCoreView(TisService service, int id)
	{
		this.service = service;
		this.coreId = id;

		Border bevel = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Border empty = BorderFactory.createEmptyBorder(8, 8, 8, 8);
		setBorder(BorderFactory.createCompoundBorder(bevel, empty));

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		idLabel = new JLabel("Node " + id);
		ipLabel = new JLabel();
		accLabel = new JLabel();
		statusLabel = new JLabel();
		romArea = new JTextArea(ROM_DEFAULT);
		romArea.setDisabledTextColor(Color.BLACK);
		romArea.setBackground(Color.LIGHT_GRAY);
		romArea.setEnabled(false);
		instructionArea = new JTextArea(INSTRUCTION_DEFAULT);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
		panel.setLayout(new GridBagLayout());
		panel.add(romArea, grid(0, 0));
		panel.add(instructionArea, fill(1, 0));

		JScrollPane rom = new JScrollPane(panel);
		rom.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		c.gridwidth = 2;
		add(idLabel, c);

		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		add(rom, c);

		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;

		c.gridy++;
		add(statusLabel, c);

		c.gridy++;
		add(ipLabel, c);

		c.gridy++;
		add(accLabel, c);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> service.refreshNode(id));
		c.gridy++;
		add(refreshButton, c);

		JButton compileButton = new JButton("Compile");
		compileButton.addActionListener(e -> onCompile());
		c.gridy++;
		add(compileButton, c);

		setIp(0);
		setId(coreId);

		service.registerNodeHandler(id, this);
	}

	private void setId(int id)
	{
		int high = coreId >> 2;
		int low = coreId & 0x3;
		idLabel.setText(String.format("Node %d%d", high, low));
	}

	private void setState(NodeState state)
	{
		statusLabel.setText(String.format("%s", state.name()));
	}

	private void setIp(int ip)
	{
		ipLabel.setText(String.format("IP: %02d", ip % 16));
	}

	private void setAcc(int acc)
	{
		accLabel.setText(String.format("ACC: %03d", acc));
	}

	@Override
	public void onState(NodeState state)
	{
		SwingUtilities.invokeLater(() ->
		{
			setState(state);
		});
	}

	@Override
	public void onIPRead(int ip)
	{
		SwingUtilities.invokeLater(() ->
		{
			setIp(ip);
		});
	}

	@Override
	public void onACCRead(int acc)
	{
		SwingUtilities.invokeLater(() ->
		{
			setAcc(acc);
		});
	}

	@Override
	public void onRomRead(List<TisInstruction> instructions)
	{
		StringBuilder rom = new StringBuilder();
		StringBuilder assembly = new StringBuilder();
		for (int i = 0; i < instructions.size(); i++)
		{
			TisInstruction instr = instructions.get(i);
			rom.append(String.format("[%02d] %s %n", i, instr.asBinary()));

			assembly.append(instr.asAssembly()).append(System.lineSeparator());
		}
		romArea.setText(rom.toString());
		instructionArea.setText(assembly.toString());
	}

	private GridBagConstraints grid(int x, int y)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		return c;
	}

	private GridBagConstraints fill(int x, int y)
	{
		GridBagConstraints c = grid(x, y);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		return c;
	}

	private void onCompile()
	{
		try
		{
			String[] lines = instructionArea.getText().split("\n");
			List<TisInstruction> instructions = new ArrayList<>();
			for (String line : lines)
			{
				TisInstruction instruction = TisInstruction.compile(line);
				instructions.add(instruction);
			}
			service.writeInstructions(coreId, instructions);
		}
		catch (TisException e)
		{
			service.getHandler().onException(e);
		}
	}
}
