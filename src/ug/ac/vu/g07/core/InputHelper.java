package ug.ac.vu.g07.core;

import java.util.Scanner;

/**
 * Shared robust input helper. Never crashes on bad input.
 * @author Group One (G07) - Integration Lead (Ssempewo Brian)
 */
public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("  >> Cannot be blank. Try again.");
        }
    }

    public String readOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v > 0) return v;
                System.out.println("  >> Must be a positive whole number.");
            } catch (NumberFormatException e) {
                System.out.println("  >> Invalid number.");
            }
        }
    }

    public double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v >= 0) return v;
                System.out.println("  >> Cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("  >> Invalid number.");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no")) return false;
            System.out.println("  >> Please answer y or n.");
        }
    }

    public int readMenuChoice(int min, int max) {
        while (true) {
            System.out.print("Enter choice (" + min + "-" + max + "): ");
            try {
                int c = Integer.parseInt(scanner.nextLine().trim());
                if (c >= min && c <= max) return c;
                System.out.println("  >> Choice must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  >> Invalid input.");
            }
        }
    }
}
