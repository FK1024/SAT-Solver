package parser;

import dataStructure.Clause;
import dataStructure.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DimacsParser {
    private static final String COMMENT_PREFIX = "c";
    private static final String PROBLEM_LINE_PREFIX = "p";

    public Vector<Vector<Integer>> parse(String CnfFilePath) {
        int numberOfVariables = 0;
        int numberOfClauses = 0;
        Vector<Vector<Integer>> clauses = new Vector<Vector<Integer>>();
        Vector<Integer> currentClause = new Vector<Integer>();

        List<String> lines = new ArrayList<String>();
        try {
            lines = Files.readAllLines(Paths.get(CnfFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            if (line.startsWith(COMMENT_PREFIX)) continue;

            String[] lineParts = line.trim().replaceAll(" +", " ").split(" ");

            if (line.startsWith(PROBLEM_LINE_PREFIX)) {
                numberOfVariables = Integer.parseInt(lineParts[2]);
                numberOfClauses = Integer.parseInt(lineParts[3]);
            } else {
                currentClause.clear();
                for (int i = 0; i < lineParts.length - 1; i++) { // - 1 because of trailing 0
                    Integer lit = Integer.parseInt(lineParts[i]);
                    currentClause.add(lit);
                }
                clauses.add(new Vector<Integer>(currentClause));
            }
        }

        return clauses;
    }
}
