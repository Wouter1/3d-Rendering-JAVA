package trrt.rendering3d.graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import trrt.rendering3d.gameObject.GameObject;
import trrt.rendering3d.primitives.Vector3;

public class Camera {
	/**
	 * field of view of the camera. Strictly reffers to the horizontal fov as
	 * vertical fov is based off screen height
	 */
	private double fov;

	/** position of the camera in world-space */
	private Vector3 position;

	/** the direction the camera is facing as a normalized vector */
	private Vector3 directionVector;

	/** yaw of camera in radians */
	private double hAngle;

	/** pitch of camera in radians */
	private double vAngle;

	/**
	 * distance that the render plane is from the camera (this can be any
	 * reasonable number and has little effet on preformance)
	 */
	private double renderPlaneDistance;

	/** the maximum distance at which this camera will render triangles */
	private double farClipDistance;

	/** the minimum distance at which this camera will render triangles */
	private double nearClipDistance;

	// movement controllers.
	private OrbitCamController orbitController = null;
	private FreeCamController freeCamController = null;

	/** width of the render plane based off fov. */
	private double renderPlaneWidth;

	public Camera(Vector3 positionIn, double farClipDistanceIn,
			double nearClipDistanceIn, double fovIn) {
		renderPlaneDistance = 50;
		hAngle = 0;
		vAngle = 0;
		position = positionIn;
		farClipDistance = farClipDistanceIn;
		nearClipDistance = nearClipDistanceIn;
		directionVector = Vector3.angleToVector(hAngle, vAngle);
		setFov(fovIn);
	}

	/**
	 * sets the v and h angles to look at the specified position.
	 * 
	 * @param pos position to look at in world space
	 */
	public void lookAt(Vector3 pos) {
		hAngle = (pos.x - position.x < 0)
				? -Math.atan((pos.z - position.z) / (pos.x - position.x))
						- Math.PI / 2
				: Math.PI / 2 - Math
						.atan((pos.z - position.z) / (pos.x - position.x));

		vAngle = Math.atan((pos.y - position.y)
				/ (Math.sqrt((pos.x - position.x) * (pos.x - position.x)
						+ (pos.z - position.z) * (pos.z - position.z))));

		hAngle %= Math.PI;
		vAngle %= Math.PI;
		directionVector = Vector3.angleToVector(hAngle, vAngle);
	}

	/**
	 * camera controller which orbits a specified GameObject. panning the camera
	 * will cause it to circle around that object. The user can also use the
	 * scroll wheel to zoom in and out from the game object.
	 */
	class OrbitCamController
			implements MouseMotionListener, MouseWheelListener, MouseListener {
		private int maxDistance = (int) farClipDistance; // maximum distance the
															// camera can be
															// from the object
		private int minDistance = (int) nearClipDistance; // minimum distance

		private double distance; // distacne from the object

		private double maxAngle = Math.PI / 2 - 0.1; // the maximum angle the
														// camera can go up to
		private double minAngle = -Math.PI / 2 + 0.1; // the minimum angle the
														// camera can go down
														// to.

		private GameObject focusObj; // the game object that the camera is
										// focused on.
		private double sensitivity; // how fast should the camera pan?
		private double scrollSensitivity;

		private int prevX = 0;
		private int prevY = 0;

		private Vector3 difference;
		private Vector3 directionUnit = Vector3.ZERO;

		public OrbitCamController(GameObject focusObjectIn,
				double sensitivityIn, double scrollSense) {
			focusObj = focusObjectIn;
			distance = (minDistance + maxDistance) / 2.0;
			sensitivity = sensitivityIn;
			scrollSensitivity = scrollSense;

			// sets up the position of the camera.
			position = Vector3.add(focusObj.getTransform().getPosition(),
					new Vector3(0, 0, -distance));
			directionUnit = position
					.subtract(focusObj.getTransform().getPosition())
					.getNormalized();
			difference = Vector3.multiply(directionUnit, distance);
		}

		// changes the distance based on the mouse movement.
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			distance = Math.max(minDistance,
					Math.min(
							distance + e.getWheelRotation() * scrollSensitivity,
							maxDistance));
			difference = Vector3.multiply(directionUnit, distance);
			updatePosition();
		}

		// pans the camera
		@Override
		public void mouseDragged(MouseEvent e) {
			directionUnit = Vector3.rotateAroundYaxis(directionUnit,
					(e.getX() - prevX) / (2000 / sensitivity));
			if (vAngle > -maxAngle
					&& (e.getY() - prevY) / (200 / sensitivity) > 0)
				directionUnit = Vector3.rotateAroundYaxis(
						Vector3.rotateAroundXaxis(
								Vector3.rotateAroundYaxis(directionUnit,
										-hAngle),
								(e.getY() - prevY) / (2000 / sensitivity)),
						hAngle);
			else if (vAngle < -minAngle
					&& (e.getY() - prevY) / (200 / sensitivity) < 0)
				directionUnit = Vector3.rotateAroundYaxis(
						Vector3.rotateAroundXaxis(
								Vector3.rotateAroundYaxis(directionUnit,
										-hAngle),
								(e.getY() - prevY) / (2000 / sensitivity)),
						hAngle);
			difference = Vector3.multiply(directionUnit, distance);
			updatePosition();
			lookAt(focusObj.getTransform().getPosition());
			vAngle = Math.max(-89, Math.min(89, vAngle));
			prevX = e.getX();
			prevY = e.getY();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			prevX = e.getX();
			prevY = e.getY();
		}

		public void setSensitivity(double sens) {
			sensitivity = sens;
		}

		// updates the position of the camera to be around the focusObject.
		public void updatePosition() {
			position = Vector3.add(focusObj.getTransform().getPosition(),
					difference);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	// used mainly for debug, but a simple controller which lets the user fly up
	// down left right forward backward.
	class FreeCamController
			implements KeyListener, MouseMotionListener, MouseListener {
		private int prevX = 0;
		private int prevY = 0;
		private double sensitivity;
		private double movementSpeed;

		public FreeCamController(double sensitivityIn, double movementSpeedIn) {
			sensitivity = sensitivityIn;
			movementSpeed = movementSpeedIn;
		}

		// for camera panning.
		@Override
		public void mouseDragged(MouseEvent e) {
			hAngle = hAngle + (e.getX() - prevX) / (100 / sensitivity);
			vAngle = vAngle - (e.getY() - prevY) / (100 / sensitivity);
			if (hAngle < 0)
				hAngle += 360;
			if (vAngle < 0)
				hAngle += 360;
			hAngle %= 360;
			vAngle %= 360;

			prevX = e.getX();
			prevY = e.getY();
			directionVector = Vector3.angleToVector(hAngle, vAngle);
		}

		// checks for keys
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyChar()) {
			case 'w':
				moveForward(movementSpeed);
				break;
			case 's':
				moveForward(-movementSpeed);
				break;
			case 'a':
				moveLeft(movementSpeed);
				break;
			case 'd':
				moveLeft(-movementSpeed);
				break;
			case 'e':
				moveUp(movementSpeed);
				break;
			case 'q':
				moveUp(-movementSpeed);
				break;
			}
		}

		private void moveForward(double distanceIn) {
			position = position.add(Vector3.multiply(
					Vector3.angleToVector(hAngle, vAngle), distanceIn));
		}

		private void moveLeft(double distanceIn) {
			position = position.add(Vector3.multiply(
					Vector3.angleToVector(hAngle - Math.PI / 2, 0),
					distanceIn));
		}

		private void moveUp(double distanceIn) {
			position = position.add(Vector3.multiply(
					Vector3.angleToVector(hAngle, vAngle + Math.PI / 2),
					distanceIn));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			prevX = e.getX();
			prevY = e.getY();
		}

		public void setFocusObj(GameObject obj) {
			orbitController.focusObj = obj;
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	/**
	 * sets the controls of the camera to orbit mode, with the needed values for
	 * the constructor.
	 */
	public void setOrbitControls(JPanel panel, GameObject focusObject,
			double sensitivity, double scrollSens) {
		orbitController = new OrbitCamController(focusObject, sensitivity,
				scrollSens);
		panel.addMouseListener(orbitController);
		panel.addMouseMotionListener(orbitController);
		panel.addMouseWheelListener(orbitController);
	}

	/** sets the controls to free cam mode. */
	public void setFreeControls(JPanel panel, double movementSpeed,
			double sensitivity) {
		freeCamController = new FreeCamController(sensitivity, movementSpeed);
		panel.addKeyListener(freeCamController);
		panel.addMouseListener(freeCamController);
		panel.addMouseMotionListener(freeCamController);
	}

	// calculates the render plane width, which is a slightly expensive method,
	// so it is only called once.
	private double calculateRenderPlaneWidth() {
		return Math.tan(fov / 2) * renderPlaneDistance * 2;
	}

	// #region getter/setter methods
	public double getFarClipDistancee() {
		return farClipDistance;
	}

	public double getNearClipDistance() {
		return nearClipDistance;
	}

	public OrbitCamController getOrbitCamController() {
		return orbitController;
	}

	public FreeCamController getFreeCamController() {
		return freeCamController;
	}

	public void setFov(double fovIn) {
		fov = Math.toRadians(fovIn);
		renderPlaneWidth = calculateRenderPlaneWidth();
	}

	public void setSensitivity(double sense) {
		if (orbitController != null)
			orbitController.setSensitivity(sense);
	}

	public double getRenderPlaneDistance() {
		return renderPlaneDistance;
	}

	public Vector3 getDirectionVector() {
		return directionVector;
	}

	public double getHorientation() {
		return hAngle;
	}

	public double getVorientation() {
		return vAngle;
	}

	public Vector3 getPosition() {
		return position;
	}

	public double getRenderPlaneWidth() {
		return renderPlaneWidth;
	}

	// #endregion
}
