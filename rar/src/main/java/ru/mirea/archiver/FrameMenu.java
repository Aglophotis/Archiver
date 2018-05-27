package ru.mirea.archiver;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import ru.mirea.data.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private JCheckBox jCheckBoxCloud;
    private JCheckBox jCheckBoxDeletePack;
    private JCheckBox jCheckBoxDeleteUnpack;
    private JScrollPane jScrollPane;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("File");
    private JMenuItem inputItem = new JMenuItem("Input files");
    private JMenuItem outputItem = new JMenuItem("Output file");

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int sizeWidth = 550;
    private int sizeHeight = 500;
    private int locationX = (screenSize.width - sizeWidth) / 2;
    private int locationY = (screenSize.height - sizeHeight) / 2;

    private static JFileChooser jFileChooserInput;
    private static JFileChooser jFileChooserOutput;
    private static FileNameExtensionFilter filter = new FileNameExtensionFilter("Rar Files", "afk");

    private static File[] inputFiles;
    private static File outputFile;
    private boolean[] flags = new boolean[6];

    private FrameMenu() throws IOException {
        jFileChooserInput = new JFileChooser(new File(".").getCanonicalPath());
        jFileChooserOutput = new JFileChooser(new File(".").getCanonicalPath());

        jFileChooserInput.setMultiSelectionEnabled(true);
        passwordEnter.setEnabled(false);
        passwordRepeat.setEnabled(false);
        jCheckBoxDeleteUnpack.setEnabled(false);

        jRadioButtonUnpack.addActionListener(e -> {
            jCheckBoxCompression.setEnabled(false);
            jCheckBoxDeleteUnpack.setEnabled(true);
            jCheckBoxDeletePack.setEnabled(false);
            //inputItem.setText("Input archive");
            //outputItem.setText("Output directory");
            jFileChooserOutput.setSelectedFile(new File(""));
            jFileChooserInput.setSelectedFiles(new File[]{new File("")});
            jFileChooserOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jFileChooserInput.addChoosableFileFilter(filter);
            jFileChooserInput.setFileFilter(filter);
        });

        jRadioButtonPack.addActionListener(e -> {
            jCheckBoxCompression.setEnabled(true);
            jCheckBoxDeleteUnpack.setEnabled(false);
            jCheckBoxDeletePack.setEnabled(true);
            //inputItem.setText("Input files");
            //outputItem.setText("Output file");
            jFileChooserOutput.setSelectedFile(new File(""));
            jFileChooserInput.setSelectedFiles(new File[]{new File("")});
            jFileChooserOutput.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooserInput.removeChoosableFileFilter(filter);
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
            for (File file : inputFiles) {
                if (!file.exists()) {
                    printDate();
                    logArea.append("File not found: " + file.getName() + "\n");
                    return;
                }
            }
            outputFile = jFileChooserOutput.getSelectedFile();
            if (outputFile == null) {
                printDate();
                logArea.append("Error: no output file selected\n");
                return;
            }
            flags[0] = jRadioButtonPack.isSelected();
            flags[1] = jCheckBoxCompression.isSelected();
            flags[2] = jCheckBoxCrypt.isSelected();
            flags[3] = jCheckBoxCloud.isSelected();
            flags[4] = jCheckBoxDeletePack.isSelected();
            flags[5] = jCheckBoxDeleteUnpack.isSelected();
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
    }

    private void openFrame() throws IOException {
        frame.setContentPane(new FrameMenu().panelMain);
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
                packer = null;
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
                encryptor = null;
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
        } else {
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
                    decryptor = null;
                    printDate();
                    logArea.append(item.getName() + " was decrypted\n");
                    updateFrame();
                    System.gc();

                    printDate();
                    logArea.append(item.getName() + " is unpacked...\n");
                    updateFrame();
                    if ((error = unpacker.unpack(new File(item.getAbsolutePath() + "dec"), outputFile)) != 0) {
                        printError(error);
                        if (item.getName().endsWith(".afkdec"))
                            if (!item.delete())
                                printError(-8);
                        return -1;
                    }
                    printDate();
                    logArea.append(item.getName() + " was unpacked\n");
                    updateFrame();
                    unpacker = null;
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
                    unpacker = null;
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
                logArea.append("Error: directory is not found\n");
                break;
            default:
                logArea.append("Error: unknown error\n");
        }
    }

    public static void main(String[] args) throws Exception {
        FrameMenu frameMenu = new FrameMenu();
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
        panelMain.setLayout(new GridLayoutManager(13, 3, new Insets(10, 20, 0, 20), -1, -1));
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
        panelMain.add(jScrollPane, new GridConstraints(1, 0, 12, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(300, 383), new Dimension(330, 222), new Dimension(300, 382), 0, false));
        logArea = new JTextArea();
        logArea.setColumns(2);
        logArea.setEditable(false);
        logArea.setEnabled(true);
        logArea.setFocusable(true);
        logArea.setLineWrap(true);
        logArea.setMaximumSize(new Dimension(300, 500));
        logArea.setText("");
        logArea.setWrapStyleWord(true);
        jScrollPane.setViewportView(logArea);
        buttonAccept = new JButton();
        buttonAccept.setActionCommand("Accept");
        buttonAccept.setLabel("Accept");
        buttonAccept.setText("Accept");
        panelMain.add(buttonAccept, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panelMain.add(jCheckBoxCrypt, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordEnter = new JPasswordField();
        panelMain.add(passwordEnter, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jCheckBoxCompression = new JCheckBox();
        jCheckBoxCompression.setActionCommand("Check");
        jCheckBoxCompression.setLabel("Compression");
        jCheckBoxCompression.setText("Compression");
        panelMain.add(jCheckBoxCompression, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordRepeat = new JPasswordField();
        panelMain.add(passwordRepeat, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Enter password");
        panelMain.add(label2, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Repeat password");
        panelMain.add(label3, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxDeletePack = new JCheckBox();
        jCheckBoxDeletePack.setText("Delete files after packing");
        panelMain.add(jCheckBoxDeletePack, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxCloud = new JCheckBox();
        jCheckBoxCloud.setText("Cloud");
        panelMain.add(jCheckBoxCloud, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jCheckBoxDeleteUnpack = new JCheckBox();
        jCheckBoxDeleteUnpack.setText("Delete archive after unpacking");
        panelMain.add(jCheckBoxDeleteUnpack, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
