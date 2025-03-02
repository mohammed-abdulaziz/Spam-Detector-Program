package csci2020u.assignment01;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class SpamDetectorGUI {
    private final JFrame frame;
    private final SpamDetector spamDetector;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;
    private final JLabel accuracyLabel;
    private final JLabel precisionLabel;
    private final JLabel recallLabel;
    private final JLabel f1ScoreLabel;
    private File currentDataFolder;

    public SpamDetectorGUI() {
        spamDetector = new SpamDetector();

        // Setup the main frame
        frame = new JFrame("Spam Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Top Panel (File Selection and Status)
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectFolderButton = new JButton("Select Data Folder");
        buttonPanel.add(selectFolderButton);

        statusLabel = new JLabel("Status: Ready to load data");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.BOLD, 12));

        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.CENTER);


        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // Middle Panel (Results Table)
        JPanel middlePanel = new JPanel(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder(null, "Test Results", TitledBorder.CENTER, TitledBorder.TOP);
        middlePanel.setBorder(border);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        tableModel.addColumn("Filename");
        tableModel.addColumn("Spam Probability");
        tableModel.addColumn("Actual Class");
        tableModel.addColumn("Classified As");

        JTable resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);

        // Custom renderer for coloring cells based on classification
        resultsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String actualClass = (String) table.getModel().getValueAt(modelRow, 2);
                    String classifiedAs = (String) table.getModel().getValueAt(modelRow, 3);


                    setHorizontalAlignment(JLabel.LEFT);

                    // Color based on correct/incorrect classification
                    if (actualClass.equals(classifiedAs)) {
                        // Correct classification
                        if (actualClass.equals("spam")) {
                            c.setBackground(new Color(255, 235, 235)); // Light red for correct spam
                        } else {
                            c.setBackground(new Color(235, 255, 235)); // Light green for correct ham
                        }
                    } else {
                        // Incorrect classification
                        c.setBackground(new Color(255, 255, 200)); // Light yellow for errors
                    }

                    // Highlight the "Classified As" column
                    if (column == 3) {
                        setFont(getFont().deriveFont(Font.BOLD));
                        if (classifiedAs.equals("spam")) {
                            setForeground(new Color(180, 0, 0)); // Dark red for spam
                        } else {
                            setForeground(new Color(0, 100, 0)); // Dark green for ham
                        }
                    } else {
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        // Adjust column widths
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Filename
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Probability
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Actual Class
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Classified As

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        middlePanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(middlePanel, BorderLayout.CENTER);

        // Bottom Panel (Evaluation Metrics)
        JPanel evaluationPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        evaluationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.GRAY),
                                "Evaluation Metrics",
                                TitledBorder.CENTER,
                                TitledBorder.TOP))
        ));

        accuracyLabel = new JLabel("Accuracy: N/A", JLabel.CENTER);
        precisionLabel = new JLabel("Precision: N/A", JLabel.CENTER);
        recallLabel = new JLabel("Recall: N/A", JLabel.CENTER);
        f1ScoreLabel = new JLabel("F1-Score: N/A", JLabel.CENTER);

        // Make metrics labels more prominent
        Font metricsFont = new Font(accuracyLabel.getFont().getName(), Font.BOLD, 14);
        accuracyLabel.setFont(metricsFont);
        precisionLabel.setFont(metricsFont);
        recallLabel.setFont(metricsFont);
        f1ScoreLabel.setFont(metricsFont);

        evaluationPanel.add(accuracyLabel);
        evaluationPanel.add(precisionLabel);
        evaluationPanel.add(recallLabel);
        evaluationPanel.add(f1ScoreLabel);

        mainPanel.add(evaluationPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        // Add ActionListener to the "Select Folder" button
        selectFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Select Folder Containing Spam/Ham Data");

                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    currentDataFolder = fileChooser.getSelectedFile();
                    processSelectedFolder(currentDataFolder);
                }
            }
        });
    }

    // Process the selected folder and train the model
    private void processSelectedFolder(File selectedFolder) {
        statusLabel.setText("Status: Loading and training data...");

        // Perform training in a background thread to keep UI responsive
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                System.out.println("Selected Directory: " + selectedFolder.getAbsolutePath());
                return spamDetector.train(selectedFolder);
            }

            @Override
            protected void done() {
                try {
                    boolean trainingSuccessful = get();
                    if (!trainingSuccessful) {
                        JOptionPane.showMessageDialog(frame,
                                "Training failed: Could not find required directories.\n\n" +
                                        "The application is looking for directories with this structure:\n" +
                                        "- train/spam\n" +
                                        "- train/ham\n" +
                                        "- test/spam\n" +
                                        "- test/ham\n\n" +
                                        "Please select a directory containing this structure.",
                                "Training Error",
                                JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("Status: Training failed. Please select a valid data directory.");
                        return;
                    }

                    statusLabel.setText("Status: Training complete. Classifying emails...");

                    // Now classify the emails
                    classifyEmails(selectedFolder);

                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Status: Error during training: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    // Classify emails and compute evaluation metrics
    private void classifyEmails(File selectedFolder) {
        SwingWorker<List<TestFile>, Void> worker = new SwingWorker<List<TestFile>, Void>() {
            @Override
            protected List<TestFile> doInBackground() throws Exception {
                return spamDetector.classifyEmails(selectedFolder);
            }

            @Override
            protected void done() {
                try {
                    List<TestFile> testResults = get();
                    tableModel.setRowCount(0); // Clear previous results

                    int correctCount = 0, truePositives = 0, falsePositives = 0, falseNegatives = 0, trueNegatives = 0;
                    if (testResults.isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                                "Classification failed: Could not find test files.\n\n" +
                                        "Please ensure your directory contains test files in:\n" +
                                        "- test/spam\n" +
                                        "- test/ham",
                                "Classification Error",
                                JOptionPane.WARNING_MESSAGE);
                        statusLabel.setText("Status: Classification failed. No test files found.");
                        return;
                    }

                    double threshold = 0.7;

                    // Populate table and calculate metrics
                    for (TestFile testFile : testResults) {
                        String classifiedAs = testFile.getSpamProbability() >= threshold ? "spam" : "ham";

                        if (classifiedAs.equals(testFile.getActualClass())) {
                            correctCount++;
                            if (classifiedAs.equals("spam")) {
                                truePositives++;
                            } else {
                                trueNegatives++;
                            }
                        } else {
                            if (classifiedAs.equals("spam")) {
                                falsePositives++;
                            } else {
                                falseNegatives++;
                            }
                        }

                        tableModel.addRow(new Object[]{testFile.getFilename(), testFile.getSpamProbRounded(),
                                testFile.getActualClass(), classifiedAs});
                    }

                    // Compute evaluation metrics
                    double accuracy = (double) correctCount / testResults.size() * 100;
                    double precision = (truePositives + falsePositives) > 0 ?
                            (double) truePositives / (truePositives + falsePositives) * 100 : 0;
                    double recall = (truePositives + falseNegatives) > 0 ?
                            (double) truePositives / (truePositives + falseNegatives) * 100 : 0;
                    double f1Score = (precision + recall) > 0 ?
                            2 * (precision * recall) / (precision + recall) : 0;

                    // Update UI labels with color coding
                    accuracyLabel.setText(String.format("Accuracy: %.2f%%", accuracy));
                    colorMetricLabel(accuracyLabel, accuracy);

                    precisionLabel.setText(String.format("Precision: %.2f%%", precision));
                    colorMetricLabel(precisionLabel, precision);

                    recallLabel.setText(String.format("Recall: %.2f%%", recall));
                    colorMetricLabel(recallLabel, recall);

                    f1ScoreLabel.setText(String.format("F1-Score: %.2f%%", f1Score));
                    colorMetricLabel(f1ScoreLabel, f1Score);

                    statusLabel.setText(String.format("Status: Classification complete.",
                            threshold, accuracy));
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Status: Error during classification: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    // Helper method to color metric labels based on their values
    private void colorMetricLabel(JLabel label, double value) {
        if (value >= 90) {
            label.setForeground(new Color(0, 120, 0)); // Dark green for excellent
        } else if (value >= 80) {
            label.setForeground(new Color(0, 100, 100)); // Teal for good
        } else if (value >= 70) {
            label.setForeground(new Color(100, 100, 0)); // Olive for average
        } else if (value >= 60) {
            label.setForeground(new Color(180, 90, 0)); // Orange for below average
        } else {
            label.setForeground(new Color(180, 0, 0)); // Red for poor
        }
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpamDetectorGUI();
            }
        });
    }
}
