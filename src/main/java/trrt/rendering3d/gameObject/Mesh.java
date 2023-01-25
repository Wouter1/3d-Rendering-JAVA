package trrt.rendering3d.gameObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import trrt.rendering3d.graphics.Lighting;
import trrt.rendering3d.primitives.Matrix3x3;
import trrt.rendering3d.primitives.Quaternion;
import trrt.rendering3d.primitives.Triangle;
import trrt.rendering3d.primitives.Vector2;
import trrt.rendering3d.primitives.Vector3;

//a class for storing groups of triangles in a mesh.
public class Mesh implements Serializable {
	private static final long serialVersionUID = 1;

	// a collection of all the triangles in the mesh.
	private List<Triangle> triangles;
	private List<Vector3> vertices;

	// the color of all the triangles of the mesh.
	private Color baseColor;

	// should the mesh be effected by lighting?
	private boolean shading;

	// a sum of all translations
	private Vector3 totalMovement;

	// the lighting object which was used last to recalculate lighting
	private Lighting lighting;

	// the texture applied to the mesh
	private BufferedImage texture;
	private Raster textureRaster;

	/**
	 * constructor for making a mesh with a texture
	 * 
	 * @param modelFile           3d model file (.obj)
	 * @param textureFile         the texture image file
	 * @param modelOffsetAmount   a 3d offset for the model to be loaded with.
	 *                            Default is {@code Vector3.ZERO}
	 * @param modelOffsetRotation a rotation offset for the model to be loaded
	 *                            with. Default is {@code Quaternion.IDENTITY}
	 * @param scale               the scale for the model to be loaded with.
	 *                            Default is 1
	 * @param shaded              should the object recieve lighting?
	 */
	public Mesh(File modelFile, File textureFile, Vector3 modelOffsetAmount,
			Quaternion modelOffsetRotation, double scale, boolean shaded) {
		long start = System.nanoTime();
		texture = null;
		try {
			if (textureFile != null)
				texture = ImageIO.read(textureFile);
		} catch (IOException e) {
			System.err.println(
					"ERROR at: Mesh/constructor:\n\tError while loading texture: "
							+ textureFile);
		}
		if (texture != null)
			textureRaster = texture.getData();

		vertices = new ArrayList<Vector3>();
		triangles = new ArrayList<Triangle>();
		shading = shaded;
		baseColor = Color.MAGENTA;
		totalMovement = Vector3.ZERO;

		if (modelFile.getName().endsWith(".obj")) {
			createTriangles(modelFile, modelOffsetAmount, modelOffsetRotation,
					scale);
			if (modelOffsetAmount == null) {
				Vector3 com = Mesh.centerOfMass(vertices);
				for (int i = 0; i < vertices.size(); i++) {
					vertices.set(i, vertices.get(i).subtract(com));
				}
			}
		} else {
			System.err.println(
					"ERROR at: Mesh/constructor:\n\tUnsupported 3d model file type. Please use .obj files");
		}
		System.out.println("mesh created: " + modelFile + " in "
				+ (System.nanoTime() - start) / 1000000 + "ms\n\t- "
				+ triangles.size() + " triangles");
	}

	/**
	 * constructor for making a mesh without a texture
	 * 
	 * @param modelFile           the 3d model file (.obj)
	 * @param color               color for the mesh
	 * @param modelOffsetAmount   a 3d offset for the model to be loaded with.
	 *                            Default is {@code Vector3.ZERO}
	 * @param modelOffsetRotation a rotation offset for the model to be loaded
	 *                            with. Default is {@code Quaternion.IDENTITY}
	 * @param scale               the scale for the model to be loaded with.
	 *                            Default is 1
	 * @param shaded              should the object recieve lighting?
	 */
	public Mesh(File modelFile, Color color, Vector3 modelOffsetAmount,
			Quaternion modelOffsetRotation, double scale, boolean shaded) {
		long start = System.nanoTime();
		texture = null;
		textureRaster = null;
		vertices = new ArrayList<Vector3>();
		triangles = new ArrayList<Triangle>();
		shading = shaded;
		baseColor = (color == null) ? Color.MAGENTA : color;
		totalMovement = Vector3.ZERO;

		if (modelFile.getName().endsWith(".obj")) {
			createTriangles(modelFile, modelOffsetAmount, modelOffsetRotation,
					scale);
			if (modelOffsetAmount == null) {
				Vector3 com = Mesh.centerOfMass(vertices);
				System.out.println(com);
				for (int i = 0; i < vertices.size(); i++) {
					vertices.set(i, vertices.get(i).subtract(com));
				}
				triangles = triangles.stream().map(tri -> tri.subtract(com))
						.collect(Collectors.toList());
			}
		} else {
			System.err.println(
					"ERROR at: Mesh/constructor:\n\tUnsupported 3d model file type. Please use .obj files");
		}
		System.out.println("mesh created: " + modelFile + " in "
				+ (System.nanoTime() - start) / 1000000 + "ms\n\t- "
				+ triangles.size() + " triangles");
	}

	protected Mesh(boolean shadedIn) {
		shading = shadedIn;
		triangles = new ArrayList<Triangle>();
	}

	/**
	 * rotates each triangle in the mesh according to a rotation matrix, and
	 * around the center of rotation.
	 * 
	 * @param quaternion       the rotation
	 * @param centerOfRotation center of rotation
	 */
	public void rotate(Quaternion quaternion, Vector3 centerOfRotation) {
		for (int i = 0; i < vertices.size(); i++) {
			vertices.set(i,
					Vector3.add(Vector3.rotate(
							vertices.get(i).subtract(centerOfRotation),
							quaternion), centerOfRotation));
		}
	}

	/**
	 * applies a 3x3 matrix to each vertex in the mesh
	 * 
	 * @param matrix           the matrix applied
	 * @param centerOfRotation the center of rotation for the mesh
	 */
	public void applyMatrix(Matrix3x3 matrix, Vector3 centerOfRotation) {
		for (int i = 0; i < vertices.size(); i++) {
			vertices.set(i,
					Vector3.add(
							Vector3.applyMatrix(matrix,
									vertices.get(i).subtract(centerOfRotation)),
							centerOfRotation));
		}
	}

	/**
	 * translates each triangle in the mesh by {@code amount}
	 * 
	 * @param amount the translation
	 */
	public void translate(Vector3 amount) {
		for (int i = 0; i < vertices.size(); i++) {
			vertices.set(i, Vector3.add(vertices.get(i), amount));
		}
		totalMovement = Vector3.add(totalMovement, amount);
	}

	// #region getter methods
	public boolean isShaded() {
		return shading;
	}

	public Raster getTextureRaster() {
		return textureRaster;
	}

	public List<Triangle> getTriangles() {
		return triangles;
	}

	public List<Vector3> getVertices() {
		return vertices;
	}

	// #endregion

	/**
	 * calculates the lighting of each triangle in the mesh based off the given
	 * lighting object
	 * 
	 * @param lightingIn the lighting object
	 */
	public void calculateLighting(Lighting lightingIn) {
		if (shading) {
			for (int i = 0; i < triangles.size(); i++) {
				triangles.get(i).calculateLightingColor(lightingIn);
			}
		}
		lighting = lightingIn;
	}

	/**
	 * refreshes the lighting based on the last used lighting object.
	 */
	public void refreshLighting() {
		if (shading) {
			for (int i = 0; i < triangles.size(); i++) {
				triangles.get(i).calculateLightingColor(lighting);
			}
		}
	}

	/**
	 * reads a .obj file (a text file) and stores triangles inside the triangle
	 * list.
	 * 
	 * @param file              .obj file name
	 * @param offsetPosition
	 * @param offsetOrientation
	 * @param scale
	 */
	private void createTriangles(File file, Vector3 offsetPosition,
			Quaternion offsetOrientation, double scale) {
		// vertices are temporarily stored before they are conbined into
		// triangles and added into the main
		// triangle list.
		ArrayList<Vector2> textureCoords = new ArrayList<Vector2>();
		Scanner scanner;
		String line = "";

		// innitialize the scanner
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR at: Mesh/readObjFile() method:\n\tfile "
					+ file.getName() + " not found in "
					+ file.getAbsolutePath());
			return;
		}

		// scanner goes through the file
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (!line.equals("")) {
				// v means Vector3 in .obj files
				if (line.startsWith("v ")) {
					StringTokenizer lineTokens = new StringTokenizer(line);
					lineTokens.nextToken();
					// create the Vector3 object
					Vector3 vertexCoordinate = new Vector3(
							Double.parseDouble(lineTokens.nextToken()),
							Double.parseDouble(lineTokens.nextToken()),
							Double.parseDouble(lineTokens.nextToken()));

					// apply transformations to the Vector3 based on offset
					// params
					vertexCoordinate = Vector3.rotate(vertexCoordinate,
							offsetOrientation);
					vertexCoordinate = Vector3.multiply(vertexCoordinate,
							scale);
					if (offsetPosition != null)
						vertexCoordinate = Vector3.add(offsetPosition,
								vertexCoordinate);

					// adds the Vector3 to the array of vertices
					vertices.add(vertexCoordinate);
				}

				// vt means Vector3 texture coordinates.
				if (texture != null && line.startsWith("vt ")) {
					StringTokenizer tokens = new StringTokenizer(line);
					tokens.nextToken();
					textureCoords.add(
							new Vector2(Double.parseDouble(tokens.nextToken()),
									Double.parseDouble(tokens.nextToken())));
				}

				// f means face in .obj files
				if (line.startsWith("f ")) {
					StringTokenizer lineTokens = new StringTokenizer(line);
					lineTokens.nextToken();
					int tokenLength = lineTokens.countTokens();
					int[] coordinateIndexes = new int[tokenLength];
					int[] textureIndexes = new int[tokenLength];
					String[] tempArr;

					Color color = baseColor;
					for (int i = 0; i < tokenLength; i++) {
						tempArr = lineTokens.nextToken().split("/");
						coordinateIndexes[i] = Integer.parseInt(tempArr[0]) - 1;
						if (texture != null)
							textureIndexes[i] = Integer.parseInt(tempArr[1])
									- 1;
					}

					// create triangles based on the indicated verticies.
					// However often verticies are not in sets of 3, so create
					// multiple triangles if necessary.
					for (int i = 0; i < coordinateIndexes.length - 2; i++) {
						if (texture == null)
							triangles.add(new Triangle(this,
									vertices.get(coordinateIndexes[0]),
									vertices.get(coordinateIndexes[i + 1]),
									vertices.get(coordinateIndexes[i + 2]),
									color));
						else
							triangles.add(new Triangle(this,
									vertices.get(coordinateIndexes[0]),
									vertices.get(coordinateIndexes[i + 1]),
									vertices.get(coordinateIndexes[i + 2]),
									textureCoords.get(textureIndexes[0]),
									textureCoords.get(textureIndexes[i + 1]),
									textureCoords.get(textureIndexes[i + 2])));
					}
				}
			}
		}
		scanner.close();
	}

	public static Vector3 centerOfMass(List<Vector3> vertices) {
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;

		for (int i = 0; i < vertices.size(); i++) {
			sumX += vertices.get(i).x;
			sumY += vertices.get(i).y;
			sumZ += vertices.get(i).z;
		}

		sumX /= vertices.size();
		sumY /= vertices.size();
		sumZ /= vertices.size();

		return new Vector3(sumX, sumY, sumZ);
	}
}
