package app;

import graphics.*;
import javax.swing.*;
import java.awt.*;

public class DiningPhilosophersApp extends JFrame
{
    private static final int INITIAL_N = 5;
    private final DinerPanel dp;
    private final MonitorPanel mp;

    private int n;
    private static final int FRAME_WIDTH = 1300;
    private static final int FRAME_HEIGHT = 900;

    public MonitorPanel getMonitor() { return mp; }

    public DiningPhilosophersApp(int n)
    {
        this.n = n;

        setTitle("Dining Philosophers App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLayout(new BorderLayout());

        dp = new DinerPanel(this, n);
        add(dp.getPanel(), BorderLayout.CENTER);

        mp = new MonitorPanel(this, n);
        add(mp.getPanel(), BorderLayout.EAST);

        dp.startAnimation();
    }

    public void addPhilosopher()
    {
        if (n == 10) return;
        dp.addPhilosopher();
        n++;
        mp.setN(n);
        dp.setN(n);
    }

    public void remPhilosopher() throws InterruptedException {
        if (n == 5) return;
        dp.remPhilosopher();
        n--;
        mp.setN(n);
        dp.setN(n);
    }

    public void remPhilosopherFromStatus(int status)
    {
        mp.remPhilosopherFromStatus(status);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            new DiningPhilosophersApp(INITIAL_N).setVisible(true);
        });
    }
}
