package com.example.mazedisplay;

import java.util.Arrays;

public class PriorityQueue {
    private Node[] heap;
    private int size;

    public PriorityQueue() {
        heap = new Node[10];
        size = 0;
    }

    public PriorityQueue(Node value) {
        heap = new Node[10];
        size = 0;
        this.add(value);
    }

    public void add(Node value) {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
        heap[size] = value;
        size++;
        heapifyUp();
    }

    public Node poll() {
        if (size == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        Node value = heap[0];
        heap[0] = heap[size - 1];
        size--;
        heapifyDown();
        return value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void heapifyUp() {
        int index = size - 1;
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap[index].compareTo(heap[parentIndex]) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void heapifyDown() {
        int index = 0;
        while (index < size / 2) {
            int leftChildIndex = 2 * index + 1;
            int rightChildIndex = 2 * index + 2;
            int smallerChildIndex = leftChildIndex;
            if (rightChildIndex < size && heap[rightChildIndex].compareTo(heap[leftChildIndex]) < 0) {
                smallerChildIndex = rightChildIndex;
            }
            if (heap[index].compareTo(heap[smallerChildIndex]) > 0) {
                swap(index, smallerChildIndex);
                index = smallerChildIndex;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        Node temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}
