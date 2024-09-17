package graphics;

import app.DiningPhilosophersApp;
import models.Philosopher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class MonitorPanel
{
    private int n;
    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 900;
    private final JPanel controlPanel;
    private final JTextArea logArea;
    private final JTextArea statusArea;
    private final JButton addButton;
    private final JButton remButton;

    private final DiningPhilosophersApp context;
    private String[] statusStrings;
    private int[] statusTotals;

    public MonitorPanel(DiningPhilosophersApp context, int n)
    {
        this.context = context;
        this.n = n;

        statusStrings = new String[n];
        statusTotals = new int[3];

        for (int i = 0; i < n; ++i) statusStrings[i] = "thinking";
        statusTotals[0] = 0;
        statusTotals[1] = 0;
        statusTotals[2] = n;

        // Control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(FRAME_WIDTH-800, FRAME_HEIGHT));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5));
        addButton = new JButton("Add");
        if (n == 10)
            addButton.setEnabled(false);
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                handleAddPhilosopher();
            }
        });

        remButton = new JButton("Del");
        if (n == 5)
            remButton.setEnabled(false);
        remButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    handleRemPhilosopher();
                } catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        });

        buttonPanel.add(remButton);
        buttonPanel.add(addButton);

        // Large Text Area for Status
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setFont(statusArea.getFont().deriveFont(18f));
        JScrollPane scrollPane1 = new JScrollPane(statusArea);

        // Large Text Area for Logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(logArea.getFont().deriveFont(12f));
        JScrollPane scrollPane2 = new JScrollPane(logArea);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(2, 1, 5, 5));
        textPanel.add(scrollPane1);
        textPanel.add(scrollPane2);

        // Add sub-panels to control panel
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(textPanel, BorderLayout.CENTER);
    }

    public JPanel getPanel() { return controlPanel; }
    public void logMonitor(String msg) { logArea.append(msg); }

    public synchronized void updateStatusPanelWithoutChange(Philosopher p)
    {
        switch (p.getState())
        {
            case 0: { statusStrings[p.getId()] = "thinking"; break; }
            case 1: { statusStrings[p.getId()] = "hungry"; break; }
            case 2: { statusStrings[p.getId()] = "eating"; break; }
        }

        String newStatus = "";
        for (int i = 0; i < n; ++i)
        {
            newStatus = newStatus.concat("Philosopher " + i + " is " + statusStrings[i] + "\n");
        }

        newStatus = newStatus.concat("\nTotal Philosophers Thinking: " + statusTotals[0]);
        newStatus = newStatus.concat("\nTotal Philosophers Hungry: " + statusTotals[1]);
        newStatus = newStatus.concat("\nTotal Philosophers Eating: " + statusTotals[2]);

        statusArea.setText(newStatus);
    }

    public synchronized void updateStatusPanel(Philosopher p)
    {
        try {
            if (p.getId() > n) return;
            switch (p.getState()) {
                case 0: {
                    statusStrings[p.getId()] = "thinking";
                    statusTotals[0]++;
                    statusTotals[2]--;
                    break;
                }
                case 1: {
                    statusStrings[p.getId()] = "hungry";
                    statusTotals[1]++;
                    statusTotals[0]--;
                    break;
                }
                case 2: {
                    statusStrings[p.getId()] = "eating";
                    statusTotals[2]++;
                    statusTotals[1]--;
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("Non critical exception occurred. Continuing execution.");
        }

        String newStatus = "";
        for (int i = 0; i < n; ++i)
        {
            newStatus = newStatus.concat("Philosopher " + i + " is " + statusStrings[i] + "\n");
        }

        newStatus = newStatus.concat("\nTotal Philosophers Thinking: " + statusTotals[0]);
        newStatus = newStatus.concat("\nTotal Philosophers Hungry: " + statusTotals[1]);
        newStatus = newStatus.concat("\nTotal Philosophers Eating: " + statusTotals[2]);

        statusArea.setText(newStatus);
    }

    private void handleAddPhilosopher(){ context.addPhilosopher(); }
    private void handleRemPhilosopher() throws InterruptedException { context.remPhilosopher(); }

    public synchronized void setN(int n)
    {
        if (n > this.n) statusTotals[2]++; // New philosopher enters thinking, but entering thinking adds 1 on those and removes 1 from eating
        this.n = n;
        remButton.setEnabled(n != 5);
        addButton.setEnabled(n != 10);
        statusStrings = Arrays.copyOf(statusStrings, n);
    }

    public void remPhilosopherFromStatus (int status)
    {
        statusTotals[status]--;
    }
}
