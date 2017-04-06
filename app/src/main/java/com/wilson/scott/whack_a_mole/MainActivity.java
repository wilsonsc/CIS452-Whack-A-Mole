/**
 *  Whack-A-Mole
 *
 *  A simplistic carnival style game in which the user hits the moles that pop out of their holes.
 *  Has custom user game board size and maximum number of moles that can be up at a time.
 *  Each mole is worth one point, hit as many as possible to obtain a high score.
 *
 *  @author Scott Wilson
 *  @version 1.0
 */
package com.wilson.scott.whack_a_mole;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Message codes for handler
    private final int WAS_HIT = 7;
    private final int MOLEHOLE_ACTIVE = 101;
    private final int MOLEHOLE_INACTIVE = 102;
    private final int INITIAL_TIMER = 30;

    private Timer t;
    private GridView grid;
    private TextView scoreText;
    private TextView timerText;
    private int time = INITIAL_TIMER;
    private int score;
    private MoleBoard board;
    private MoleAdapter adapter;
    int width;
    int moleLimit = 3;
    int boardSize = 3;

    /**
     * The start up function upon loading the application
     * Creates the default game board upon loading and begins the game
     *
     * @param savedInstanceState a previously saved instance (unused by this application)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load layout based off XML properties
        setContentView(R.layout.activity_main);

        //Retrieves information about the physical size of the users device
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Store the width the device
        width = dm.widthPixels;

        //Initialize grid based off parameters declared in xml
        grid = (GridView) findViewById(R.id.gameBoard);
        grid.setStretchMode(GridView.NO_STRETCH);
        scoreText = (TextView) findViewById(R.id.score);
        timerText = (TextView) findViewById(R.id.timerText);

        board = new MoleBoard(boardSize, moleLimit);
        board.newGame();

        // The possible sizes the user may select for the game
        String[] sizes = new String[] {"3x3", "4x4", "5x5"};

        // An adapter that holds possible user sizes in dropdown box
        ArrayAdapter<String> selectAdapter = new ArrayAdapter<> (this,
                android.R.layout.simple_spinner_item, sizes);

        //Sets the dropdown box based off xml defined values
        Spinner gameSize = (Spinner) findViewById(R.id.selectSize);

        //Sets the adapter for the dropdown box
        gameSize.getLayoutParams().width = width / 6;
        gameSize.setAdapter(selectAdapter);
        gameSize.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {

                     /**
                      * Set what happens when an item is selected from the dropdown box
                      * Size of the board is set to the index of the selected item + 3
                      *
                      * @param parent the parent view
                      * @param view the current view from which this was selected
                      * @param position the position of the selected item
                      * @param id the id of the selected item
                      **/
                     public void onItemSelected(AdapterView<?> parent, View view,
                                                int position, long id) {

                         //Set the custom size to the index of the item + 3
                         boardSize = (position + 3);
                     }

                     /**
                      * Must implement this abstract method, does nothing in this application
                      **/
                     public void onNothingSelected(AdapterView<?> parent) {

                     }
                 });

        // The number of maximum moles the user may select for the game
        String[] maxMoles = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

        // An adapter that holds possible number of max moles in dropdown box
        ArrayAdapter<String> selectMaxMoles = new ArrayAdapter<> (this,
                android.R.layout.simple_spinner_item, maxMoles);

        //Sets the dropdown box based off xml defined values
        Spinner maxMoleSpin = (Spinner) findViewById(R.id.maxMoleSpinner);

        //Sets the adapter for the dropdown box
        maxMoleSpin.getLayoutParams().width = width / 6;
        maxMoleSpin.setAdapter(selectMaxMoles);
        maxMoleSpin.setOnItemSelectedListener
                (new AdapterView.OnItemSelectedListener() {

                    /**
                     * Set what happens when an item is selected from the dropdown box
                     * Size of the board is set to the index of the selected item + 1
                     *
                     * @param parent the parent view
                     * @param view the current view from which this was selected
                     * @param position the position of the selected item
                     * @param id the id of the selected item
                     **/
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {

                        //Set the custom size to the index of the item + 1
                        moleLimit = (position + 1);
                    }

                    /**
                     * Must implement this abstract method, does nothing in this application
                     **/
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

       startGame();

        /**
         * A click listener for the grid
         * When user clicks on a grid location, it will forward the location and a current timestamp
         * to the game board for processing
         **/
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Defines what happens when the user clicks on a given piece
             *
             * @param parent the parent view of the view in which the listener exists
             * @param v the view in which the listener exists
             * @param position the position of the piece clicked
             * @param id the id of the piece clicked
             */
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                /**
                 * A timestamp is used here as a means to ensure that no matter how the cpu context
                 * switches, it will still have a way to verify if a click was made in time or not
                 */
                board.hit(position, System.currentTimeMillis());
            }
        });

    }

    /**
     * A handler for the UI thread
     * Messages of moles popping up, or returning to their holes or that it was successfully hit
     * are all valid messages that may be passed by the game engine
     */
    Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if ( msg.what == MOLEHOLE_ACTIVE  || msg.what == MOLEHOLE_INACTIVE) {
                adapter.updateView();
            }

            if ( msg.what == WAS_HIT ) {
                score++;
                scoreText.setText("Score: " + score);
                adapter.updateView();
            }
        }
    };

    /**
     * Timer that starts at time zero and decrements every 1000 milliseconds.
     * Updates timer text every second.
     **/
    public void beginTimer() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        //Initialize timer from xml parameters
                        timerText = (TextView) findViewById
                                (R.id.timerText);


                            timerText.setText("Time Remaining: " + time);

                        // When the player runs out of time, stop the game.
                        if (time == 0) {
                            stopGame();
                        }

                       time -= 1;
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * Overrides method in Menu class. Initializes the contents of the Activity's standard
     * options menu.
     *
     * @param menu The options menu in which the items are placed
     * @return true Must return true for the menu to be displayed
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Overrides method in MenuItem class. Called whenever an item in the menu is selected.
     *
     * @param item The menu item that was selected
     * @return  status of menu click
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // performs tasks depending on menu item selected
        switch (item.getItemId()) {
            case R.id.applySettings:
                resetGame();
                startGame();
                return true;
            case R.id.reset:
                resetGame();
                beginTimer();
                board.startGame(uiHandler);
                return true;
            case R.id.exit:
                board.endGame();
                t.cancel();
                t.purge();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper method to reset the game
     */
    private void resetGame() {
        score = 0;
        scoreText.setText("Score: " + score);
        time = INITIAL_TIMER;
        board.endGame();
        t.cancel();
        t.purge();
    }

    /**
     * Helper method to start a new game
     */
    private void startGame() {
        board = new MoleBoard(boardSize, moleLimit);
        board.newGame();
        grid.setColumnWidth(width / boardSize);
        adapter = new MoleAdapter(this, board);
        grid.setAdapter(adapter);
        board.startGame(uiHandler);
        beginTimer();
    }

    /**
     * Helper method to stop the game
     */
    private void stopGame() {
        board.endGame();
        t.cancel();
        t.purge();
    }

}
