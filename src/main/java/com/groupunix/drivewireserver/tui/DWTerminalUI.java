package com.groupunix.drivewireserver.tui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DWTerminalUI {
    private Screen screen;
    private MultiWindowTextGUI gui;
    private Label statusLabel;
    private TextBox logBox;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private BasicWindow window;

    public void start() {
        Thread tuiThread = new Thread(() -> {
            try {
                DefaultTerminalFactory factory = new DefaultTerminalFactory();
                Terminal terminal = factory.createTerminal();
                screen = new TerminalScreen(terminal);
                screen.startScreen();

                gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));

                Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

                // Status Panel
                Panel statusPanel = new Panel(new LinearLayout(Direction.VERTICAL));
                statusPanel.addComponent(new Label("DriveWire Server - Live Monitor").addStyle(SGR.BOLD));
                statusLabel = new Label("Initializing...").addStyle(SGR.BOLD);
                statusPanel.addComponent(statusLabel);
                mainPanel.addComponent(statusPanel.withBorder(Borders.singleLine("Status/Metrics"))
                        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));

                // Log Panel
                logBox = new TextBox(new TerminalSize(80, 10));
                logBox.setReadOnly(true);
                mainPanel.addComponent(logBox.withBorder(Borders.singleLine("Server Logs"))
                        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill)));

                window = new BasicWindow();
                window.setComponent(mainPanel);
                window.setHints(java.util.Arrays.asList(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

                gui.addWindow(window);

                while (running.get()) {
                    synchronized (this) {
                        gui.updateScreen();
                    }

                    KeyStroke key = screen.pollInput();
                    if (key != null && (key.getKeyType() == KeyType.EOF ||
                            (key.getKeyType() == KeyType.Character
                                    && (key.getCharacter() == 'q' || key.getCharacter() == 'Q')))) {
                        com.groupunix.drivewireserver.DriveWireServer.shutdown();
                        running.set(false);
                    }

                    Thread.sleep(100);
                }

                screen.stopScreen();
            } catch (Exception e) {
                System.err.println("TUI Failed: " + e.getMessage());
                e.printStackTrace();
            }
        }, "DW-TUI-Thread");
        tuiThread.setDaemon(true);
        tuiThread.start();
    }

    public synchronized void updateLogLines(List<String> lines) {
        if (logBox != null && lines != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            logBox.setText(sb.toString());
            logBox.setCaretPosition(lines.size(), 0);
        }
    }

    public synchronized void updateStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    public void stop() {
        running.set(false);
    }
}
