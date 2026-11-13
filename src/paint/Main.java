package paint;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Application Paint (Java Swing). */
public class Main extends JFrame {

    private final DrawingPanel panel = new DrawingPanel(900, 600);
    private final JButton colorBtn = new JButton("   ");

    public Main() {
        super("Paint - Java GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());
        add(createToolBar(), BorderLayout.NORTH);
        add(new JScrollPane(panel), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Fichier");
        file.add(item("Nouveau", KeyEvent.VK_N, e -> panel.newCanvas(900, 600)));
        file.add(item("Ouvrir...", KeyEvent.VK_O, e -> openImage()));
        file.add(item("Enregistrer (PNG)...", KeyEvent.VK_S, e -> saveImage()));
        file.addSeparator();
        file.add(item("Quitter", KeyEvent.VK_Q, e -> System.exit(0)));

        JMenu edit = new JMenu("Edition");
        edit.add(item("Annuler", KeyEvent.VK_Z, e -> panel.undo()));
        edit.add(item("Effacer tout", KeyEvent.VK_E, e -> panel.clear()));

        bar.add(file);
        bar.add(edit);
        return bar;
    }

    private JMenuItem item(String text, int key, java.awt.event.ActionListener l) {
        JMenuItem it = new JMenuItem(text);
        it.setAccelerator(KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        it.addActionListener(l);
        return it;
    }

    private JToolBar createToolBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        ButtonGroup group = new ButtonGroup();
        for (Tool t : Tool.values()) {
            JToggleButton b = new JToggleButton(t.toString());
            b.addActionListener(e -> panel.setTool(t));
            if (t == Tool.PENCIL) b.setSelected(true);
            group.add(b);
            tb.add(b);
        }
        tb.addSeparator();

        colorBtn.setBackground(panel.getColor());
        colorBtn.setOpaque(true);
        colorBtn.setToolTipText("Choisir la couleur");
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Couleur", panel.getColor());
            if (c != null) {
                panel.setColor(c);
                colorBtn.setBackground(c);
            }
        });
        tb.add(new JLabel(" Couleur : "));
        tb.add(colorBtn);

        // palette rapide
        Color[] palette = {Color.BLACK, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
        for (Color c : palette) {
            JButton q = new JButton();
            q.setPreferredSize(new Dimension(20, 20));
            q.setMaximumSize(new Dimension(20, 20));
            q.setBackground(c);
            q.setOpaque(true);
            q.addActionListener(e -> {
                panel.setColor(c);
                colorBtn.setBackground(c);
            });
            tb.add(q);
        }
        tb.addSeparator();

        tb.add(new JLabel(" Epaisseur : "));
        JSpinner spin = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        spin.setMaximumSize(new Dimension(60, 28));
        spin.addChangeListener(e -> panel.setStrokeSize((Integer) spin.getValue()));
        tb.add(spin);

        JCheckBox fill = new JCheckBox("Rempli");
        fill.addActionListener(e -> panel.setFilled(fill.isSelected()));
        tb.add(fill);
        return tb;
    }

    private void saveImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image PNG", "png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) f = new File(f.getPath() + ".png");
            try {
                ImageIO.write(panel.getCanvas(), "png", f);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'enregistrement : " + ex.getMessage());
            }
        }
    }

    private void openImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "gif", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = ImageIO.read(fc.getSelectedFile());
                if (img != null) panel.setImage(img);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'ouverture : " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
