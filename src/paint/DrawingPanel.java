package paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

/** Zone de dessin : gère l'image, la souris, l'annulation et le remplissage. */
public class DrawingPanel extends JPanel {

    private BufferedImage canvas;
    private final Deque<BufferedImage> undoStack = new ArrayDeque<>();
    private static final int MAX_UNDO = 30;

    private Tool tool = Tool.PENCIL;
    private Color color = Color.BLACK;
    private int strokeSize = 3;
    private boolean filled = false;

    private Point start, current;
    private Point last;

    public DrawingPanel(int w, int h) {
        newCanvas(w, h);
        setPreferredSize(new Dimension(w, h));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { onPress(e.getPoint()); }
            @Override public void mouseDragged(MouseEvent e) { onDrag(e.getPoint()); }
            @Override public void mouseReleased(MouseEvent e) { onRelease(e.getPoint()); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // ---------- Accesseurs ----------
    public void setTool(Tool t) { this.tool = t; }
    public void setColor(Color c) { this.color = c; }
    public Color getColor() { return color; }
    public void setStrokeSize(int s) { this.strokeSize = s; }
    public void setFilled(boolean f) { this.filled = f; }
    public BufferedImage getCanvas() { return canvas; }

    // ---------- Canevas ----------
    public void newCanvas(int w, int h) {
        canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        undoStack.clear();
        repaint();
    }

    public void setImage(BufferedImage img) {
        saveUndo();
        BufferedImage copy = new BufferedImage(Math.max(img.getWidth(), canvas.getWidth()),
                Math.max(img.getHeight(), canvas.getHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        canvas = copy;
        setPreferredSize(new Dimension(canvas.getWidth(), canvas.getHeight()));
        revalidate();
        repaint();
    }

    public void clear() {
        saveUndo();
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.dispose();
        repaint();
    }

    // ---------- Annuler ----------
    private void saveUndo() {
        BufferedImage copy = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(canvas, 0, 0, null);
        g.dispose();
        undoStack.push(copy);
        if (undoStack.size() > MAX_UNDO) undoStack.removeLast();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            canvas = undoStack.pop();
            repaint();
        }
    }

    // ---------- Souris ----------
    private void onPress(Point p) {
        start = p;
        current = p;
        last = p;
        saveUndo();
        if (tool == Tool.FILL) {
            floodFill(p.x, p.y, color);
            repaint();
        } else if (tool == Tool.PENCIL || tool == Tool.ERASER) {
            drawSegment(p, p);
        }
    }

    private void onDrag(Point p) {
        current = p;
        if (tool == Tool.PENCIL || tool == Tool.ERASER) {
            drawSegment(last, p);
            last = p;
        }
        repaint();
    }

    private void onRelease(Point p) {
        current = p;
        if (tool == Tool.LINE || tool == Tool.RECT || tool == Tool.OVAL) {
            Graphics2D g = canvas.createGraphics();
            setup(g, color);
            drawShape(g, start, p);
            g.dispose();
        }
        start = null;
        repaint();
    }

    private void drawSegment(Point a, Point b) {
        Graphics2D g = canvas.createGraphics();
        setup(g, tool == Tool.ERASER ? Color.WHITE : color);
        if (tool == Tool.ERASER) g.setStroke(new BasicStroke(strokeSize * 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(a.x, a.y, b.x, b.y);
        g.dispose();
    }

    private void setup(Graphics2D g, Color c) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c);
        g.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private void drawShape(Graphics2D g, Point a, Point b) {
        int x = Math.min(a.x, b.x), y = Math.min(a.y, b.y);
        int w = Math.abs(a.x - b.x), h = Math.abs(a.y - b.y);
        switch (tool) {
            case LINE:
                g.drawLine(a.x, a.y, b.x, b.y);
                break;
            case RECT:
                if (filled) g.fill(new Rectangle2D.Double(x, y, w, h));
                else g.draw(new Rectangle2D.Double(x, y, w, h));
                break;
            case OVAL:
                if (filled) g.fill(new Ellipse2D.Double(x, y, w, h));
                else g.draw(new Ellipse2D.Double(x, y, w, h));
                break;
            default:
        }
    }

    // ---------- Remplissage (pot de peinture) ----------
    private void floodFill(int x, int y, Color fill) {
        if (x < 0 || y < 0 || x >= canvas.getWidth() || y >= canvas.getHeight()) return;
        int target = canvas.getRGB(x, y);
        int repl = fill.getRGB();
        if (target == repl) return;
        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(x, y));
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            if (p.x < 0 || p.y < 0 || p.x >= canvas.getWidth() || p.y >= canvas.getHeight()) continue;
            if (canvas.getRGB(p.x, p.y) != target) continue;
            // balayage horizontal
            int l = p.x, r = p.x;
            while (l > 0 && canvas.getRGB(l - 1, p.y) == target) l--;
            while (r < canvas.getWidth() - 1 && canvas.getRGB(r + 1, p.y) == target) r++;
            for (int i = l; i <= r; i++) {
                canvas.setRGB(i, p.y, repl);
                if (p.y > 0 && canvas.getRGB(i, p.y - 1) == target) stack.push(new Point(i, p.y - 1));
                if (p.y < canvas.getHeight() - 1 && canvas.getRGB(i, p.y + 1) == target) stack.push(new Point(i, p.y + 1));
            }
        }
    }

    // ---------- Affichage ----------
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.drawImage(canvas, 0, 0, null);
        if (start != null && current != null && (tool == Tool.LINE || tool == Tool.RECT || tool == Tool.OVAL)) {
            setup(g, color);
            drawShape(g, start, current);
        }
    }
}
