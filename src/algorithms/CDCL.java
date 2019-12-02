package algorithms;

import dataStructure.Clause;
import dataStructure.ClauseSet;
import dataStructure.Variable;

import java.util.*;

public class CDCL {
    // Die Instanz, auf der der CDCL Algorithmus arbeitet
    private ClauseSet instance;

    // Der Stack, auf dem die Variablen, die belegt werden, gespeichert werden
    private Stack<Variable> stack;

    public CDCL(ClauseSet clauseSet) {
        this.instance = clauseSet;
        this.stack = new Stack<Variable>();
    }

    // Diese Methode berechnet die Resolvente zwischen c1 und c2 und gibt diese als neue Klausel zurück
    private Clause resolve(Clause c1, Clause c2) {
        Vector<Integer> res = new Vector<Integer>();

        for (Integer lit1Res : c1.getLiterals()) {
            for (Integer lit2Res : c2.getLiterals()) {
                if (lit1Res == -lit2Res) {
                    for (Integer lit1 : c1.getLiterals()) {
                        if (!lit1.equals(lit1Res)) {
                            res.add(lit1);
                        }
                    }
                    for (Integer lit2 : c2.getLiterals()) {
                        if (!lit2.equals(lit2Res) && !res.contains(lit2)) {
                            res.add(lit2);
                        }
                    }
                    return new Clause(res, this.instance.getVariables());
                }
            }
        }
        System.out.println("no resolvente found");
        res.addAll(c1.getLiterals());
        res.addAll(c2.getLiterals());
        return new Clause(res, this.instance.getVariables());
    }

    // Diese Methode berechnet die 1UIP Klausel ausgehend von einer leeren Klausel conflict (= unterster Grund)
    // und einem Grund reason (= vorletzer Grund)
    private Clause get1UIP(Clause conflict, Clause reason) {
        int stackIndex = this.stack.size() - 1;

        Clause currResolvente = resolve(conflict, reason);
        Clause nextReason = this.stack.get(stackIndex).getReason();
        while (nextReason != null) {
            currResolvente = resolve(currResolvente, nextReason);
            stackIndex--;
            nextReason = this.stack.get(stackIndex).getReason();
        }
        return currResolvente;
    }

    //Die Methode lernt eine neue Klausel (1UIP) aus dem Konflikt und gibt das Level zurück, zu dem zurückgesprungen werden muss
    private int analyseConflict(Clause conflict) {
        Clause firstUIP = get1UIP(conflict, this.stack.pop().getReason());
        this.instance.addClause(firstUIP);
        HashSet<Integer> levels = new HashSet<Integer>();
        for (Integer lit : firstUIP.getLiterals()) {
            int level = this.instance.getVariables().get(Math.abs(lit)).getLevel();
            levels.add(level);
        }
        List<Integer> levelsSorted = new ArrayList<Integer>(levels);
        Collections.sort(levelsSorted);

        if (levels.size() == 0) {
            return -1;
        } else if (levels.size() == 1) {
            return levelsSorted.get(0);
        } else {
            return levelsSorted.get(levels.size() - 2);
        }
    }

    // gibt die nächste Variable zurück, die belegt werden soll = Variable mit der höchsten Aktivität
    private Variable getNextVar() {
        float maxActivity = 0;
        Variable maxActive = null;
        for (Variable var : this.instance.getVariables().values()) {
            if (var.getState() == Variable.State.OPEN && var.getActivity() > maxActivity) {
                maxActivity = var.getActivity();
                maxActive = var;
            }
        }
        this.instance.decActivityForAllVars();
        return maxActive;
    }

    public boolean solve() {
        int level = 0;
        while (true) {
            Clause UPResult = this.instance.unitPropagation(this.stack, level);
            if (UPResult != null) {
                level = analyseConflict(UPResult);
                if (level == -1) {
                    return false;
                }
                backtrack(level);
            } else {
                boolean isSAT = true;
                for (Clause clause : this.instance.getClauses()) {
                    if (clause.getClauseState() != Clause.ClauseState.SAT) {
                        isSAT = false;
                        break;
                    }
                }
                if (isSAT) {
                    return true;
                }
                level++;
                Variable nextVar = getNextVar();
                nextVar.assign(false, this.instance.getVariables(), this.instance.getUnits(), this.stack, null, level);
            }
        }
    }

    private void backtrack(int level) {
        Variable lastVar = this.stack.lastElement();
        while (lastVar.getLevel() > level) {
            this.stack.pop();
            lastVar.unassign(this.instance.getClauses(), this.instance.getVariables());
//            for (Clause clause : this.instance.getClauses()) {
//                if (clause.getLiterals().contains(lastVar.getId()) || clause.getLiterals().contains(-lastVar.getId())) {
//                    // ToDo: check watching the new unassigned variable in this clause
//                }
//            }
            lastVar = this.stack.lastElement();
        }
    }
}
