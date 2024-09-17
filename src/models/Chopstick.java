package models;

public class Chopstick
{
    private final int id;
    private int owner;

    public Chopstick(int chopstickId)
    {
        // Default no-owner value
        owner = -1;
        id = chopstickId;
    }
    public synchronized boolean tryPickUp(int philosopherIndex)
    {
        if (owner == -1)
        {
            owner = philosopherIndex;
            return true;
        }
        else return false;
    }
    public synchronized void putDown()
    {
        owner = -1;
    }

    // Getters
    public int getId() { return id; }
    public int getOwner() { return owner; }
}