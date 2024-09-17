package models;

import graphics.DinerPanel;

public class Philosopher implements Runnable
{
    private Chopstick chopstick1;
    private Chopstick chopstick2;
    private int state;
    private final int id;
    private final DinerPanel dp;

    private static final int CHOPSTICK_ANIMATION_TIME = 1000;

    public Philosopher(DinerPanel dp, Chopstick chopstick1, Chopstick chopstick2, int philosopherId)
    {
        this.dp = dp;
        this.chopstick1 = chopstick1;
        this.chopstick2 = chopstick2;
        id = philosopherId;
        state = 0; // Created
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(50);
                dp.logMonitor("Philosopher " + id + " just joined");

                think();

                while(!chopstick1.tryPickUp(id)) Thread.sleep((int) (Math.random() * 50));
                pickUpChopstick1();

                while(!chopstick2.tryPickUp(id)) Thread.sleep((int) (Math.random() * 50));
                pickUpChopstick2();

                eat();

                putDownChopstick2();
                chopstick2.putDown();

                putDownChopstick1();
                chopstick1.putDown();
            }
            catch (InterruptedException e)
            {
                // If interrupted, assume that it's the exit signal
                dp.logMonitor("Philosopher " + id + " just left.");
                if (chopstick1.getOwner() == id) chopstick1.putDown();
                if (chopstick2.getOwner() == id) chopstick2.putDown();
                return;
            }
        }
    }

    private void think() throws InterruptedException
    {
        state = 0; // Thinking
        updatePhilosopherLabel();
        dp.logMonitor("Philosopher " + id + " started thinking");
        dp.updateStatusPanel(this);
        Thread.sleep(3000 + (int)(Math.random() * 2000));

        // After thinking, get hungry
        state = 1;
        dp.logMonitor("Philosopher " + id + " is hungry");
        dp.updateStatusPanel(this);
        updatePhilosopherLabel();
    }

    private void eat() throws InterruptedException
    {
        state = 2; // Eating
        updatePhilosopherLabel();
        dp.logMonitor("Philosopher " + id + " started eating");
        dp.updateStatusPanel(this);
        Thread.sleep(3000 + (int)(Math.random() * 2000));
    }

    private void pickUpChopstick1() throws InterruptedException
    {
        animateMoveChopstick(chopstick1, 0, 1);
        dp.logMonitor("Philosopher " + id + " got (first) chopstick " + chopstick1.getId());
        Thread.sleep(CHOPSTICK_ANIMATION_TIME);
    }

    private void pickUpChopstick2() throws InterruptedException
    {
        animateMoveChopstick(chopstick2, 1, 1);
        dp.logMonitor("Philosopher " + id + " got (second) chopstick " + chopstick2.getId());
        Thread.sleep(CHOPSTICK_ANIMATION_TIME);
    }

    private void putDownChopstick1() throws InterruptedException
    {
        animateMoveChopstick(chopstick1, 0, 0);
        dp.logMonitor("Philosopher " + id + " left (first) chopstick " + chopstick1.getId());
        Thread.sleep(CHOPSTICK_ANIMATION_TIME);
    }

    private void putDownChopstick2() throws InterruptedException
    {
        animateMoveChopstick(chopstick2, 1, 0);
        dp.logMonitor("Philosopher " + id + " left (second) chopstick " + chopstick2.getId());
        Thread.sleep(CHOPSTICK_ANIMATION_TIME);
    }

    private void updatePhilosopherLabel()
    {
        dp.updatePhilosopherLabel(this);
    }

    private void animateMoveChopstick(Chopstick chopstick, int orientation, int direction)
    {
        dp.animateMoveChopstick(this, chopstick, orientation, direction);
    }

    public int getId() { return id; }
    public int getState() { return state; }

    public Chopstick getChopstick1() { return chopstick1; }
    public Chopstick getChopstick2() { return chopstick2; }

    public void setChopstick1(Chopstick c) { chopstick1 = c; }
}