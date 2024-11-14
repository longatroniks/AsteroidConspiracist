package dte.masteriot.mdp.asteroidconspiracist.recyclerview.legend;

public class LegendItem {
    private final int color;
    private final String label;

    public LegendItem(int color, String label) {
        this.color = color;
        this.label = label;
    }

    public int getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }
}
