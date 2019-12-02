package dataStructure;

import parser.DimacsParser;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

/**
 * A set of clauses.
 * 
 */
public class ClauseSet {

	/* Clauses of this set */
	private Vector<Clause> clauses;

	/* List of all variables */
	private HashMap<Integer, Variable> variables;

	private Vector<Clause> units;

	/**
	 * Constructs a clause set from the given DIMACS file.
	 * 
	 * @param filePath
	 *            file path of the DIMACS file.
	 */
	public ClauseSet(String filePath) {
		DimacsParser parser = new DimacsParser();
		Vector<Vector<Integer>> parsedClauses = parser.parse(filePath);

		// collect all variables
		this.variables = new HashMap<Integer, Variable>();
		for (Vector<Integer> clause : parsedClauses) {
			for (Integer lit : clause) {
				int var = Math.abs(lit);
				if (!variables.containsKey(var)) {
					this.variables.put(var, new Variable(var));
				} else {
					this.variables.get(var).incCount();
				}

			}
		}

		// collect all clauses
		this.clauses = new Vector<Clause>();
		for (Vector<Integer> literals : parsedClauses) {
			this.clauses.add(new Clause(literals, this.variables));
		}

		// collect initial unit clauses
		this.units = new Vector<Clause>();
		for (Clause clause : this.clauses) {
			if (clause.getClauseState() == Clause.ClauseState.UNIT) {
				units.add(clause);
			}
		}
	}

	public Vector<Clause> getUnits() {
		return units;
	}

	public Vector<Clause> getClauses() {
		return clauses;
	}

	public void addClause(Clause clause) {
		this.clauses.add(clause);
		for (Integer lit : clause.getLiterals()) {
			this.variables.get(Math.abs(lit)).incActivity();
		}
	}

	public HashMap<Integer, Variable> getVariables() {
		return this.variables;
	}

	public void decActivityForAllVars() {
		for (Variable var : this.variables.values()) {
			var.decActivity();
		}
	}

	/**
	 * Executes unit propagation and checks for the existence of an empty
	 * clause.
	 * 
	 * @return true, if an empty clause exists, otherwise false.
	 */
	public Clause unitPropagation(Stack<Variable> stack, int level) {
		while (!this.units.isEmpty()) {
			Clause unitClause = this.units.get(0);
			this.units.remove(0);
			int unitLit = unitClause.getUnassigned(this.variables);
			if (unitLit == 0) {
				unitClause.setClauseState(Clause.ClauseState.EMPTY);
				return unitClause; // actually no unit clause but "EMPTY"
			}
			Variable unitVar = this.variables.get(Math.abs(unitLit));
			unitVar.assign(unitLit > 0, this.variables, this.units, stack, unitClause, level);
		}

		return null;
	}

	@Override
	public String toString() {
		return clausesToString() + "\n\n" + varsToString();
	}

	/**
	 * Returns all clauses as string representation.
	 * 
	 * @return a string representation of all clauses.
	 */
	public String clausesToString() {
		String res = "";
		for (Clause clause : clauses)
			res += clause + "\n";
		return res;
	}

	/**
	 * Returns all variables as string representation.
	 * 
	 * @return a string representation of all variables.
	 */
	public String varsToString() {
		String res = "";
		for (int i = 1; i <= variables.size(); i++)
			res += "Variable " + i + ": " + variables.get(i) + "\n\n";
		return res;
	}
}