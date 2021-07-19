package be.seeseemelk.tiside.ui;

import java.awt.GridLayout;

import javax.swing.JPanel;

import be.seeseemelk.tiside.tis.TisService;

/**
 * Show the state of the entire system.
 */
public class TisSystemView extends JPanel
{
	private static final long serialVersionUID = 1008076017869251761L;
	private final TisService service;

	public TisSystemView(TisService service)
	{
		this.service = service;

		GridLayout layout = new GridLayout(3, 4);
		layout.setHgap(16);
		layout.setVgap(16);
		setLayout(layout);

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				int id = (x << 2) | (y);
				add(createCoreView(id));
			}
		}
	}

	private TisCoreView createCoreView(int id)
	{
		return new TisCoreView(service, id);
	}

}
