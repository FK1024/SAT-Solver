package dataStructure;

import java.util.HashMap;
import java.util.Vector;

/**
 * A clause.
 * 
 */
public class Clause {
	/* Literals of the clause */
	private Vector<Integer> literals;

	private ClauseState clauseState;

	// watched literals
	private int lit1, lit2;

	public enum ClauseState {
		SAT,
		EMPTY,
		UNIT,
		SUCCESS
	}

	/**
	 * Creates a new clause with the given literals.
	 * 
	 * @param literals
	 *            literals of the clause
	 * @param variables
	 */
	public Clause(Vector<Integer> literals, HashMap<Integer, Variable> variables) {
		this.literals = literals;

		int openVarsCount = 0;
		for (Integer lit : literals) {
			if (variables.get(Math.abs(lit)).getState() == Variable.State.OPEN) {
				openVarsCount++;
			}
		}

		switch (openVarsCount) {
			case 0:
				this.clauseState = ClauseState.EMPTY;
				break;
			case 1:
				this.clauseState = ClauseState.UNIT;
				this.lit1 = this.literals.get(0);
				this.lit2 = 0;
				variables.get(Math.abs(this.lit1)).watch(this);
				break;
			default:
				this.clauseState = initWatch(variables);
		}
	}

	public ClauseState getClauseState() {
		return this.clauseState;
	}

	public void setClauseState(ClauseState state) {
		this.clauseState = state;
	}

	public ClauseState initWatch(HashMap<Integer, Variable> variables) {
		if (this.clauseState != ClauseState.EMPTY && this.clauseState != ClauseState.UNIT) {
			lit1 = this.literals.get(0);
			variables.get(Math.abs(lit1)).watch(this);
			lit2 = this.literals.get(1);
			variables.get(Math.abs(lit2)).watch(this);
			return ClauseState.SUCCESS;
		} else {
			return this.clauseState;
		}
	}

	public ClauseState reWatch(HashMap<Integer, Variable> variables, int lit) {
		if (this.clauseState == ClauseState.SAT) return ClauseState.SAT;

		boolean litIsLit1 = lit == this.lit1;
		Variable.State varState;

		for (Integer literal : this.literals) {
			varState = variables.get(Math.abs(literal)).getState();
			if (literal != lit1 && literal != lit2 && (varState == Variable.State.OPEN || eval(varState, literal))) {
				if (litIsLit1) {
					variables.get(Math.abs(lit1)).unwatch(this);
					this.lit1 = literal;
					variables.get(Math.abs(lit1)).watch(this);
				} else {
					variables.get(Math.abs(lit2)).unwatch(this);
					this.lit2 = literal;
					variables.get(Math.abs(lit2)).watch(this);
				}
				return ClauseState.SUCCESS;
			}
		}
		// no new literal found to watch ToDo: maybe reset the current assigned to 0 to indicate that it is no more watched
		int otherLit = litIsLit1 ? this.lit2 : this.lit1;

		Variable.State otherLitVarState = variables.get(Math.abs(otherLit)).getState();
		if (otherLitVarState == Variable.State.OPEN) {
			return ClauseState.UNIT;
		} else if (eval(otherLitVarState, otherLit)) {
			return ClauseState.SAT;
		} else {
			return ClauseState.EMPTY;
		}
	}

	//Todo programmiert wie ein lappen um 24 uhr vllt noch verbessern
	public ClauseState reWatch_openlit(HashMap<Integer, Variable> variables, int varid) {
		int lit = 0;
		for (Integer literal : this.literals) {
			if (varid == Math.abs(literal)) {
				lit = literal;
			}
		}

		if (this.clauseState == ClauseState.EMPTY) {
			this.lit1 = lit;
			return ClauseState.UNIT;
		}

		for (Integer literal : this.literals) {
			if (eval(variables.get(Math.abs(literal)).getState(), lit)) return ClauseState.SAT;
		}

		if (variables.get(Math.abs(lit1)).getState() == Variable.State.OPEN) {
			if (variables.get(Math.abs(lit2)).getState() == Variable.State.OPEN) {
				return ClauseState.SUCCESS;
			} else {
				this.lit2 = lit;
				return ClauseState.SUCCESS;
			}
		} else {
			if (variables.get(Math.abs(lit2)).getState() == Variable.State.OPEN) {
				this.lit1 = lit;
				return ClauseState.SUCCESS;
			} else {
				return ClauseState.EMPTY; // Shouldnt ever happen
			}
		}
	}


	public boolean eval(Variable.State state, Integer literal) {
		return (state == Variable.State.TRUE && literal > 0) || (state == Variable.State.FALSE && literal < 0);
	}

	/**
	 * Returns the literals of this clause.
	 * 
	 * @return literals of this clause
	 */
	public Vector<Integer> getLiterals() {
		return literals;
	}

	/**
	 * Returns an unassigned literal of this clause.
	 * 
	 * @param variables
	 *            variable objects
	 * @return an unassigned literal, if one exists, 0 otherwise
	 */
	public int getUnassigned(HashMap<Integer, Variable> variables) {
		if (variables.get(Math.abs(lit1)).getState() == Variable.State.OPEN) {
			return lit1;
		} else if (variables.get(Math.abs(lit2)).getState() == Variable.State.OPEN) {
			return lit2;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the phase of the variable within this clause.
	 * 
	 * @param num
	 *            variable ID (>= 1)
	 * @return true, if variable is positive within this clause, otherwise false
	 */
	public boolean getPolarity(int num) {
		return this.literals.contains(num);
	}

	/**
	 * Returns the size of this clause.
	 * 
	 * @return size of this clause.
	 */
	public int size() {
		return literals.size();
	}

	@Override
	public String toString() {
		String res = "{ ";
		for (Integer i : literals)
			res += i + " ";
		return res + "}" + ", clause state = " + clauseState;
	}
}