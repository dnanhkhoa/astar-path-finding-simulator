package algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTextArea;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public abstract class VisualGraph {

	protected Graph graph;
	protected Viewer viewer;
	protected ViewPanel viewPanel;

	private float speed;
	protected boolean isRunning;

	// Controls
	private final JTextArea descTextArea;
	private final JList<String> codeList;

	public VisualGraph(JTextArea descTextArea, JList<String> codeList) {
		this.graph = initializeGraph(getClass().getSimpleName());

		if (this.graph != null) {
			this.graph.addAttribute("ui.quality");
			this.graph.addAttribute("ui.antialias");

			this.viewer = new Viewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			this.setAutoLayout(true);
			this.viewPanel = this.viewer.addDefaultView(false);
		}

		this.descTextArea = descTextArea;
		this.codeList = codeList;

		this.speed = 1.0f;
		this.isRunning = false;
	}

	// Khởi tạo graph, override nếu muốn sử dụng MultiGraph
	protected Graph initializeGraph(String graphName) {
		return new SingleGraph(graphName);
	}

	// In chuỗi ra khung mô tả
	protected void print(String msg) {
		if (descTextArea != null) {
			descTextArea.setText(msg);
		}
	}

	// Highlight các dòng đang được thực thi ở khung codes
	protected void highlight(int lineNumber, int numLines) {
		if (codeList != null) {
			// Xóa highlight nếu vị trí dòng không hợp lệ
			if (lineNumber < 0 || numLines <= 0 || lineNumber + numLines > codeList.getModel().getSize()) {
				codeList.clearSelection();
			} else {

				int[] indices = new int[numLines];

				for (int i = 0; i < numLines; ++i)
					indices[i] = lineNumber + i;

				codeList.setSelectedIndices(indices);
			}
		}
	}

	// Highlight các dòng đang được thực thi ở khung codes
	protected void highlight(int lineNumber) {
		highlight(lineNumber, 1);
	}

	// Dừng tạm thời dùng để điều chỉnh tốc độ chạy thuật toán
	protected void sleep() {
		sleep((long) (speed * 100));
	}

	// Dừng tạm thời dùng để điều chỉnh tốc độ chạy thuật toán
	protected void sleep(long milisecond) {
		try {
			Thread.sleep(milisecond);
		} catch (InterruptedException e) {
		}
	}

	// Load code vào list
	protected void loadCode(String code) {
		if (codeList != null) {
			codeList.setListData(code.split("\n"));
		}
	}

	// Thêm thuộc tính nâng cao
	protected void addAttribute(Element element, boolean append, String key, Object... values) {
		if (append) {
			List<Object> objects = new ArrayList<>();

			Object object = element.getAttribute(key);
			if (object instanceof Object[]) {
				objects.addAll(Arrays.asList((Object[]) object));
			} else if (object != null) {
				objects.add(object);
			}
			objects.addAll(Arrays.asList(values));
			element.addAttribute(key, objects.toArray());
		} else {
			element.addAttribute(key, values);
		}
	}

	// Xóa thuộc tính nâng cao
	protected void removeAttribute(Element element, String key, Object... values) {
		List<Object> objects = new ArrayList<>();

		Object object = element.getAttribute(key);
		if (object instanceof Object[]) {
			objects.addAll(Arrays.asList((Object[]) object));
		} else if (object != null) {
			objects.add(object);
		}

		for (Object value : values) {
			if (objects.contains(value)) {
				objects.remove(value);
			}
		}

		if (objects.isEmpty()) {
			element.removeAttribute(key);
		} else {
			element.addAttribute(key, objects.toArray());
		}
	}

	// Thay đổi tốc độ chạy của thuật toán
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	// Kiểm tra thuật toán có đang chạy
	public boolean isRunning() {
		return isRunning;
	}

	// Dừng thuật toán
	public void stop() {
		isRunning = false;
	}

	// Khôi phục trạng thái mặc định của View
	public void resetView() {
		viewPanel.getCamera().resetView();
	}

	// Góc hiện tại của khung hình
	public double getViewRotation() {
		return viewPanel.getCamera().getViewRotation();
	}

	// Tỉ lệ zoom [0, 1]
	public double getViewPercent() {
		return viewPanel.getCamera().getViewPercent();
	}

	// Vị trí trung tâm của khung hình
	public Point3 getViewCenter() {
		return viewPanel.getCamera().getViewCenter();
	}

	// Góc hiện tại của khung hình
	public void setViewRotation(double theta) {
		viewPanel.getCamera().setViewRotation(theta);
	}

	// Tỉ lệ zoom [0, 1]
	public void setViewPercent(double percent) {
		viewPanel.getCamera().setViewPercent(percent);
	}

	// Đặt vị trí trung tâm của khung hình
	public void setViewCenter(double x, double y, double z) {
		viewPanel.getCamera().setViewCenter(x, y, z);
	}

	public void focusNode(Node node) {
		double[] pos = Toolkit.nodePosition(node);
		setViewCenter(pos[0], pos[1], pos[2]);
	}

	// Cho phép tự động dồn graph để nằm trong khung hình
	public void setAutoFitView(boolean on) {
		viewPanel.getCamera().setAutoFitView(on);
	}

	// Cho phép tự động sắp xếp layout
	public void setAutoLayout(boolean on) {
		if (viewer != null) {
			if (on) {
				viewer.enableAutoLayout();
			} else {
				viewer.disableAutoLayout();
			}
		}
	}

	// Trả về GraphView để tích hợp vào GUI
	public ViewPanel getGraphView() {
		return viewPanel;
	}

	// Cho biết thuật toán dùng tập tin để khởi tạo hay khởi tạo ngẫu nhiên
	public boolean isUsingDataFile() {
		return false;
	}

	// Load dữ liệu từ file lên graph
	public abstract void loadData(File file);

	// Chạy thuật toán
	public abstract void run();
}
