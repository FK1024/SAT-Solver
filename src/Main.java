import algorithms.CDCL;
import dataStructure.Clause;
import dataStructure.ClauseSet;

public class Main {
    public static void main(String[] args) {
//        ClauseSet clauseSet = new ClauseSet("formulas/formula01.cnf");
        ClauseSet clauseSet = new ClauseSet("formulas/small_aim/yes/aim-50-1_6-yes1-1.cnf");
//        ClauseSet clauseSet = new ClauseSet("formulas/small_aim/yes/ownExample-yes.cnf");
        CDCL cdcl = new CDCL(clauseSet);
        System.out.println(cdcl.solve());
    }
}
