package edu.eci.arsw.highlandersim;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean isPaused = false;

    private boolean stillAlive;


    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        stillAlive = true;
    }

    public void run() {

        while (stillAlive) {

            synchronized (this) {
                while (isPaused) {
                    try {
                        wait();  // Pausa el hilo si `paused` es true
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
        // Sorting by name
        List<Immortal> immortals = new ArrayList<>();
        immortals.add(this);
        immortals.add(i2);
        Collections.sort(immortals, new ImmortalComparator());

        synchronized (immortals.get(0)){
            synchronized (immortals.get(1)){
                if(immortalsPopulation.contains(this) && immortalsPopulation.contains(i2)){
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                    } else if (immortalsPopulation.contains(i2)) {
                        i2.alreadyDead();
                        immortalsPopulation.remove(i2);
                        System.out.println(i2.name + " HAS DIED!!! AHHHHHHHHHH!!!!!!!!!!!!!");
                        System.out.println(immortalsPopulation);
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    }
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    public void alreadyDead(){ this.stillAlive = false; }

    public void setPaused(boolean isPaused) {
        synchronized (this) {
            this.isPaused = isPaused;
            if (!isPaused) {
                notifyAll();  // Notifica a todos los hilos que están esperando
            }
        }
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

}
