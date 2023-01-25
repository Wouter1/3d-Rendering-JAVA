package trrt.rendering3d.primitives;

public class Matrix3x3 {
	// R means row and C means column. R2C3 would be second row third column.
	public final double R1C1, R1C2, R1C3, R2C1, R2C2, R2C3, R3C1, R3C2, R3C3;

	// overloaded constructor which accepts three Vector3s.
	public Matrix3x3(Vector3 column1, Vector3 column2, Vector3 column3) {
		R1C1 = column1.x;
		R1C2 = column2.x;
		R1C3 = column3.x;
		R2C1 = column1.y;
		R2C2 = column2.y;
		R2C3 = column3.y;
		R3C1 = column1.z;
		R3C2 = column2.z;
		R3C3 = column3.z;
	}

	// overloaded constructor which allows all 9 values of the matrix.
	public Matrix3x3(double r1c1, double r1c2, double r1c3, double r2c1,
			double r2c2, double r2c3, double r3c1, double r3c2, double r3c3) {
		R1C1 = r1c1;
		R1C2 = r1c2;
		R1C3 = r1c3;
		R2C1 = r2c1;
		R2C2 = r2c2;
		R2C3 = r2c3;
		R3C1 = r3c1;
		R3C2 = r3c2;
		R3C3 = r3c3;
	}

	// formats the values in the matrix into a string.
	@Override
	public String toString() {
		return String.format(
				"\n|%39s\n|%10.2f%10.2f%10.2f%9s\n|%39s\n|%10.2f%10.2f%10.2f%9s\n|%39s\n|%10.2f%10.2f%10.2f%9s\n|%39s\n",
				"|", R1C1, R1C2, R1C3, "|", "|", R2C1, R2C2, R2C3, "|", "|",
				R3C1, R3C2, R3C3, "|", "|");
	}

	/**
	 * @return the determinant of the 3x3 matrix.
	 * 
	 */
	public double getDeterminant() {
		return R1C1 * (R2C2 * R3C3 - R2C3 * R3C2)
				- R1C2 * (R2C1 * R3C3 - R2C3 * R3C1)
				+ R1C3 * (R2C1 * R3C2 - R2C2 * R3C1);
	}

	/**
	 * 
	 * @return the cofactor matrix
	 */
	public Matrix3x3 getCofactorMatrix() {
		return new Matrix3x3(R2C2 * R3C3 - R2C3 * R3C2,
				-(R2C1 * R3C3 - R2C3 * R3C1), R2C1 * R3C2 - R2C2 * R3C1,
				-(R1C2 * R3C3 - R1C3 * R3C2), R1C1 * R3C3 - R1C3 * R3C1,
				-(R1C1 * R3C2 - R1C2 * R3C1), R1C2 * R2C3 - R1C3 * R2C2,
				-(R1C1 * R2C3 - R1C3 * R2C1), R1C1 * R2C2 - R1C2 * R2C1);
	}

	/**
	 * 
	 * @return the adjugate matrix, basically just the transposed cofactor
	 *         matrix
	 */
	public Matrix3x3 getAdjugateMatrix() {
		return new Matrix3x3(R2C2 * R3C3 - R2C3 * R3C2,
				-(R1C2 * R3C3 - R1C3 * R3C2), R1C2 * R2C3 - R1C3 * R2C2,
				-(R2C1 * R3C3 - R2C3 * R3C1), R1C1 * R3C3 - R1C3 * R3C1,
				-(R1C1 * R2C3 - R1C3 * R2C1), R2C1 * R3C2 - R2C2 * R3C1,
				-(R1C1 * R3C2 - R1C2 * R3C1), R1C1 * R2C2 - R1C2 * R2C1);
	}

	/**
	 * 
	 * @return the inverse of the matrix, which is just the adjugate/det NOTE
	 *         this will be unstable if det approaches 0 and you will get div by
	 *         zero error if matrix is singular.
	 */
	public Matrix3x3 getInverse() {
		return getAdjugateMatrix().multiply(1 / getDeterminant());
	}

	/**
	 * 
	 * @param m2
	 * @return this.m2 so this matrix multiplied with m2
	 */
	public Matrix3x3 multiply(Matrix3x3 m2) {
		return new Matrix3x3(R1C1 * m2.R1C1 + R1C2 * m2.R2C1 + R1C3 * m2.R3C1,
				R1C1 * m2.R1C2 + R1C2 * m2.R2C2 + R1C3 * m2.R3C2,
				R1C1 * m2.R1C3 + R1C2 * m2.R2C3 + R1C3 * m2.R3C3,
				R2C1 * m2.R1C1 + R2C2 * m2.R2C1 + R2C3 * m2.R3C1,
				R2C1 * m2.R1C2 + R2C2 * m2.R2C2 + R2C3 * m2.R3C2,
				R2C1 * m2.R1C3 + R2C2 * m2.R2C3 + R2C3 * m2.R3C3,
				R3C1 * m2.R1C1 + R3C2 * m2.R2C1 + R3C3 * m2.R3C1,
				R3C1 * m2.R1C2 + R3C2 * m2.R2C2 + R3C3 * m2.R3C2,
				R3C1 * m2.R1C3 + R3C2 * m2.R2C3 + R3C3 * m2.R3C3);
	}

	/**
	 * 
	 * @param scalar
	 * @return this*scalar (=all elements multiplied by scalar)
	 */
	// multiplies a matrix by a scalar value
	public Matrix3x3 multiply(double scalar) {
		return new Matrix3x3(R1C1 * scalar, R1C2 * scalar, R1C3 * scalar,
				R2C1 * scalar, R2C2 * scalar, R2C3 * scalar, R3C1 * scalar,
				R3C2 * scalar, R3C3 * scalar);
	}

}
