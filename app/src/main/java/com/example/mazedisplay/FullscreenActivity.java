package com.example.mazedisplay;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {


    /**
     * User added vars - Maze details
     */
    public static final int NUM_ROWS = 32;
    public static final int NUM_COLS = 20;

    private static final short[][] grid = new short[NUM_ROWS][NUM_COLS];
    private static final short WALL = 0;
    private static final short PATH = 1;
    private static final short TRAVELED = 2;
    private static final short FINAL = 3;

    private static final int TIMESTEP_MS = 0;
    private static final int MARGIN = 1;
    private static final int DEV_HEIGHT = 1280;
    private static final int DEV_WIDTH = 800;

    private static int[] startPoint;
    private static int[] endPoint;
    private static boolean solved;

    // Colors
    private static final int BACKGROUND_COLOR = Color.LTGRAY;
    private static final int PATH_DEFAULT_COLOR = Color.GRAY;
    private static final int PATH_EXPLORED_COLOR = Color.DKGRAY;
    private static final int PATH_FINAL_COLOR = Color.GREEN;
    private static final int GOAL_COLOR = Color.YELLOW;
    private static final int WALL_COLOR = Color.BLUE;

    // We use a custom PriorityQueue class since our minimum SDK
    // does not have access to the built-in PriorityQueue class.
    private static PriorityQueue queue;
    private static Set<Node> visited;
    private static LinkedList<Node> path;

    private static final Random rand = new Random();
    private Handler handler;
    private Runnable runnable;

    public void fullscreenCall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        // Find the root view of the activity
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // Set the system UI visibility of the root view to hide the border with the app name at the top
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setupMap();

        GridLayout gridLayout = findViewById(R.id.grid);
        gridLayout.setBackgroundColor(BACKGROUND_COLOR);
        gridLayout.setRowCount(NUM_ROWS);
        gridLayout.setColumnCount(NUM_COLS);
        redrawSquares();

        // Create a new handler
        handler = new Handler();

        // Create a new runnable
        runnable = () -> {
            fixedInterval();
            fullscreenCall();
            // Schedule the runnable to be executed again after a certain delay
            handler.postDelayed(runnable, TIMESTEP_MS);
        };

        // Schedule the runnable to be executed after a certain delay
        handler.postDelayed(runnable, TIMESTEP_MS);
    }

    private void redrawSquares() {
        GridLayout gridLayout = findViewById(R.id.grid);
        int width = DEV_WIDTH / NUM_COLS - 2 * MARGIN;
        int height = DEV_HEIGHT / NUM_ROWS - 2 * MARGIN;
        gridLayout.removeAllViews();
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                TextView textView = new TextView(this);
                textView.setGravity(Gravity.CENTER);
                if ((startPoint[0] == i && startPoint[1] == j)) {
                    textView.setBackgroundColor(PATH_FINAL_COLOR);
                    if (NUM_ROWS < 50) textView.setText("O");
                } else if (endPoint[0] == i && endPoint[1] == j) {
                    textView.setBackgroundColor(GOAL_COLOR);
                    if (NUM_ROWS < 50) textView.setText("X");
                } else if (grid[i][j] == FINAL) {
                    textView.setBackgroundColor(PATH_FINAL_COLOR);
                } else if (grid[i][j] == TRAVELED) {
                    textView.setBackgroundColor(PATH_EXPLORED_COLOR);
                } else if (grid[i][j] == PATH) {
                    textView.setBackgroundColor(PATH_DEFAULT_COLOR);
                } else {
                    textView.setBackgroundColor(WALL_COLOR);
                }
                // Set the width and height of the TextView element
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                layoutParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

                textView.setLayoutParams(layoutParams);

                gridLayout.addView(textView);
            }
        }
    }

    // Use randomized Prim's algorithm to set up grid maze
    private void setupMap() {
        // Clear entire grid, fill with walls
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                grid[i][j] = 0;
            }
        }

        // Choose a starting cell randomly and mark it as a path
        int startRow = rand.nextInt(NUM_ROWS);
        int startCol = rand.nextInt(NUM_COLS);

        // Add the starting cell to the list of frontier cells
        ArrayList<int[]> frontierCells = new ArrayList<>();
        frontierCells.add(new int[]{startRow, startCol, startRow, startCol});

        // While there are still frontier cells
        while (!frontierCells.isEmpty()) {
            // Choose a random cell from the list of frontier cells
            int[] cell = frontierCells.remove(rand.nextInt(frontierCells.size()));
            int row = cell[2];
            int col = cell[3];
            if (grid[row][col] == WALL) {
                grid[cell[0]][cell[1]] = grid[row][col] = PATH;
                if (row > 1 && grid[row - 2][col] == WALL) {
                    frontierCells.add(new int[]{row - 1, col, row - 2, col});
                }
                if (col > 1 && grid[row][col - 2] == WALL) {
                    frontierCells.add(new int[]{row, col - 1, row, col - 2});
                }
                if (row < NUM_ROWS - 2 && grid[row + 2][col] == WALL) {
                    frontierCells.add(new int[]{row + 1, col, row + 2, col});
                }
                if (col < NUM_COLS - 2 && grid[row][col + 2] == WALL) {
                    frontierCells.add(new int[]{row, col + 1, row, col + 2});
                }
            }
        }

        // Now that the map has set up, we traverse to find starting and ending points
        startPoint = getStartPoint();
        endPoint = getEndPoint();
        // Set up queue for map solving
        queue = new PriorityQueue(new Node(startPoint[0], startPoint[1], 0, endPoint, null));
        visited = new HashSet<>();
        path = new LinkedList<>();
        solved = false;
    }

    private int[] getStartPoint() {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                if (grid[i][j] == 1) return new int[] {i, j};
            }
        }
        return null;
    }

    private int[] getEndPoint() {
        for (int i = NUM_ROWS - 1; i > 0; i--) {
            for (int j = NUM_COLS - 1; j > 0; j--) {
                if (grid[i][j] == 1) return new int[] {i, j};
            }
        }
        return null;
    }

    private ArrayList<int[]> getNeighbors(int row, int col) {
        ArrayList<int[]> neighbors = new ArrayList<>();
        if (row > 0 && grid[row - 1][col] == PATH) {
            neighbors.add(new int[]{row - 1, col});
        }
        if (col > 0 && grid[row][col - 1] == PATH) {
            neighbors.add(new int[]{row, col - 1});
        }
        if (row < NUM_ROWS - 1 && grid[row + 1][col] == PATH) {
            neighbors.add(new int[]{row + 1, col});
        }
        if (col < NUM_COLS - 1 && grid[row][col + 1] == PATH) {
            neighbors.add(new int[]{row, col + 1});
        }
        return neighbors;
    }

    private void fixedInterval() {
        if (solved) {
            if (path.isEmpty()) {
                setupMap();
            } else {
                reconstructPath();
            }
        } else {
            solveMaze();
        }
        redrawSquares();
    }

    private void reconstructPath() {
        Node val = path.pop();
        grid[val.row][val.col] = FINAL;
        if (path.isEmpty()) grid[endPoint[0]][endPoint[1]] = FINAL;
    }

    private void solveMaze() {
        if (!queue.isEmpty()) {
            Node curr = queue.poll();
            if (curr.row == endPoint[0] && curr.col == endPoint[1]) {
                solved = true;
                while (curr != null) {
                    path.push(curr);
                    curr = curr.prev;
                }
                return;
            }
            visited.add(curr);
            for (int[] neighbor : getNeighbors(curr.row, curr.col)) {
                if (!visited.contains(new Node(neighbor[0], neighbor[1], 0, endPoint, null))) {
                    queue.add(new Node(neighbor[0], neighbor[1], curr.cost + 1, endPoint, curr));
                    grid[neighbor[0]][neighbor[1]] = TRAVELED;
                }
            }
        }
    }
}