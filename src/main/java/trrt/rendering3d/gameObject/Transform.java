package trrt.rendering3d.gameObject;

import java.io.Serializable;

import trrt.rendering3d.primitives.Matrix3x3;
import trrt.rendering3d.primitives.Quaternion;
import trrt.rendering3d.primitives.Vector3;

public class Transform implements Serializable {
	private static final long serialVersionUID = 1;

	/** the game object parent that the transform is attached to */
	private GameObject gameObject;

	/** position of the Transform in worldspace */
	private Vector3 position;

	/**
	 * the forward vector of the transform in world space. This is always the
	 * z-axis in local transform space
	 */
	private Vector3 forward;

	/**
	 * the rightwards vector of the transform in world space. This is always the
	 * x-axis in local transform space.
	 */
	private Vector3 right;

	/**
	 * the upwards vector of the transform in world space. This is always the
	 * y-axis in local transform space.
	 */
	private Vector3 up;

	/** the quaternion rotation of the transform */
	private Quaternion rotation;

	/** default rotation always {@link Quaternion#IDENTITY} */
	public Transform(Vector3 positionIn) {
		position = positionIn;
		rotation = new Quaternion(1, 0, 0, 0);
		forward = new Vector3(0, 0, 1);
		right = new Vector3(1, 0, 0);
		up = new Vector3(0, 1, 0);
	}

	public void setPosition(Vector3 positionIn) {
		gameObject.getMesh().translate(positionIn.subtract(position));
		position = positionIn;
	}

	public void move(Vector3 amount) {
		gameObject.getMesh().translate(amount);
		position = Vector3.add(position, amount);
	}

	public void rotate(Quaternion q) {
		rotation = rotation.multiply(q);
		gameObject.getMesh().rotate(q, position);
		forward.rotate(q);
		right.rotate(q);
		up.rotate(q);
	}

	/**
	 * returns the world-space equivilant of {@code point} in local space.
	 * example: in local space, the forward direction can always be represented
	 * by (0, 0, 1), but translating that into world space will return the
	 * instance's {@code forward} vector, which could be something like (0, 0.3,
	 * 0.4) if the transform is pitched up.
	 * 
	 * @param point the point to transform
	 * @return the world space coordinate
	 */
	public Vector3 transformToWorld(Vector3 point) {
		up = up.getNormalized();
		forward = forward.getNormalized();
		right = right.getNormalized();
		return point.multiply(new Matrix3x3(right, up, forward));
	}

	/**
	 * opposite of "transformToWorld". It returns a point with local-space
	 * coordindates equivilant to the inputted world-space coordinates.
	 * 
	 * @param point the point to transform
	 * @return the equivilant point in local space
	 */
	public Vector3 transformToLocal(Vector3 point) {
		up = up.getNormalized();
		forward = forward.getNormalized();
		right = right.getNormalized();
		return point.multiply(new Matrix3x3(right, up, forward).getInverse());
	}

	// #region getter/setter methods
	public Vector3 getForward() {
		return (forward = forward.getNormalized());
	}

	public Vector3 getUp() {
		return (up = up.getNormalized());
	}

	public Vector3 getRight() {
		return (right = right.getNormalized());
	}

	public Vector3 getPosition() {
		return position;
	}

	public GameObject getGameObject() {
		return gameObject;
	}

	public void setGameObject(GameObject gameObjectIn) {
		gameObject = gameObjectIn;
	}
	// #endregion
}
