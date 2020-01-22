package com.example.wordstack;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.Arrays;
import java.util.Stack;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {
    private ArrayList<String> words = new ArrayList<String>();
    private static int WORD_LENGTH = 3;
    private Stack<LetterTile> placedTiles = new Stack<LetterTile>();
    private StackedLayout stackedLayout;
    private String word1, word2, playerWord1, playerWord2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == WORD_LENGTH)
                    words.add(word);
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }

        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.linearLayout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());


        playerWord1 = "";
        playerWord2 = "";
    }

    //TouchListener.onTouch moves the top tile to either of the white areas when the user touches the area.
    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }

                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    if(v.getId() == R.id.word1)
                        playerWord1 += tile.moveToViewGroup((ViewGroup) v);
                    else
                        playerWord2 += tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        checkWin();
                    }
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }



    public void onStartGame(View view) {
            ViewGroup word1LinearLayout = (ViewGroup)findViewById(R.id.word1);
            ViewGroup word2LinearLayout = (ViewGroup)findViewById(R.id.word2);

            word1LinearLayout.removeAllViews();
            word2LinearLayout.removeAllViews();
        try{
            stackedLayout.clear();
        }catch(EmptyStackException e){}

        //set text to messagebox
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game Started");

        Collections.shuffle(words);
        word1 = words.get(0);
        word2 = words.get(1);
        //shuffle the letters of the words while preserving word order
        Random rand = new Random();

        int count1 = 0;
        int count2 = 0;
        String scrambledWord = "";
        while (count1 < word1.length() || count2 < word2.length()) {
            int pick = rand.nextInt(2);
            if (pick == 0 && count1<WORD_LENGTH) {
                scrambledWord += word1.charAt(count1);
                count1++;
            } else if(count2 < WORD_LENGTH){
                scrambledWord += word2.charAt(count2);
                count2++;
            }

        }

        //messageBox settext
        for(int i = scrambledWord.length()-1; i>=0; --i){
            stackedLayout.push(new LetterTile(this, scrambledWord.charAt(i)));
        }


        }

    public void onUndo(View view) {

        if(!placedTiles.isEmpty()) {

            if (((View)placedTiles.peek().getParent()).getId() == R.id.word1){
                playerWord1 = new StringBuilder(playerWord1).deleteCharAt(playerWord1.length()-1).toString();
                placedTiles.pop().moveToViewGroup(stackedLayout);
            }
            else {
                playerWord2 = new StringBuilder(playerWord2).deleteCharAt(playerWord2.length()-1).toString();
                placedTiles.pop().moveToViewGroup(stackedLayout);
            }
        }

    }

    public void checkWin() {

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        if (word1.equals(playerWord1) && word2.equals(playerWord2))
            messageBox.setText("You win! " + word1 + " " + word2);
        else if (words.contains(playerWord1) && words.contains(playerWord2)) {
            messageBox.setText("You found alternative words! " + playerWord1 + " " + playerWord2);
        } else {
            messageBox.setText("Try again");
        }


    }


}
