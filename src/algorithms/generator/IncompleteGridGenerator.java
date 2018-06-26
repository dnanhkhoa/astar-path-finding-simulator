package algorithms.generator;

import java.util.HashSet;

import org.graphstream.algorithm.generator.BaseGenerator;

// Lớp Generator cho phép tạo đồ thị không đầy đủ
public class IncompleteGridGenerator extends BaseGenerator {

	protected int currentWidth = 0;
	protected int currentHeight = 0;

	protected float holeProbability = 0.5f;

	protected int holeMaxSize = 5;
	protected int holesPerStep = 3;

	protected boolean cross = true;

	// Ids các nodes kề với node bị xóa -> Đảm bảo đồ thị liên thông
	protected HashSet<String> unbreakable = new HashSet<String>();

	public IncompleteGridGenerator() {
		this(true, 0.5f, 5, 3);
	}

	public IncompleteGridGenerator(boolean cross, float holeProbability, int holeMaxSize, int holesPerStep) {
		setUseInternalGraph(true);

		this.cross = cross;
		this.holeProbability = holeProbability;
		this.holeMaxSize = holeMaxSize;
		this.holesPerStep = holesPerStep;
	}

	protected String getNodeId(int x, int y) {
		return String.format("%d_%d", x, y);
	}

	protected String getEdgeId(String n1, String n2) {
		return n1.compareTo(n2) < 0 ? String.format("%s-%s", n2, n1) : String.format("%s-%s", n1, n2);
	}

	// Tạo liên kết giữa các nodes
	protected void connectNode(int x, int y) {
		String nodeId = getNodeId(x, y);
		String neigh;

		// Duyệt theo 4 hướng từ node hiện tại
		if (x > 0) {
			neigh = getNodeId(x - 1, y);

			// Tạo liên kết với node lân cân nếu tồn tại
			// Ngược lại thì node hiện tại không được xóa để đảm bảo liên thông
			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}
		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}
		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}
		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}

		// Tạo các đường chéo
		if (x > 0) {
			if (y > 0) {
				neigh = getNodeId(x - 1, y - 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross && internalGraph.getEdge(getEdgeId(getNodeId(x - 1, y), getNodeId(x, y - 1))) == null
							&& random.nextFloat() < holeProbability) {
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
					}
				} else
					unbreakable.add(nodeId);
			}

			if (y < currentHeight - 1) {
				neigh = getNodeId(x - 1, y + 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross && internalGraph.getEdge(getEdgeId(getNodeId(x - 1, y), getNodeId(x, y + 1))) == null
							&& random.nextFloat() < holeProbability)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}
		}

		if (x < currentWidth - 1) {
			if (y > 0) {
				neigh = getNodeId(x + 1, y - 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross && internalGraph.getEdge(getEdgeId(getNodeId(x + 1, y), getNodeId(x, y - 1))) == null
							&& random.nextFloat() < holeProbability)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}

			if (y < currentHeight - 1) {
				neigh = getNodeId(x + 1, y + 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross && internalGraph.getEdge(getEdgeId(getNodeId(x + 1, y), getNodeId(x, y + 1))) == null
							&& random.nextFloat() < holeProbability)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}
		}
	}

	// Xóa liên kết giữa 2 nodes
	protected void disconnectNode(int x, int y) {
		String nodeId = getNodeId(x, y);
		String neigh;

		if (x > 0) {
			neigh = getNodeId(x - 1, y);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (cross) {
			if (x > 0) {
				if (y > 0) {
					neigh = getNodeId(x - 1, y - 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x - 1, y + 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}
			}

			if (x < currentWidth - 1) {
				if (y > 0) {
					neigh = getNodeId(x + 1, y - 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x + 1, y + 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}
			}
		}
	}

	protected double random(double min, double max) {
		return min + random.nextDouble() * (max - min);
	}

	protected double randPosition(double x) {
		return x * random(9.5, 10.5) + random(-3.0, 3.0);
	}

	public void begin() {
	}

	public boolean nextEvents() {
		for (int i = 0; i < currentWidth; i++) {
			addNode(getNodeId(i, currentHeight), randPosition(i), randPosition(currentHeight));
			connectNode(i, currentHeight);
		}

		for (int i = 0; i < currentHeight; i++) {
			addNode(getNodeId(currentWidth, i), randPosition(currentWidth), randPosition(i));
			connectNode(currentWidth, i);
		}

		addNode(getNodeId(currentWidth, currentHeight), randPosition(currentWidth), randPosition(currentHeight));
		connectNode(currentWidth, currentHeight);

		currentWidth++;
		currentHeight++;

		for (int k = 0; k < holesPerStep; k++) {
			if (random.nextFloat() < holeProbability) {
				int x1, y1, t;
				int sizeX, sizeY;

				t = 0;

				do {
					x1 = random.nextInt(currentWidth);
					y1 = random.nextInt(currentHeight);
					t++;
				} while ((internalGraph.getNode(getNodeId(x1, y1)) == null || unbreakable.contains(getNodeId(x1, y1)))
						&& t < internalGraph.getNodeCount());

				if (t >= internalGraph.getNodeCount())
					continue;

				sizeX = random.nextInt(holeMaxSize);
				sizeY = random.nextInt(holeMaxSize - sizeX);

				for (int i = 0; i < sizeX; i++)
					for (int j = 0; j < sizeY; j++) {
						String id = getNodeId(x1 + i, y1 + j);
						if (internalGraph.getNode(id) != null && !unbreakable.contains(id)) {
							disconnectNode(x1 + i, y1 + j);
							delNode(getNodeId(x1 + i, y1 + j));

							if (j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 + i, y1 - 1));
							if (j == sizeY - 1 && y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 + i, y1 + sizeY));

							if (i == 0 && x1 > 0)
								unbreakable.add(getNodeId(x1 - 1, y1 + j));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth)
								unbreakable.add(getNodeId(x1 + sizeX, y1 + j));

							if (i == 0 && x1 > 0 && j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 - 1, y1 - 1));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth && j == sizeY - 1
									&& y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 + sizeX, y1 + sizeY));
							if (i == 0 && x1 > 0 && j == sizeY - 1 && y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 - 1, y1 + sizeY));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth && j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 + sizeX, y1 - 1));
						}
					}
			}
		}

		return true;
	}

	@Override
	public void end() {
		super.end();
	}
}