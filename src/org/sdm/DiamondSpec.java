package org.sdm;

public class DiamondSpec {

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

    public DiamondSpec(long date, int reportNr, String shape, double carot, double depth, double table, double girdleThickness, double culetSize, int polish, int symmetry, int clarity, int color, int cut, int fluorescence, String laserInscription) {
        this.date = date;
        this.reportNr = reportNr;
        this.shape = shape;
        this.carot = carot;
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
}
