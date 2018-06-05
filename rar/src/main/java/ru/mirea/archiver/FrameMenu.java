package ru.mirea.archiver;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ru.mirea.data.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FrameMenu {
    private JFrame frame = new JFrame("Archiver");
    private JTextArea logArea;
    private JPanel panelMain;
    private JCheckBox jCheckBoxCompression;
    private JRadioButton jRadioButtonPack;
    private JRadioButton jRadioButtonUnpack;
    private JButton buttonAccept;
    private JCheckBox jCheckBoxCrypt;
    private JPasswordField passwordEnter;
    private JPasswordField passwordRepeat;
    private JCheckBox jCheckBoxToServer;
    private JCheckBox jCheckBoxDeletePack;
    private JCheckBox jCheckBoxDeleteUnpack;
    private JScrollPane jScrollPane;
    private JCheckBox jCheckBoxFromServer;
    private JTextField textFiledFileName;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem inputItem = new JMenuItem("Input files");
    private JMenuItem outputItem = new JMenuItem("Output file");

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int sizeWidth = 550;
    private int sizeHeight = 550;
    private int locationX = (screenSize.width - sizeWidth) / 2;
    private int locationY = (screenSize.height - sizeHeight) / 2;

    private static JFileChooser jFileChooserInput;
    private static JFileChooser jFileChooserOutput;
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter("Rar Files", "afk");

    private static File[] inputFiles;
    private static File outputFile;
    private boolean[] flags = new boolean[7];

    private BlockingQueue<File> qOut;
    private BlockingQueue<File> qIn;
    private BlockingQueue<File> qFile;

    public FrameMenu(BlockingQueue<File> qOut, BlockingQueue<File> qIn, BlockingQueue<File> qFile) throws IOException {
        this.qOut = qOut;
        this.qIn = qIn;
        this.qFile = qFile;
        jFileChooserInput = new JFileChooser(new File(".").getCanonicalPath());
        jFileChooserOutput = new JFileChooser(new File(".").getCanonicalPath());

        jFileChooserInput.setMultiSelectionEnabled(true);
        passwordEnter.setEnabled(false);
        passwordRepeat.setEnabled(false);
        jCheckBoxDeleteUnpack.setEnabled(false);
        jCheckBoxFromServer.setEnabled(false);
        textFiledFileName.setEnabled(false);

        jRadioButtonUnpack.addActionListener(e -> {
            jCheckBoxCompression.setEnabled(false);
            jCheckBoxDeleteUnpack.setEnabled(true);
            jCheckBoxDeletePack.setEnabled(false);
            jFileChooserOutput.setSelectedFile(new File(""));
            jFileChooserInput.setSelectedFiles(new File[]{new File("")});
            jFileChooserOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jFileChooserInput.addChoosableFileFilter(filter);
            jFileChooserInput.setFileFilter(filter);
            jCheckBoxToServer.setEnabled(false);
            jCheckBoxFromServer.setEnabled(true);
            textFiledFileName.setEnabled(true);
        });

        jRadioButtonPack.addActionListener(e -> {
            jCheckBoxCompression.setEnabled(true);
            jCheckBoxDeleteUnpack.setEnabled(false);
            jCheckBoxDeletePack.setEnabled(true);
            jFileChooserOutput.setSelectedFile(new File(""));
            jFileChooserInput.setSelectedFiles(new File[]{new File("")});
            jFileChooserOutput.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooserInput.removeChoosableFileFilter(filter);
            jCheckBoxToServer.setEnabled(true);
            jCheckBoxFromServer.setEnabled(false);
            textFiledFileName.setEnabled(false);
        });

        inputItem.addActionListener(e -> {
            if (jFileChooserInput.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                inputFiles = jFileChooserInput.getSelectedFiles();
            }
        });

        outputItem.addActionListener(e -> {
            if (jFileChooserOutput.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                outputFile = jFileChooserOutput.getSelectedFile();
            }
        });

        jCheckBoxCrypt.addActionListener(e -> {
            if (!passwordRepeat.isEnabled()) {
                passwordEnter.setEnabled(true);
                passwordRepeat.setEnabled(true);
            } else {
                passwordEnter.setEnabled(false);
                passwordRepeat.setEnabled(false);
            }
        });

        buttonAccept.addActionListener(e -> {
            inputFiles = jFileChooserInput.getSelectedFiles();
            if (inputFiles == null || inputFiles.length == 0) {
                printDate();
                logArea.append("Error: no input files selected\n");
                return;
            }
            if (inputFiles.length == 1) {
                if (inputFiles[0].getName().equals("")) {
                    printDate();
                    logArea.append("Error: no input files selected\n");
                    return;
                }
            }

            for (File item : inputFiles) {
                if (!item.exists()) {
                    printDate();
                    logArea.append("File not found: " + item.getName() + "\n");
                    return;
                }
                if ((item.equals(outputFile) || (new File(outputFile.getAbsolutePath() + ".afk").equals(item))) && !jCheckBoxFromServer.isSelected()) {
                    printDate();
                    logArea.append("Input file: " + item.getAbsolutePath() + " equals output file\n");
                    return;
                }
            }
            outputFile = jFileChooserOutput.getSelectedFile();
            if (outputFile == null || outputFile.getName().equals("")) {
                printDate();
                logArea.append("Error: no output file selected\n");
                return;
            }
            flags[0] = jRadioButtonPack.isSelected();
            flags[1] = jCheckBoxCompression.isSelected();
            flags[2] = jCheckBoxCrypt.isSelected();
            flags[3] = jCheckBoxToServer.isSelected();
            flags[4] = jCheckBoxDeletePack.isSelected();
            flags[5] = jCheckBoxDeleteUnpack.isSelected();
            flags[6] = jCheckBoxFromServer.isSelected();

            if (flags[6]) {
                if (textFiledFileName.getText().length() == 0) {
                    printDate();
                    logArea.append("Error: filename not entered\n");
                    return;
                }
            }
            if (flags[2]) {
                String password = new String(passwordEnter.getPassword());
                String rPassword = new String(passwordRepeat.getPassword());
                if (password.length() == 0) {
                    printDate();
                    logArea.append("Error: password not entered\n");
                    return;
                }
                if (!password.equals(rPassword)) {
                    printDate();
                    logArea.append("Error: repeated password is not equal to the entered password\n");
                    return;
                }
                for (int i = 0; i < password.length(); i++) {
                    char tmpChar = password.charAt(i);
                    if (tmpChar < 33 || tmpChar > 125) {
                        printError(-14);
                        return;
                    }
                }
            }
            logArea.append("Start: \n");
            int result = 1;
            try {
                result = executeOperation(inputFiles, outputFile, new String(passwordEnter.getPassword()), flags);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            if (result == 0)
                logArea.append("Complete!\n");
            if (result == -1)
                logArea.append("Failure\n");
            if (result == 1)
                logArea.append("Unknown error\n");
        });

        jCheckBoxToServer.addActionListener(e -> {
            if (jCheckBoxToServer.isSelected()) {
                jFileChooserInput.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jCheckBoxFromServer.setSelected(false);
            }
        });

        jCheckBoxFromServer.addActionListener(e -> {
            if (jCheckBoxFromServer.isSelected()) {
                jFileChooserInput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jCheckBoxToServer.setSelected(false);
            } else {
                jFileChooserInput.setFileSelectionMode(JFileChooser.FILES_ONLY);
            }
        });
    }

    public void openFrame() {
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(locationX, locationY, sizeWidth, sizeHeight);

        menuBar.add(fileMenu);
        fileMenu.add(inputItem);
        fileMenu.add(outputItem);
        frame.setJMenuBar(menuBar);

        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void printDate() {
        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss");
        logArea.append(formatForDateNow.format(date) + ": ");
    }

    private int executeOperation(File[] inputFiles, File outputFile, String password, boolean[] flags) throws Exception {
        int error;
        if (flags[0]) {
            File file = new File(outputFile.getAbsolutePath() + ".afk");
            if (file.exists())
                if (!file.delete())
                    return -1;
            for (File item : inputFiles) {
                Packer packer = new PackerImpl();
                printDate();
                logArea.append(item.getName() + " is packed...\n");
                updateFrame();
                if ((error = packer.pack(item, outputFile, flags[1])) != 0) {
                    printError(error);
                    return -1;
                }
                printDate();
                logArea.append(item.getName() + " was packed\n");
                updateFrame();
                System.gc();
            }

            if (flags[2]) {
                Encryptor encryptor = new EncryptorImpl();
                printDate();
                logArea.append(outputFile.getName() + ".afk is encrypted...\n");
                updateFrame();
                if ((error = encryptor.encryption(password, outputFile)) != 0) {
                    printError(error);
                    return -1;
                }
                printDate();
                logArea.append(outputFile.getName() + ".afk was encrypted\n");
                updateFrame();
                System.gc();
            }
            if (flags[4]) {
                for (File item : inputFiles) {
                    if (!item.delete()) {
                        printError(-11);
                        return -1;
                    } else {
                        printDate();
                        logArea.append(item.getName() + " was deleted\n");
                        updateFrame();
                    }
                }
            }
            if (flags[3]) {
                qOut.put(new File(outputFile.getAbsolutePath() + ".afk"));
            }
        } else {
            if (flags[6]) {
                qIn.put(new File(textFiledFileName.getText()));
                qFile.put(inputFiles[0]);
                while (qFile.size() != 0)
                    Thread.sleep(1);
                File file = new File(qFile.take().getAbsolutePath());
                if (file.getName().equals(":error")) {
                    printError(-15);
                    return -1;
                }
                inputFiles[0] = file;
            }
            if (flags[2]) {
                for (File item : inputFiles) {
                    Decryptor decryptor = new DecryptorImpl();
                    Unpacker unpacker = new UnpackerImpl();
                    printDate();
                    logArea.append(item.getName() + " is decrypted...\n");
                    updateFrame();
                    if ((error = decryptor.decryption(password, item)) != 0) {
                        printError(error);
                        return -1;
                    }
                    printDate();
                    logArea.append(item.getName() + " was decrypted\n");
                    updateFrame();
                    System.gc();

                    printDate();
                    logArea.append(item.getName() + " is unpacked...\n");
                    updateFrame();
                    if ((error = unpacker.unpack(new File(item.getAbsolutePath() + "dec"), outputFile)) != 0) {
                        printError(error);
                        return -1;
                    }
                    printDate();
                    logArea.append(item.getName() + " was unpacked\n");
                    updateFrame();
                    System.gc();
                }
            } else {
                for (File item : inputFiles) {
                    Unpacker unpacker = new UnpackerImpl();
                    printDate();
                    logArea.append(item.getName() + " is unpacked...\n");
                    updateFrame();
                    if ((error = unpacker.unpack(item, outputFile)) != 0) {
                        printError(error);
                        return -1;
                    }
                    printDate();
                    logArea.append(item.getName() + " was unpacked\n");
                    updateFrame();
                    System.gc();
                }
            }
            if (flags[5]) {
                for (File item : inputFiles) {
                    if (!item.delete()) {
                        printError(-12);
                        return -1;
                    } else {
                        printDate();
                        logArea.append(item.getName() + " was deleted\n");
                        updateFrame();
                    }
                }
            }
        }
        return 0;
    }

    private void updateFrame() {
        jScrollPane.setViewportView(logArea);
        JScrollBar vBar = jScrollPane.getVerticalScrollBar();
        vBar.setValue(vBar.getMaximum());
        logArea.scrollRectToVisible(logArea.getVisibleRect());
        logArea.paint(logArea.getGraphics());
    }

    private void printError(int error) {
        printDate();
        switch (error) {
            case -1:
                logArea.append("Error: root is broken\n");
                break;
            case -2:
                logArea.append("Error: sequence is broken\n");
                break;
            case -3:
                logArea.append("Error: incorrect special byte\n");
                break;
            case -4:
                logArea.append("Error: checksum does not match\n");
                break;
            case -5:
                logArea.append("Error: duplicate character is missing\n");
                break;
            case -6:
                logArea.append("Error: metadata is broken\n");
                break;
            case -7:
                logArea.append("Error: sequence length does not match the metadata\n");
                break;
            case -8:
                logArea.append("Error: can not delete the decrypted file\n");
                break;
            case -9:
                logArea.append("Error: can not delete the encrypted file\n");
                break;
            case -10:
                logArea.append("Error: can not rename the encrypted file\n");
                break;
            case -11:
                logArea.append("Error: can not delete the input file\n");
                break;
            case -12:
                logArea.append("Error: can not delete the input archive\n");
                break;
            case -13:
                logArea.append("Error: path or filename are incorrect\n");
                break;
            case -14:
                logArea.append("Error: password is incorrect\n");
                break;
            case -15:
                logArea.append("Error: file not found in server\n");
                break;
            default:
                logArea.append("Error: unknown error\n");
        }
    }

    public static void main(String[] args) throws Exception {
        BlockingQueue<File> qOut = new LinkedBlockingQueue<>();
        BlockingQueue<File> qIn = new LinkedBlockingQueue<>();
        BlockingQueue<File> qFile = new LinkedBlockingQueue<>();
        FrameMenu frameMenu = new FrameMenu(qOut, qIn, qFile);
        frameMenu.openFrame();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(16, 3, new Insets(10, 20, 0, 20), -1, -1));
        panelMain.setAutoscrolls(true);
        panelMain.setFocusable(true);
        panelMain.setInheritsPopupMenu(false);
        panelMain.setMaximumSize(new Dimension(600, 300));
        panelMain.setMinimumSize(new Dimension(600, 300));
        panelMain.setRequestFocusEnabled(true);
        panelMain.setVerifyInputWhenFocusTarget(true);
        panelMain.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-4504510)), null));
        jScrollPane = new JScrollPane();
        jScrollPane.setAutoscrolls(true);
        panelMain.add(jScrollPane, new GridConstraints(1, 0, 15, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(300, 433), new Dimension(330, 433), new Dimension(300, 433), 0, false));
        logArea = new JTextArea();
        logArea.setColumns(2);
        logArea.setEditable(false);
        logArea.setEnabled(true);
        logArea.setFocusable(true);
        logArea.setLineWrap(true);
        logArea.setMaximumSize(new Dimension(300, 550));
        logArea.setText("");
        logArea.setWrapStyleWord(true);
        jScrollPane.setViewportView(logArea);
        buttonAccept = new JButton();
        buttonAccept.setActionCommand("Accept");
        buttonAccept.setLabel("Accept");
        buttonAccept.setText("Accept");
        panelMain.add(buttonAccept, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setAlignmentX(0.0f);
        label1.setAlignmentY(0.0f);
        label1.setFocusable(false);
        label1.setText("Log of work");
        panelMain.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 16), null, 0, false));
        jRadioButtonPack = new JRadioButton();
        jRadioButtonPack.setSelected(true);
        jRadioButtonPack.setText("Pack");
        panelMain.add(jRadioButtonPack, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jRadioButtonUnpack = new JRadioButton();
        jRadioButtonUnpack.setLabel("Unpack");
        jRadioButtonUnpack.setText("Unpack");
        panelMain.add(jRadioButtonUnpack, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 20), null, 0, false));
        jCheckBoxCrypt = new JCheckBox();
        jCheckBoxCrypt.setText("Crypt");
        panelMain.add(jCheckBoxCrypt, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordEnter = new JPasswordField();
        panelMain.add(passwordEnter, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jCheckBoxCompression = new JCheckBox();
        jCheckBoxCompression.setActionCommand("Check");
        jCheckBoxCompression.setLabel("Compression");
        jCheckBoxCompression.setText("Compression");
        panelMain.add(jCheckBoxCompression, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordRepeat = new JPasswordField();
        panelMain.add(passwordRepeat, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Enter password");
        panelMain.add(label2, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Repeat password");
        panelMain.add(label3, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxDeletePack = new JCheckBox();
        jCheckBoxDeletePack.setText("Delete files after packing");
        panelMain.add(jCheckBoxDeletePack, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxToServer = new JCheckBox();
        jCheckBoxToServer.setText("To server");
        panelMain.add(jCheckBoxToServer, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxDeleteUnpack = new JCheckBox();
        jCheckBoxDeleteUnpack.setText("Delete archive after unpacking");
        panelMain.add(jCheckBoxDeleteUnpack, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxFromServer = new JCheckBox();
        jCheckBoxFromServer.setText("From server");
        panelMain.add(jCheckBoxFromServer, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFiledFileName = new JTextField();
        panelMain.add(textFiledFileName, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Enter filename");
        panelMain.add(label4, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(jRadioButtonPack);
        buttonGroup.add(jRadioButtonUnpack);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
