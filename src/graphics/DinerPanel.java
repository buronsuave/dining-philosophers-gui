package graphics;

import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import app.DiningPhilosophersApp;

public class DinerPanel
{
    private static final int PHILOSOPHER_LABEL_WIDTH = 100;
    private static final int PHILOSOPHER_LABEL_HEIGHT = 120;
    private static final int CHOPSTICK_LABEL_WIDTH = 50;
    private static final int CHOPSTICK_LABEL_HEIGHT = 70;
    private static final int DINER_PANEL_WIDTH = 800;
    private static final int DINER_PANEL_HEIGHT = 800;
    private static final int TABLE_RADIUS = 300;
    private static final int TABLE_IN_RADIUS = 250;
    private static final int TABLE_X_MARGIN = 100;
    private static final int TABLE_Y_MARGIN = 100;
    private static final int CHOPSTICK_ANIMATION_TIME = 1000;
    private static final int CHOPSTICK_ANIMATION_DELAY = 20;
    private static final int CHOPSTICK_ANIMATION_STEPS = CHOPSTICK_ANIMATION_TIME / CHOPSTICK_ANIMATION_DELAY;

    private static final String[] PHILOSOPHER_LABEL_RES = {
        "./res/thinking.jpeg", // URLs to images for each philosopher state
        "./res/hungry.jpeg",
        "./res/eating.jpeg"
    };

    // URL to chopstick image
    private static final String[] CHOPSTICK_LABEL_RES = {
            "./res/chopstick_a.jpeg",
            "./res/chopstick_b.jpeg"
    };

    private JPanel panel;
    private int n;
    private Chopstick[] chopsticks;
    private Philosopher[] philosophers;
    private JLabel[] chopstickLabels;
    private JLabel[] philosopherLabels;
    private Timer[] animationTimers;
    private int[] xChopstickRoots;
    private int[] yChopstickRoots;
    private Thread[] threads;

    private final DiningPhilosophersApp context;

    public DinerPanel(DiningPhilosophersApp context, int n)
    {
        this.n = n;
        chopsticks = new Chopstick[n];
        philosophers = new Philosopher[n];
        chopstickLabels = new JLabel[n];
        philosopherLabels = new JLabel[n];
        animationTimers = new Timer[n];
        xChopstickRoots = new int[n];
        yChopstickRoots = new int[n];
        threads = new Thread[n];

        this.context = context;

        initPanel();
    }

    private void initPanel()
    {
        panel = new JPanel()
        {
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                drawTable(g);
            }
        };
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(DINER_PANEL_WIDTH, DINER_PANEL_HEIGHT));

        fillModels();
        fillLabels();
        computePositions();
    }

    private void computePositions()
    {
        // 0. Add philosophers
        double angleStep = (2 * Math.PI) / n;
        for (int i = 0; i < n; ++i)
        {
            double angle = i * angleStep;
            int xPhilosopherLabel =
                    (int) ((double) DINER_PANEL_WIDTH/2 + TABLE_RADIUS*Math.cos(angle))
                            - PHILOSOPHER_LABEL_WIDTH/2;
            int yPhilosopherLabel =
                    (int) ((double) DINER_PANEL_HEIGHT/2 + TABLE_RADIUS*Math.sin(angle))
                            - PHILOSOPHER_LABEL_HEIGHT/2;

            philosopherLabels[i].setLocation(xPhilosopherLabel, yPhilosopherLabel);
            panel.add(philosopherLabels[i]);
        }

        // 1. Build logical sequence for chopsticks
        // 2. Compute positions
        // 3. Save values in root arrays
        // 4. Assign values

        int[] logicalChopstickSequence = new int[n];
        for (int i = 0; i < n; ++i)
        {
            Philosopher p0 = philosophers[(n-1+i)%n];
            Philosopher p1 = philosophers[i];

            // Then ph0 chopstick1 is the one in between
            if (p0.getChopstick1() == p1.getChopstick1() || p0.getChopstick1() == p1.getChopstick2())
                logicalChopstickSequence[i] = p0.getChopstick1().getId();
            else // Otherwise it's chopstick2
                logicalChopstickSequence[i] = p0.getChopstick2().getId();
        }

        for (int i = 0; i < n; ++i)
        {
            double angle = i * angleStep;
            int xPhilosopherLabel =
                    (int) ((double) DINER_PANEL_WIDTH/2 + TABLE_RADIUS*Math.cos(angle))
                            - PHILOSOPHER_LABEL_WIDTH/2;
            int yPhilosopherLabel =
                    (int) ((double) DINER_PANEL_HEIGHT/2 + TABLE_RADIUS*Math.sin(angle))
                            - PHILOSOPHER_LABEL_HEIGHT/2;

            philosopherLabels[i].setLocation(xPhilosopherLabel, yPhilosopherLabel);

            int xChopstickLabel =
                    (int) ((double) DINER_PANEL_WIDTH/2 + TABLE_IN_RADIUS*Math.cos(angle - Math.PI/n))
                            - CHOPSTICK_LABEL_WIDTH/2;
            int yChopstickLabel =
                    (int) ((double) DINER_PANEL_HEIGHT/2 + TABLE_IN_RADIUS*Math.sin(angle - Math.PI/n))
                            - CHOPSTICK_LABEL_HEIGHT/2;

            xChopstickRoots[logicalChopstickSequence[i]] = xChopstickLabel;
            yChopstickRoots[logicalChopstickSequence[i]] = yChopstickLabel;

            // Assign coordinates
            if (chopsticks[logicalChopstickSequence[i]].getOwner() == -1)
                chopstickLabels[logicalChopstickSequence[i]].setLocation(xChopstickLabel, yChopstickLabel);
            else // The chopstick has an owner. Trunk animation.
            {
                // Final chopstick coordinates
                int x1 = philosopherLabels[chopsticks[logicalChopstickSequence[i]].getOwner()].getX();
                int y1 = philosopherLabels[chopsticks[logicalChopstickSequence[i]].getOwner()].getY() + PHILOSOPHER_LABEL_HEIGHT;

                if (philosophers[chopsticks[logicalChopstickSequence[i]].getOwner()].getChopstick1() != chopsticks[logicalChopstickSequence[i]])
                    x1 += CHOPSTICK_LABEL_WIDTH;

                chopstickLabels[logicalChopstickSequence[i]].setLocation(x1, y1);
            }

            // Add to panel
            panel.add(chopstickLabels[logicalChopstickSequence[i]]);
            panel.add(philosopherLabels[i]);
        }
    }

    private void fillModels()
    {
        // Create chopsticks that didn't exist
        for (int i = 0; i < n; ++i)
            if (chopsticks[i] == null)
                chopsticks[i] = new Chopstick(i);

        // Create philosophers that didn't exist
        for (int i = 0; i < n-1; ++i)
            if (philosophers[i] == null)
                philosophers[i] = new Philosopher(
                        this,
                        chopsticks[i],
                        chopsticks[(i + 1) % n],
                        i
                );
        if (philosophers[n-1] == null)
            philosophers[n-1] = new Philosopher(
                    this,
                    chopsticks[0],
                    chopsticks[n-1],
                    n-1
            );
    }

    private void fillLabels()
    {
        // Create chopstick and philosopher labels that didn't exist
        for (int i = 0; i < n; ++i)
        {
            if (chopstickLabels[i] == null)
            {
                chopstickLabels[i] = new JLabel(new ImageIcon(CHOPSTICK_LABEL_RES[0]));
                chopstickLabels[i].setSize(CHOPSTICK_LABEL_WIDTH, CHOPSTICK_LABEL_HEIGHT);
                chopstickLabels[i].setText("CH " + chopsticks[i].getId());
                chopstickLabels[i].setHorizontalTextPosition(JLabel.CENTER);
                chopstickLabels[i].setVerticalTextPosition(JLabel.NORTH);
            }

            if (philosopherLabels[i] == null)
            {
                philosopherLabels[i] = new JLabel(new ImageIcon(PHILOSOPHER_LABEL_RES[0]));
                philosopherLabels[i].setSize(PHILOSOPHER_LABEL_WIDTH, PHILOSOPHER_LABEL_HEIGHT);
                philosopherLabels[i].setText("PH " + philosophers[i].getId());
                philosopherLabels[i].setHorizontalTextPosition(JLabel.CENTER);
                philosopherLabels[i].setVerticalTextPosition(JLabel.NORTH);
            }
        }
    }

    private void drawTable(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillOval(TABLE_X_MARGIN, TABLE_Y_MARGIN, 2*TABLE_RADIUS, 2*TABLE_RADIUS);
    }

    public JPanel getPanel() { return panel; }

    // Methods to interact with models
    public void updatePhilosopherLabel(Philosopher p)
    {
        try
        {
            if (p.getId() > n) return;
            philosopherLabels[p.getId()].setIcon(new ImageIcon(PHILOSOPHER_LABEL_RES[p.getState()]));
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("No critical exception occurred. Continuing execution.");
        }
    }

    // Orientation 0 for left, 1 for right
    // Direction 1 if outwards (pick-up movement), 0 if inwards (put-down movement)
    public void animateMoveChopstick(Philosopher p, Chopstick c, int orientation, int direction)
    {
        chopstickLabels[c.getId()].setIcon(new ImageIcon(CHOPSTICK_LABEL_RES[direction]));
        int x0, y0, x1, y1;
        double xStep, yStep;

        // Outwards
        if (direction == 1)
        {
            // Initial chopstick coordinates
            x0 = chopstickLabels[c.getId()].getX();
            y0 = chopstickLabels[c.getId()].getY();

            // Final chopstick coordinates
            x1 = philosopherLabels[p.getId()].getX() + (orientation * CHOPSTICK_LABEL_WIDTH);
            y1 = philosopherLabels[p.getId()].getY() + PHILOSOPHER_LABEL_HEIGHT;

            xStep = (double) (x1 - x0) / CHOPSTICK_ANIMATION_STEPS;
            yStep = (double) (y1 - y0) / CHOPSTICK_ANIMATION_STEPS;
        }

        else
        {
            // Initial chopstick coordinates
            x0 = chopstickLabels[c.getId()].getX();
            y0 = chopstickLabels[c.getId()].getY();

            xStep = (double) (xChopstickRoots[c.getId()] - x0) / CHOPSTICK_ANIMATION_STEPS;
            yStep = (double) (yChopstickRoots[c.getId()] - y0) / CHOPSTICK_ANIMATION_STEPS;
        }

        animationTimers[c.getId()] = new Timer(CHOPSTICK_ANIMATION_DELAY, new ActionListener()
        {
            int currentStep = 0;

            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (currentStep <= CHOPSTICK_ANIMATION_STEPS)
                {
                    double newX = x0 + currentStep * xStep;
                    double newY = y0 + currentStep * yStep;
                    chopstickLabels[c.getId()].setLocation((int) newX, (int) newY);
                    currentStep++;
                    panel.repaint();
                }
                else
                {
                    panel.repaint();
                    ((Timer) actionEvent.getSource()).stop();
                }
            }
        });

        animationTimers[c.getId()].start();
    }

    public void stopChopstickAnimations()
    {
        for (int i = 0; i < n; ++i)
            if (animationTimers[i] != null)
                animationTimers[i].stop();
    }

    public void startAnimation()
    {
        for (int i = 0; i < n; ++i)
        {
            threads[i] = new Thread(philosophers[i]);
            threads[i].start();
        }

    }

    public void logMonitor(String message)
    {
        context.getMonitor().logMonitor(message+"\n");
    }

    public void updateStatusPanel(Philosopher p)
    {
        context.getMonitor().updateStatusPanel(p);
    }

    public void addPhilosopher()
    {
        chopsticks = Arrays.copyOf(chopsticks, n+1);
        philosophers = Arrays.copyOf(philosophers, n+1);
        chopstickLabels = Arrays.copyOf(chopstickLabels, n+1);
        philosopherLabels = Arrays.copyOf(philosopherLabels, n+1);
        animationTimers = Arrays.copyOf(animationTimers, n+1);
        xChopstickRoots = Arrays.copyOf(xChopstickRoots, n+1);
        yChopstickRoots = Arrays.copyOf(yChopstickRoots, n+1);
        threads = Arrays.copyOf(threads, n+1);

        chopsticks[n] = new Chopstick(n);

        // Check who owns the Ph0 chopstick1
        Chopstick c = philosophers[0].getChopstick1();

        if (c.getOwner() == 0 || c.getOwner() == -1)
        {
            philosophers[n] = new Philosopher(this, c, chopsticks[n], n);
            philosophers[n-1].setChopstick1(chopsticks[n]);
        }
        else
        {
            philosophers[n] = new Philosopher(this, chopsticks[n], c, n);
            philosophers[0].setChopstick1(chopsticks[n]);
        }

        threads[n] = new Thread(philosophers[n]);
        threads[n].start();
    }

    public void remPhilosopherFromStatus(int status)
    {
        context.remPhilosopherFromStatus(status);
    }

    public void broadcastStatus()
    {
        for (int i = 0; i < n; ++i)
            context.getMonitor().updateStatusPanelWithoutChange(philosophers[i]);
    }

    public void remPhilosopher() throws InterruptedException {
        // Stop execution
        threads[n-1].interrupt();

        // Last chopstick is either the primary chopstick of ph(0) or
        // secondary chopstick of ph(n-1)
        if (philosophers[0].getChopstick1() == chopsticks[n-1])
        {
            philosophers[0].setChopstick1(philosophers[n-2].getChopstick1());
        }
        else
        {
            philosophers[n-2].setChopstick1(philosophers[0].getChopstick1());
        }

        // Remove graphics
        panel.remove(chopstickLabels[n-1]);
        panel.remove(philosopherLabels[n-1]);

        // Remove from total count of status
        remPhilosopherFromStatus(philosophers[n-1].getState());

        // Shrink arrays
        chopsticks = Arrays.copyOf(chopsticks, n-1);
        philosophers = Arrays.copyOf(philosophers, n-1);
        chopstickLabels = Arrays.copyOf(chopstickLabels, n-1);
        philosopherLabels = Arrays.copyOf(philosopherLabels, n-1);

        if (animationTimers[n-1] != null)
            animationTimers[n-1].stop();
        animationTimers = Arrays.copyOf(animationTimers, n-1);

        xChopstickRoots = Arrays.copyOf(xChopstickRoots, n-1);
        yChopstickRoots = Arrays.copyOf(yChopstickRoots, n-1);
        threads = Arrays.copyOf(threads, n-1);
    }

    public void setN(int n)
    {
        this.n = n;
        stopChopstickAnimations();
        fillLabels();
        computePositions();
        broadcastStatus();
    }
}
