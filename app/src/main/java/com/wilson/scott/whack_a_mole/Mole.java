package com.wilson.scott.whack_a_mole;

import android.os.Handler;
import android.os.Message;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;


/**
 * A mole class that is ran upon a single thread for a single mole hole
 * Used with the MoleBoard object
 *
 * @author Scott Wilson
 */


public class Mole implements Runnable {

    private Semaphore moleSem;
    private boolean active = false;
    private Handler uiHandler;
    private long hitAt;
    private long becameActiveAt;
    private final long MIN_SLEEP = 1000;
    private final int WAS_HIT = 7;
    private final int MOLEHOLE_ACTIVE = 101;
    private final int MOLEHOLE_INACTIVE = 102;
    private int maxSleep;
    private int activeFor;
    private boolean alreadyBeenHit = false;

    // Volatile variable that determines if the thread is ready to be shut down gracefully
    private volatile boolean running = true;

    /**
     * Default constructor of a mole object
     *
     * @param moleSem   The semaphore that allows for a maximum number of moles
     * @param uiHandler The UI handler, which allows for this class to communicate to the UI thread
     * @param numMoles  The total number of moles in the current game
     */
    public Mole(Semaphore moleSem, Handler uiHandler, int numMoles) {
        this.moleSem = moleSem;
        this.uiHandler = uiHandler;

        // Total number of moles in the game * 0.8 seconds as the maximum sleep timer
        maxSleep = numMoles * 800;
    }

    /**
     * The method that is ran when the thread is created for this mole
     */
    @Override
    public void run() {

        while (running) {
            /**
             * Random sleep timer occurs before acquiring semaphore so the first x threads aren't
             * always the first to get a turn to "pop up"
             */
            try {
                //Sleep for time between MIN_SLEEP and maxSleep
                sleep((int)(Math.random() * (maxSleep - MIN_SLEEP)) + MIN_SLEEP);
            } catch (InterruptedException e) {
                //Do nothing if interrupted here
            }
            //Check to see if the mole was hit during its last active time
            checkHit();

            //Attempt to acquire the semaphore, if all slots are full, skip the loop and sleep
            if (moleSem.tryAcquire()) {
                //Let the UI thread know that you are popping up
                Message isActive = new Message();
                isActive.what = MOLEHOLE_ACTIVE;
                uiHandler.sendMessage(isActive);
                active = true;
                alreadyBeenHit = false;
                //Record keeping timestamp log to determine if successfully hit
                becameActiveAt = System.currentTimeMillis();
                try {
                    //Stay active for between 1-3 seconds
                    activeFor = (int)(Math.random() * 2000) + 1000;
                    sleep(activeFor);
                } catch (InterruptedException e) {
                    //If interrupted, check to see if a hit was successful
                    checkHit();
                }
                //Notify UI thread that you are inactive again
                Message inactive = new Message();
                inactive.what = MOLEHOLE_INACTIVE;
                uiHandler.sendMessage(inactive);
                active = false;
                //Release semaphore
                moleSem.release();
            }
        }
    }

    /**
     * Used to set the time in which the user hit the hole based off a timestamp
     *
     * @param hitAt The number of milliseconds since epoch in which the attempt to hit was made
     */
    public void setHitTime(long hitAt) {
       this.hitAt = hitAt;
    }

    /**
     * Used to check if the mole was successfully hit
     * Check if the hitAt timestamp falls between when this mole was last active and the time it
     * was to stay active for and if it has already been registered as a hit
     */
    private void checkHit() {
        if (hitAt > becameActiveAt && hitAt < (becameActiveAt + activeFor) && !alreadyBeenHit) {
            alreadyBeenHit = true;
            Message m = new Message();
            m.what = WAS_HIT;
            uiHandler.sendMessage(m);
        }
    }

    /**
     * Used to mark this mole and thread for termination
     */
    public void terminate() {
        running = false;
    }

    /**
     * The current status of this mole (in the hole or out)
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }
}
