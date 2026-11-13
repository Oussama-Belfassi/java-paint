package paint;

/** Outils disponibles dans l'application Paint. */
public enum Tool {
    PENCIL("Crayon"), LINE("Ligne"), RECT("Rectangle"), OVAL("Ovale"), ERASER("Gomme"), FILL("Remplir");

    private final String label;

    Tool(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
