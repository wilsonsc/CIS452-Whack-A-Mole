package com.wilson.scott.whack_a_mole;

import android.os.Handler;

import java.util.concurrent.Semaphore;

/**
 * A representation of the board used in the Whack-A-Mole game
 *
 * @author Scott Wilson
 */

public class MoleBoard {

    private int gameSize;
    private int columns;
    private boolean gameInProgress;
    private Mole[] moles;
    private Thread[] threads;
    private Semaphore moleSem;
    private int maxMoles;

    /**
     * Constructor for MoleBoard class
     *
     * @param size  the size of the board (size by size)
     * @param maxMoles  the maximum number of moles that can be up at a given time
     */
    public MoleBoard(int size, int maxMoles) {

        this.maxMoles = maxMoles;
        gameSize = size * size;
        columns = size;
    }

    /**
     * Obtain an array of the current moles
     *
     * @return an array of mole objects
     **/
    public Mole[] getMoles() {
        return moles;
    }

    /**
     * Create the game with the appropriate size
     **/
    public void newGame() {

            moles = new Mole[gameSize];
            moleSem = new Semaphore(maxMoles, true);
    }

    /**
     * After the game has been created, it can be started through this method
     *
     * @param uiHandler The handler in which messages will be passed back to for UI updating
     */
    public void startGame(Handler uiHandler) {
        threads = new Thread[gameSize];

        //Initialize moles and assign them to threads
        for (int i = 0; i < gameSize; i++) {
            moles[i] = new Mole(moleSem, uiHandler, gameSize);
            threads[i] = new Thread(moles[i]);
            threads[i].start();
        }
        gameInProgress = true;
    }

    /**
     * Getter for is the game in progress variable
     *
     * @return true if the game is in progress, false otherwise
     **/
    public boolean getGameProgress() {

        return gameInProgress;
    }

    /**
     * Game is over, set all threads for clean termination
     **/
    public void endGame() {
        gameInProgress = false;
        if (threads != null) {
            for (int i = 0; i < threads.length; i++) {
                moles[i].terminate();
            }
        }
    }

    /**
     * The user has clicked on a grid position, pass this information to the mole to check if
     * the hit is successful
     *
     * @param number the grid position in which the user tapped
     * @param timeStamp the timestamp in milliseconds since epoch that the grid was tapped
     */
    public void hit(int number, long timeStamp) {
        if (gameInProgress) {
            //Set the timestamp on the mole
            moles[number].setHitTime(timeStamp);
            //If the mole is currently active, interrupt it to check the hit
            if (moles[number].isActive()) {
                threads[number].interrupt();
            }
        }
    }
}