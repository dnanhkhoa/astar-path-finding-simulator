package algorithms;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import algorithms.generator.IncompleteGridGenerator;

public final class ShortestPathFinding extends VisualGraph {

	private final int BOARD_SIZE = 6;

	private String source = null;
	private String destination = null;

	SpriteManager spriteManager = null;

	private Comparator<Node> comparator = new Comparator<Node>() {

		@Override
		public int compare(Node a, Node b) {
			if ((double) a.getAttribute("f") > (double) b.getAttribute("f"))
				return 1;
			if ((double) a.getAttribute("f") < (double) b.getAttribute("f"))
				return -1;
			return 0;
		}

	};

	public ShortestPathFinding(JTextArea descTextArea, JList<String> codeList) {
		super(descTextArea, codeList);

		// Cho phép thêm edge sẽ tự thêm node
		graph.setStrict(false);
		graph.setAutoCreate(true);

		setAutoLayout(false);

		// CSS
		graph.addAttribute("ui.stylesheet", "url('file:///../css/shortestpathfinding.css')");

		// Code
		loadCode("01. Compute f(S) = g(S) + h(S)\n"
				+ "02. Visited = {} and PQueue = {S}\n"
				+ "03. While size(PQueue) > 0:\n"
				+ "04.     N = pop(PQueue)\n"
				+ "05.     If isGoal(N):\n"
				+ "06.         Break\n"
				+ "07.     For all N' in children(N):\n"
				+ "08.         Compute f'(N') = g(N) + w(N, N') + h(N')\n"
				+ "09.         If N' in Visited and f'(N') >= f(N'):\n"
				+ "10.             Go to line 3\n"
				+ "11.         If f'(N') < f(N'):\n"
				+ "12.             f(N') = f'(N')\n"
				+ "13.             If N' not in PQueue:\n"
				+ "14.                 insert(PQ, N')\n"
				+ "15.     markVisited(N)\n"
				+ "16. If isGoal(N):\n"
				+ "17.     backTracking(N)\n");
	}

	private String parseNodeId(String nodeId) {
		String[] parts = nodeId.split("_");
		Integer x = Integer.parseInt(parts[0]);
		Integer y = Integer.parseInt(parts[1]);
		return String.format("%d", x * BOARD_SIZE + y);
	}

	private String getNodeId(String id) {
		Integer nodeId = Integer.parseInt(id);
		return String.format("%d_%d", nodeId / BOARD_SIZE, nodeId % BOARD_SIZE);
	}

	private double getEuclideanDistance(Node a, Node b) {
		Point3 posA = Toolkit.nodePointPosition(a);
		Point3 posB = Toolkit.nodePointPosition(b);
		return Math.sqrt((posA.x - posB.x) * (posA.x - posB.x) + (posA.y - posB.y) * (posA.y - posB.y));
	}

	private void markVisited(Node node, boolean visited) {
		node.addAttribute("visited", visited);
	}

	private boolean isVisited(Node node) {
		return node.getAttribute("visited", Boolean.class);
	}

	@Override
	public void loadData(File file) {
		// Tạo lưới với xác suất bỏ ô là 0.5
		Generator gen = new IncompleteGridGenerator(true, 0.5f, 5, 3);

		gen.addSink(graph);
		gen.begin();

		for (int i = 0; i < BOARD_SIZE; i++) {
			gen.nextEvents();
		}

		gen.end();

		for (Edge edge : graph.getEachEdge()) {
			// Hiển thị nhãn và tính khoảng cách cho edge
			double weight = Toolkit.edgeLength(edge);
			edge.addAttribute("w", weight);
			edge.addAttribute("ui.label", String.format("%.02f", weight));
		}

		for (Node node : graph) {
			// Gán nhãn cho node
			node.addAttribute("ui.label", parseNodeId(node.getId()));
		}

		spriteManager = new SpriteManager(graph);

		// Nhập node nguồn
		while (source == null) {
			try {
				String result = JOptionPane.showInputDialog("Please input the index of start node!");
				String nodeId = getNodeId(result);
				Node sourceNode = graph.getNode(nodeId);
				if (result != null && !result.isEmpty() && sourceNode != null) {
					source = nodeId;
					sourceNode.addAttribute("ui.class", "focus");
					break;
				}
			} catch (Exception e) {
				source = null;
			}
		}

		// Nhập node đích
		while (destination == null) {
			try {
				String result = JOptionPane.showInputDialog("Please input the index of goal node!");
				String nodeId = getNodeId(result);
				Node goalNode = graph.getNode(nodeId);
				if (result != null && !result.isEmpty() && goalNode != null) {
					destination = nodeId;
					goalNode.addAttribute("ui.class", "goal");

					// Tính điểm f, g và h
					for (Node node : graph) {
						node.addAttribute("g", 0.);
						node.addAttribute("h", getEuclideanDistance(node, goalNode));
						node.addAttribute("f", Double.MAX_VALUE);

						markVisited(node, false);
						node.addAttribute("back", "-1");

						// Thêm nhãn f cho node
						Sprite sprite = spriteManager.addSprite(node.getId());
						sprite.addAttribute("ui.label", "∞");
						sprite.attachToNode(node.getId());
					}

					break;
				}
			} catch (Exception e) {
				destination = null;
			}
		}

		setAutoFitView(false);
	}

	@Override
	public void run() {
		if (isRunning()) {
			print("Application is running!");
			return;
		}

		if (graph.getNodeCount() <= 0) {
			print("Graph is invalid!");
		}

		isRunning = true;

		PriorityQueue<Node> queue = new PriorityQueue<>(comparator);

		Node currentNode = graph.getNode(source);
		Node goalNode = graph.getNode(destination);

		currentNode.changeAttribute("f", currentNode.getAttribute("h", Double.class));

		// In thông báo dòng 1 | OK
		print(String.format("Compute f(S) = g(S) + h(S) = %.02f", currentNode.getAttribute("h", Double.class)));
		highlight(0, 1);
		sleep();

		queue.add(currentNode);

		// In thông báo dòng 2 | OK
		print(String.format("Visited = {} and PQueue = {%s}", parseNodeId(currentNode.getId())));
		highlight(1, 1);
		sleep();

		while (isRunning() && !queue.isEmpty()) {

			// In thông báo dòng 3 | OK
			print(String.format("While size(PQueue) = %d > 0", queue.size()));
			highlight(2, 1);
			sleep();

			currentNode = queue.poll();

			currentNode.addAttribute("ui.class", "focus");
			focusNode(currentNode);

			// Cập nhật f cho node hiện tại
			spriteManager.getSprite(currentNode.getId()).addAttribute("ui.label",
					String.format("%.2f", currentNode.getAttribute("f", Double.class)));

			// In thông báo dòng 4 | OK
			print(String.format("N = pop(PQueue) = %s", parseNodeId(currentNode.getId())));
			highlight(3, 1);
			sleep();

			boolean flag = currentNode.equals(goalNode);

			// In thông báo dòng 5 | OK
			print(String.format("isGoal(%s) is %s", parseNodeId(currentNode.getId()), flag));
			highlight(4, 1);
			sleep();

			if (flag) {
				// In thông báo dòng 6 | OK
				print("Break");
				highlight(5, 1);
				sleep();
				break;
			}

			Iterator<Node> nodeIterator = currentNode.getNeighborNodeIterator();
			// In thông báo dòng 7 | OK
			print(String.format("For all N' in children(%s)", parseNodeId(currentNode.getId())));
			highlight(6, 1);
			sleep();

			while (isRunning() && nodeIterator.hasNext()) {
				Node childNode = nodeIterator.next();

				double g = currentNode.getAttribute("g", Double.class);
				double w = childNode.getEdgeBetween(currentNode).getAttribute("w", Double.class);
				double h = childNode.getAttribute("h", Double.class);
				double fOld = childNode.getAttribute("f", Double.class);
				double fNew = g + w + h;

				// In thông báo dòng 8 | OK
				print(String.format("Compute f'(%s) = g(%s) + w(%s, %s) + h(%s) = %.02f + %.02f + %.02f = %.02f",
						parseNodeId(childNode.getId()), parseNodeId(currentNode.getId()),
						parseNodeId(currentNode.getId()), parseNodeId(childNode.getId()),
						parseNodeId(childNode.getId()), g, w, h, fNew));
				highlight(7, 1);
				sleep();

				// In thông báo dòng 9 | OK
				if (fOld == Double.MAX_VALUE) {
					print(String.format("(%s in Visited) is %s and (f'(%s) = %.02f >= f(%s) = oo) is %s",
							parseNodeId(childNode.getId()), isVisited(childNode), parseNodeId(childNode.getId()), fNew,
							parseNodeId(childNode.getId()), fNew >= fOld));
				} else {
					print(String.format("(%s in Visited) is %s and (f'(%s) = %.02f >= f(%s) = %.02f) is %s",
							parseNodeId(childNode.getId()), isVisited(childNode), parseNodeId(childNode.getId()), fNew,
							parseNodeId(childNode.getId()), fOld, fNew >= fOld));
				}
				highlight(8, 1);
				sleep();

				if (isVisited(childNode) && fNew >= fOld) {
					// In thông báo dòng 10
					print("Go to line 3");
					highlight(9, 1);
					sleep();

					continue;
				}

				// In thông báo dòng 11
				if (fOld == Double.MAX_VALUE) {
					print(String.format("(f'(%s) = %.02f < f(%s) = oo) is %s", parseNodeId(childNode.getId()), fNew,
							parseNodeId(childNode.getId()), fNew < fOld));
				} else {
					print(String.format("(f'(%s) = %.02f < f(%s) = %.02f) is %s", parseNodeId(childNode.getId()), fNew,
							parseNodeId(childNode.getId()), fOld, fNew < fOld));
				}
				highlight(10, 1);
				sleep();

				if (fNew < fOld) {
					childNode.addAttribute("ui.class", "highlight");
					childNode.getEdgeBetween(currentNode).addAttribute("ui.class", "highlight");

					spriteManager.getSprite(childNode.getId()).addAttribute("ui.label", String.format("%.2f", fNew));

					childNode.changeAttribute("g", g + w);
					childNode.changeAttribute("f", fNew);
					childNode.changeAttribute("back", currentNode.getId());

					// In thông báo dòng 12
					print(String.format("f(%s) = f'(%s) = %.02f", parseNodeId(childNode.getId()),
							parseNodeId(childNode.getId()), fNew));
					highlight(11, 1);
					sleep();

					// In thông báo dòng 13
					print(String.format("(%s in PQueue) is %s", parseNodeId(childNode.getId()),
							queue.contains(childNode)));
					highlight(12, 1);
					sleep();

					if (!queue.contains(childNode)) {
						// In thông báo dòng 14
						print(String.format("insert(PQ, %s)", parseNodeId(childNode.getId())));
						highlight(13, 1);
						sleep();

						queue.add(childNode);
					}

					childNode.removeAttribute("ui.class");
					childNode.getEdgeBetween(currentNode).removeAttribute("ui.class");
				}
			}

			// In thông báo dòng 15 | OK
			print(String.format("markVisited(%s)", parseNodeId(currentNode.getId())));
			highlight(14, 1);
			sleep();

			markVisited(currentNode, true);
			currentNode.addAttribute("ui.class", "visited");
		}

		// In thông báo dòng 16 | OK
		print(String.format("isGoal(%s) = %s", parseNodeId(currentNode.getId()), currentNode.equals(goalNode)));
		highlight(15, 1);
		sleep();

		if (currentNode.equals(goalNode)) {
			// In thông báo dòng 17 | OK
			print(String.format("Make path from %s to %s", parseNodeId(destination), parseNodeId(source)));
			highlight(16, 1);
			sleep();

			currentNode.addAttribute("ui.class", "back");
			String nodeId = currentNode.getAttribute("back", String.class);

			while (!nodeId.equals("-1")) {
				Node prevNode = graph.getNode(nodeId);
				prevNode.getEdgeBetween(currentNode).addAttribute("ui.class", "back");

				currentNode = prevNode;
				currentNode.addAttribute("ui.class", "back");

				nodeId = currentNode.getAttribute("back", String.class);

				sleep();
			}
		} else {
			// Không có lời giải
			print("No solution");
			highlight(-1);
		}

		resetView();

		isRunning = false;
	}
}
