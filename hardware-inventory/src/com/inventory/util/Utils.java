package com.inventory.util;

import com.inventory.model.Dependency;
import java.util.Scanner;

public class Utils {
    private static final Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static Dependency readDependencyInteractive() {
        String g = readLine("  groupId: ");
        String a = readLine("  artifactId: ");
        String v = readLine("  version: ");
        return new Dependency(g, a, v);
    }
}
