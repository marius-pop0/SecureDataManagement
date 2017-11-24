package org.sdm.blockchain;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiamondSpec implements Serializable {

	private long date;
	private int reportNr;
	private String shape;
	private double carot;
	private double depth;
	private double table;
	private double girdleThickness;
	private double culetSize;
	private int polish;
	private int symmetry;
	private int clarity;
	private int color;
	private int cut;
	private int fluorescence;
	private String laserInscription;
	private String origin;
	private boolean natural;

	private byte[] id;

	public DiamondSpec(long date,
					   int reportNr,
					   String shape,
					   double carat,
					   double depth,
					   double table,
					   double girdleThickness,
					   double culetSize,
					   int polish,
					   int symmetry,
					   int clarity,
					   int color,
					   int cut,
					   int fluorescence,
					   String laserInscription,
					   String origin,
					   boolean natural) {

		this.date = date;
		this.reportNr = reportNr;
		this.shape = shape;
		this.carot = carat;
		this.depth = depth;
		this.table = table;
		this.girdleThickness = girdleThickness;
		this.culetSize = culetSize;
		this.polish = polish;
		this.symmetry = symmetry;
		this.clarity = clarity;
		this.color = color;
		this.cut = cut;
		this.fluorescence = fluorescence;
		this.laserInscription = laserInscription;
		this.origin = origin;
		this.natural = natural;

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			this.id = digest.digest(getDiamondBytes());
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public long getDate() {
		return date;
	}

	public int getReportNr() {
		return reportNr;
	}

	public String getShape() {
		return shape;
	}

	public double getCarot() {
		return carot;
	}

	public double getDepth() {
		return depth;
	}

	public double getTable() {
		return table;
	}

	public double getGirdleThickness() {
		return girdleThickness;
	}

	public double getCuletSize() {
		return culetSize;
	}

	public int getPolish() {
		return polish;
	}

	public int getSymmetry() {
		return symmetry;
	}

	public int getClarity() {
		return clarity;
	}

	public int getColor() {
		return color;
	}

	public int getCut() {
		return cut;
	}

	public int getFluorescence() {
		return fluorescence;
	}

	public String getLaserInscription() {
		return laserInscription;
	}

	public String getOrigin() {
		return origin;
	}

	public boolean isNatural() {
		return natural;
	}

	public byte[] getId() {
		return id;
	}

	public byte[] getDiamondBytes() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(bos);
		o.writeObject(this);
		o.flush();
		byte[] bytes = bos.toByteArray();
		bos.close();
		return bytes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DiamondSpec that = (DiamondSpec) o;

		if (date != that.date) return false;
		if (reportNr != that.reportNr) return false;
		if (Double.compare(that.carot, carot) != 0) return false;
		if (Double.compare(that.depth, depth) != 0) return false;
		if (Double.compare(that.table, table) != 0) return false;
		if (Double.compare(that.girdleThickness, girdleThickness) != 0) return false;
		if (Double.compare(that.culetSize, culetSize) != 0) return false;
		if (polish != that.polish) return false;
		if (symmetry != that.symmetry) return false;
		if (clarity != that.clarity) return false;
		if (color != that.color) return false;
		if (cut != that.cut) return false;
		if (fluorescence != that.fluorescence) return false;
		if (natural != that.natural) return false;
		if (!shape.equals(that.shape)) return false;
		if (!laserInscription.equals(that.laserInscription)) return false;
		return origin.equals(that.origin);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = (int) (date ^ (date >>> 32));
		result = 31 * result + reportNr;
		result = 31 * result + shape.hashCode();
		temp = Double.doubleToLongBits(carot);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(depth);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(table);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(girdleThickness);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(culetSize);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + polish;
		result = 31 * result + symmetry;
		result = 31 * result + clarity;
		result = 31 * result + color;
		result = 31 * result + cut;
		result = 31 * result + fluorescence;
		result = 31 * result + laserInscription.hashCode();
		result = 31 * result + origin.hashCode();
		result = 31 * result + (natural ? 1 : 0);
		return result;
	}

	public static DiamondSpec deserialize(byte[] bytes) {
		DiamondSpec d = null;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			 ObjectInputStream in = new ObjectInputStream(bis)) {
			d = (DiamondSpec) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return d;
	}

}
