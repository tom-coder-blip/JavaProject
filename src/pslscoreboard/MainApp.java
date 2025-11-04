/// The MainApp class is the main driver and user interface of the PSL Scoreboard application.
/// It creates a graphical interface (GUI) and connects the visual part of the program (buttons, tables, text areas)
/// with the core logic in the League class — making it interactive

package pslscoreboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the main entry point for the PSL Scoreboard application.
 * It creates a Swing GUI that allows users to input, process, and display football match results.
 */
public class MainApp {
    private final League league = new League(); // Holds the league logic (teams, matches, points)

    // Default list of PSL teams (these are preloaded when the app starts)
    private static final List<String> DEFAULT_TEAMS = Arrays.asList(
            "Mamelodi Sundowns",
            "Kaizer Chiefs",
            "Orlando Pirates",
            "SuperSport United",
            "Cape Town City",
            "Stellenbosch",
            "Sekhukhune United",
            "Maritzburg United",
            "Moroka Swallows",
            "Chippa United",
            "Richards Bay",
            "Golden Arrows",
            "AmaZulu",
            "Polokwane City",
            "Black Leopards",
            "Tuks"
    );

    // GUI components
    private final JFrame frame = new JFrame("PSL Scoreboard — Interactive");
    private final JTextArea inputArea = new JTextArea(12, 50);
    private final JTable table = new JTable();
    private final RankTableModel tableModel = new RankTableModel();
    private final JLabel statusLabel = new JLabel("Ready");

    // Constructor: sets up the interface and seeds default teams
    public MainApp() {
        initUI();
        league.seedTeams(DEFAULT_TEAMS); // Load default teams into the league
    }

    /**
     * Initializes the graphical user interface (GUI).
     * Creates all panels, buttons, tables, and layouts.
     */
    private void initUI() {
        // Set up theme colors (dark green with gold accents for a PSL look)
        Color bg = new Color(12, 68, 52);
        Color panel = new Color(20, 90, 68);
        Color accent = new Color(212, 175, 55);
        Color text = Color.WHITE;

        // Base window setup
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bg);

        // ===== Top Title =====
        JLabel title = new JLabel("PSL Scoreboard Builder");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(accent);
        title.setBorder(new EmptyBorder(10, 10, 0, 10));
        frame.add(title, BorderLayout.NORTH);

        // ===== Center Split Layout (Input on Left, Table on Right) =====
        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.45); // 45% input, 55% table

        // ===== Left Panel: Input Area =====
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(panel);
        left.setBorder(new EmptyBorder(10,10,10,10));

        JLabel instructions = new JLabel("<html>Paste match results (one per line):<br/>" +
                "<i>Format:</i> Team A 3, Team B 1</html>");
        instructions.setForeground(text);
        left.add(instructions, BorderLayout.NORTH);

        // Multi-line text area for match results
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(false);
        inputArea.setBackground(Color.WHITE);
        inputArea.setCaretColor(Color.BLACK);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        left.add(inputScroll, BorderLayout.CENTER);

        // Buttons under the input area
        JPanel leftButtons = new JPanel();
        leftButtons.setBackground(panel);
        JButton loadBtn = new JButton("Load File...");
        JButton processBtn = new JButton("Process");
        JButton resetBtn = new JButton("Reset League");
        JButton seedBtn = new JButton("Seed Default Teams");

        // Add buttons to panel
        leftButtons.add(loadBtn);
        leftButtons.add(processBtn);
        leftButtons.add(resetBtn);
        leftButtons.add(seedBtn);
        left.add(leftButtons, BorderLayout.SOUTH);
        split.setLeftComponent(left);

        // ===== Right Panel: Table Display =====
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(panel);
        right.setBorder(new EmptyBorder(10,10,10,10));
        table.setModel(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(table);
        right.add(tableScroll, BorderLayout.CENTER);

        // Quick controls at the top of the table area
        JPanel rightTop = new JPanel();
        rightTop.setBackground(panel);
        JButton exportBtn = new JButton("Export Ranking to Text File");
        JButton clearInputBtn = new JButton("Clear Input");
        rightTop.add(clearInputBtn);
        rightTop.add(exportBtn);
        right.add(rightTop, BorderLayout.NORTH);

        split.setRightComponent(right);
        frame.add(split, BorderLayout.CENTER);

        // ===== Bottom Status Bar =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(bg);
        statusLabel.setForeground(text);
        statusLabel.setBorder(new EmptyBorder(6,10,6,10));
        bottom.add(statusLabel, BorderLayout.WEST);
        frame.add(bottom, BorderLayout.SOUTH);

        // ===== Button Actions =====
        processBtn.addActionListener(e -> processInput()); // Parse text input and update table
        loadBtn.addActionListener(this::onLoadFile);        // Load results from file
        resetBtn.addActionListener(e -> {                   // Reset league
            league.clear();
            status("League cleared.");
            tableModel.setData(Collections.emptyList());
        });
        seedBtn.addActionListener(e -> {                    // Re-add default teams
            league.seedTeams(DEFAULT_TEAMS);
            status("Seeded default PSL teams.");
        });
        clearInputBtn.addActionListener(e -> inputArea.setText("")); // Clear input box
        exportBtn.addActionListener(e -> onExport());       // Export rankings to text file

        // ===== Final Window Setup =====
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Small styling tweaks for table
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    /**
     * Handles loading match data from a text file.
     */
    private void onLoadFile(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        int ret = chooser.showOpenDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                List<String> lines = Files.readAllLines(f.toPath());
                inputArea.setText(String.join("\n", lines)); // Display file content
                status("Loaded file: " + f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error reading file: " + ex.getMessage());
            }
        }
    }

    /**
     * Exports the current team ranking to a text file.
     */
    private void onExport() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                List<RankEntry> rows = tableModel.getRows();
                // Write each team's details line by line
                for (RankEntry r : rows) {
                    Team t = r.getTeam();
                    bw.write(String.format("%d. %s — %d pts (GF:%d GA:%d GD:%d)%n",
                            r.getRank(), t.getName(), t.getPoints(),
                            t.getGoalsFor(), t.getGoalsAgainst(), t.getGoalDifference()));
                }
                status("Exported ranking to " + f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Write error: " + ex.getMessage());
            }
        }
    }

    /**
     * Processes all input lines from the text area.
     * Each line is a match result in the format: "Team A 3, Team B 1"
     */
    private void processInput() {
        String text = inputArea.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please paste or load match results first.");
            return;
        }

        // Split text into lines and process each match result
        String[] lines = text.split("\\r?\\n");
        int success = 0, failed = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            boolean ok = league.processLine(line);
            if (ok) success++; else failed++;
        }

        // Update ranking table after processing
        List<RankEntry> ranking = league.getRanking();
        tableModel.setData(ranking);
        status(String.format("Processed: %d lines applied, %d failed.", success, failed));
    }

    // Update status label text at bottom
    private void status(String s) {
        statusLabel.setText(s);
    }

    /**
     * Starts the program. Create the GUI on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }

    /**
     * Table model used by JTable to display ranked teams in a table format.
     */
    static class RankTableModel extends AbstractTableModel {
        private final String[] cols = {"Rank", "Team", "Points", "MP", "GF", "GA", "GD"};
        private List<RankEntry> rows = new ArrayList<>();

        // Updates the table data and refreshes the display
        public void setData(List<RankEntry> rows) {
            this.rows = rows == null ? new ArrayList<>() : rows;
            fireTableDataChanged();
        }

        public List<RankEntry> getRows() { return rows; }

        @Override
        public int getRowCount() { return rows.size(); }

        @Override
        public int getColumnCount() { return cols.length; }

        @Override
        public String getColumnName(int column) { return cols[column]; }

        // Determines what data to show in each column
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RankEntry re = rows.get(rowIndex);
            Team t = re.getTeam();
            switch (columnIndex) {
                case 0: return re.getRank();
                case 1: return t.getName();
                case 2: return t.getPoints();
                case 3: return t.getMatchesPlayed();
                case 4: return t.getGoalsFor();
                case 5: return t.getGoalsAgainst();
                case 6: return t.getGoalDifference();
                default: return "";
            }
        }
    }
}