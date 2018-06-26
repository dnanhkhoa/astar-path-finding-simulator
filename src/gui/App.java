package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import algorithms.ShortestPathFinding;
import algorithms.VisualGraph;

public class App extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel mainPane;
	private JPanel controlPanel;
	private JPanel viewPanel;
	private JScrollPane descScrollPanel;
	private JScrollPane codeScrollPanel;
	private JTextArea descTextArea;
	private JList<String> codeList;
	private JButton btnLoad;
	private JButton btnStop;
	private JButton btnRun;
	private JLabel lblZoom;
	private JSlider zoomSlider;
	private JLabel lblSpeed;
	private JSlider speedSlider;

	private File dataFile = null;
	private VisualGraph visualGraph = null;
	private boolean isDone = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App frame = new App();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public App() {
		// Cho phép Graph hỗ trợ đầy đủ các thuộc tính CSS
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		initialize();
	}

	private void initialize() {
		setTitle("Visual Graph");
		setFont(new Font("Dialog", Font.PLAIN, 14));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1600, 850);
		setMinimumSize(new Dimension(1600, 850));
		setLocationRelativeTo(null);

		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
						FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("max(200dlu;min)"),
						FormSpecs.RELATED_GAP_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, }));
		mainPane.add(getViewPanel(), "2, 2, fill, fill");
		mainPane.add(getControlPanel(), "4, 2, fill, fill");

		setContentPane(mainPane);
	}

	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel();
			controlPanel.setLayout(new FormLayout(
					new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
							ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
							ColumnSpec.decode("default:grow"), },
					new RowSpec[] { RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC,
							RowSpec.decode("default:grow(4)"), FormSpecs.UNRELATED_GAP_ROWSPEC,
							FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
							FormSpecs.UNRELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(25dlu;default)"), }));
			controlPanel.add(getDescScrollPanel(), "1, 1, 5, 1, fill, fill");
			controlPanel.add(getCodeScrollPanel(), "1, 3, 5, 1, fill, fill");
			controlPanel.add(getLblZoom(), "1, 5, right, default");
			controlPanel.add(getZoomSlider(), "3, 5, 3, 1");
			controlPanel.add(getLblSpeed(), "1, 7, right, default");
			controlPanel.add(getSpeedSlider(), "3, 7, 3, 1");
			controlPanel.add(getBtnRun(), "1, 9");
			controlPanel.add(getBtnStop(), "3, 9");
			controlPanel.add(getBtnLoad(), "5, 9");
		}
		return controlPanel;
	}

	private JPanel getViewPanel() {
		if (viewPanel == null) {
			viewPanel = new JPanel();
			viewPanel.setBackground(Color.WHITE);
			viewPanel.setBorder(new LineBorder(new Color(30, 144, 255)));
			viewPanel.setLayout(new BorderLayout(0, 0));
		}
		return viewPanel;
	}

	private JScrollPane getDescScrollPanel() {
		if (descScrollPanel == null) {
			descScrollPanel = new JScrollPane();
			descScrollPanel.setBorder(new LineBorder(new Color(30, 144, 255)));
			descScrollPanel.setViewportView(getDescTextArea());
		}
		return descScrollPanel;
	}

	private JScrollPane getCodeScrollPanel() {
		if (codeScrollPanel == null) {
			codeScrollPanel = new JScrollPane();
			codeScrollPanel.setBorder(new LineBorder(new Color(30, 144, 255)));
			codeScrollPanel.setViewportView(getCodeList());
		}
		return codeScrollPanel;
	}

	private JTextArea getDescTextArea() {
		if (descTextArea == null) {
			descTextArea = new JTextArea();
			descTextArea.setForeground(Color.BLUE);
			descTextArea.setFocusable(false);
			descTextArea.setEditable(false);
			descTextArea.setLineWrap(true);
			descTextArea.setFont(new Font("Tahoma", Font.PLAIN, 18));
		}
		return descTextArea;
	}

	private JList<String> getCodeList() {
		if (codeList == null) {
			codeList = new JList<>();
			codeList.setFocusable(false);
			codeList.setFont(new Font("Tahoma", Font.PLAIN, 18));
		}
		return codeList;
	}

	private JButton getBtnLoad() {
		if (btnLoad == null) {
			btnLoad = new JButton("Load");
			btnLoad.setFocusable(false);
			btnLoad.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					do_btnLoad_actionPerformed(arg0);
				}
			});
		}
		return btnLoad;
	}

	private JButton getBtnStop() {
		if (btnStop == null) {
			btnStop = new JButton("Stop");
			btnStop.setEnabled(false);
			btnStop.setFocusable(false);
			btnStop.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					do_btnStop_actionPerformed(e);
				}
			});
		}
		return btnStop;
	}

	private JButton getBtnRun() {
		if (btnRun == null) {
			btnRun = new JButton("Run");
			btnRun.setFocusable(false);
			btnRun.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					do_btnRun_actionPerformed(e);
				}
			});
		}
		return btnRun;
	}

	private JLabel getLblZoom() {
		if (lblZoom == null) {
			lblZoom = new JLabel("Zoom");
			lblZoom.setFocusable(false);
		}
		return lblZoom;
	}

	private JSlider getZoomSlider() {
		if (zoomSlider == null) {
			zoomSlider = new JSlider();
			zoomSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					do_zoomSlider_stateChanged(arg0);
				}
			});
			zoomSlider.setMinimum(20);
			zoomSlider.setMaximum(100);
			zoomSlider.setValue(100);
			zoomSlider.setFocusable(false);
			zoomSlider.setInverted(true);
		}
		return zoomSlider;
	}

	private JLabel getLblSpeed() {
		if (lblSpeed == null) {
			lblSpeed = new JLabel("Speed");
			lblSpeed.setFocusable(false);
			lblSpeed.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		return lblSpeed;
	}

	private JSlider getSpeedSlider() {
		if (speedSlider == null) {
			speedSlider = new JSlider();
			speedSlider.setFocusable(false);
			speedSlider.setMinimum(1);
			speedSlider.setValue(10);
			speedSlider.setMaximum(10);
		}
		return speedSlider;
	}

	protected void do_zoomSlider_stateChanged(ChangeEvent e) {
		if (visualGraph != null) {
			if (getZoomSlider().getValue() == getZoomSlider().getMaximum()) {
				visualGraph.resetView();
			} else {
				visualGraph.setViewPercent(1.0 * getZoomSlider().getValue() / getZoomSlider().getMaximum());
			}
		}
	}

	protected void do_btnLoad_actionPerformed(ActionEvent e) {
		if (visualGraph != null && visualGraph.isRunning()) {
			JOptionPane.showMessageDialog(this, "Application is running!");
			return;
		}

		// Xóa thông tin dữ liệu cũ
		isDone = false;
		dataFile = null;

		// Xóa bỏ các đối tượng cũ
		clear();

		// Khởi tạo thuật toán mới
		initAlgorithm();

		// Load graph lên view
		loadGraphView();

		if (visualGraph.isUsingDataFile()) {
			// Chọn file data của thuật toán nếu có
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Load data");
			fileChooser.setCurrentDirectory(new java.io.File("."));
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File inFile = fileChooser.getSelectedFile();
				if (!inFile.isFile()) {
					JOptionPane.showMessageDialog(this, "Data file does not exist!");
					return;
				}

				// Khởi tạo đối tượng Visual Graph
				dataFile = inFile;
			}
		}

		// Load dữ liệu
		visualGraph.loadData(dataFile);
	}

	protected void do_btnStop_actionPerformed(ActionEvent e) {
		if (visualGraph != null && visualGraph.isRunning()) {
			visualGraph.stop();
			isDone = true;
		}
		getBtnRun().setEnabled(true);
		getBtnStop().setEnabled(false);
	}

	protected void do_btnRun_actionPerformed(ActionEvent e) {
		if (visualGraph == null) {
			JOptionPane.showMessageDialog(this, "Please load data first!");
			return;
		}

		if (isDone) {
			// Xóa bỏ các đối tượng cũ
			clear();

			// Khởi tạo thuật toán mới
			initAlgorithm();

			// Load graph lên view
			loadGraphView();

			// Load dữ liệu
			visualGraph.loadData(dataFile);

			isDone = false;
		}

		if (visualGraph.isRunning()) {
			JOptionPane.showMessageDialog(this, "Application is running!");
			return;
		}

		visualGraph.setSpeed(getSpeedSlider().getMaximum() - getSpeedSlider().getValue() + 1);

		Thread algoThread = new Thread(new Runnable() {

			@Override
			public void run() {
				getBtnRun().setEnabled(false);
				getBtnStop().setEnabled(true);

				visualGraph.run();

				getBtnRun().setEnabled(true);
				getBtnStop().setEnabled(false);

				isDone = true;
			}
		});

		algoThread.start();
	}

	private void initAlgorithm() {
		visualGraph = new ShortestPathFinding(descTextArea, codeList);
	}

	private void clear() {
		getDescTextArea().setText(null);
		getCodeList().setListData(new String[0]);
		getZoomSlider().setValue(getZoomSlider().getMaximum());
		getViewPanel().removeAll();
		getViewPanel().updateUI();
	}

	private void loadGraphView() {
		if (visualGraph != null) {
			getViewPanel().add(visualGraph.getGraphView());
			getViewPanel().updateUI();
		}
	}
}
