package com.example.mazedisplay;

public class Node implements Comparable<Node>{
    int row;
    int col;
    int cost;
    int estimatedCost;
    Node prev;

    public Node(int row, int col, int cost, int[] end, Node prev) {
        this.row = row;
        this.col = col;
        this.cost = cost;
        this.estimatedCost = Math.abs(end[0] - row) + Math.abs(end[1] - col);
        this.prev = prev;
    }

    @Override
    public int compareTo(Node other) {
        return this.cost + this.estimatedCost - other.cost - other.estimatedCost;
    }

    @Override
    public String toString() {
        return "Node at row " + row + " and col " + col;
    }
}
