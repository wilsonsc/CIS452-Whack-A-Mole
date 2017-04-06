package com.wilson.scott.whack_a_mole;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Custom wrote adapter to allow mole class files to be visually shown in a grid layout.
 *
 * @author Scott Wilson
 **/

public class MoleAdapter extends BaseAdapter {

    private Context context;

    //the pieces that the adapter will display
    private Mole[] moles;
    private MoleBoard board;

    /**
     * Default constructor
     * Saves parameters for future use
     *
     * @param context context resource from which this class was called
     * @param board the current game board
     **/
    public MoleAdapter(Context context, MoleBoard board) {
        this.board = board;
        moles = board.getMoles();
        this.context = context;
    }

    /**
     * Used to update the grid upon any changes (such as a mole popping up or down)
     **/
    public void updateView() {
        notifyDataSetChanged();
    }

    /**
     * Produces a displayable view for the display element that uses this CustomAdapter
     *
     * @param pos the position of the piece
     * @param convertView a view that will be reused if present
     * @param parent the group in which this view belongs to
     * @return the created view
     **/
    public View getView(int pos, View convertView, ViewGroup parent) {

        //Inflates context
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Creates a new view if an existing view is not available
        if (convertView == null)
            convertView = inflater.inflate(R.layout.molehole, null);

        SquareImageView iv = (SquareImageView) convertView.findViewById(R.id.moleImage);

        if (moles[pos].isActive() && board.getGameProgress())
            iv.setImageResource(R.drawable.mole);
        else
            iv.setImageResource(R.drawable.molehole);

        return convertView;

    }

    /**
     * A count of the number of grid positions
     *
     * @return the number of grid positions
     **/
    public int getCount() {
        return moles.length;
    }

    /**
     * Gets the mole at the requested position
     *
     * @param position the position of the mole
     * @return the mole at the indicated position
     */
    public Object getItem(int position) {
        return moles[position];
    }

    /**
     * This is an abstract method that required an implementation, this application does not use it
     **/
    public long getItemId(int position) {
        return 0;
    }
}