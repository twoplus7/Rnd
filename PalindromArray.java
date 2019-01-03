package com.array;

import java.util.Scanner;

public class PalindromArray {

	@SuppressWarnings("resource")
	public static void main(String[] args) {

		/* Take an array as user input */
		Scanner s = new Scanner(System.in);
		System.out.println("Enter number of elements");
		int n = s.nextInt();
		int a[] = new int[n];
		System.out.println("Enter elements");
		for (int i = 0; i < n; i++) {
			a[i] = s.nextInt();

		}

		/* Reversing an array */
		int k = 0;
		int[] b = new int[a.length];
		for (int i = a.length - 1; i <= a.length; i--) {
			b[k] = a[i];
			if (k < a.length) {
				k++;
			}
			if (i == 0)
				break;
		}

		/* Compare two arrays */
		int count = 0;
		for (int j = 0; j <= a.length - 1; j++) {
			if (b[j] == a[j]) {
				count++;
			}
		}
		if (count == a.length) {
			System.out.println("Palindrome");
		} else {
			System.out.println("Not a Palindrome");
		}
	}
}
