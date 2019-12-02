package dataStructure;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

/**
 * A variable.
 * 
 */
public class Variable {

	/* Assignment states of a variable */
	public enum State {
		TRUE, FALSE, OPEN
	};

	/* Current assignment */
	private State state;

	public int getId() {
		return id;
	}

	/* Variable ID (range from 1 to n) */
	private int id;

	private Vector<Clause> watched;

	private float activity;

	// Grund der Belegung (bei Entscheidung ist der Grund null)
	private Clause reason;

	// Entscheidungslevel
	private int level;

	/**
	 * Creates a variable with the given ID.
	 * 
	 * @param id
	 *            ID of the variable
	 */
	public Variable(int id) {
		this.id = id;
		this.state = State.OPEN;
		this.watched = new Vector<Clause>();
		this.activity = 1;
	}

	public void incCount() {
		this.activity++;
	}

	public float getActivity() {
		return this.activity;
	}

	public void incActivity() {
		this.activity *= 1.1;
	}

	public void decActivity() {
		this.activity *= 0.9;
	}

	public Clause getReason() {
		return this.reason;
	}

	public int getLevel() {
		return this.level;
	}

	/**
	 * Returns the current assignment state of this variable.
	 * 
	 * @return current assignment state
	 */
	public State getState() {
		return state;
	}

	public void watch(Clause clause) {
		if (!this.watched.contains(clause)) this.watched.add(clause);
	}

	public void unwatch(Clause clause) {
		this.watched.remove(clause);
	}

	/**
	 * Assigns variable with the given value and updates the internal state of
	 * the corresponding clauses.
	 * 
	 * @param val
	 *            value to be assigned
	 */
	public Clause assign(boolean val, HashMap<Integer, Variable> variables, Vector<Clause> units, Stack<Variable> stack, Clause reason, int level) {
		stack.push(this);
		this.reason = reason;
		this.level = level;

		if (val) {
			this.state = State.TRUE;
		} else {
			this.state = State.FALSE;
		}

		for (int c = this.watched.size() - 1; c >= 0; c--) {
			Clause clause = this.watched.get(c);
			for (int l = 0; l < clause.getLiterals().size(); l++) {
				Vector<Clause> watchedCopy = new Vector<Clause>(this.watched);
				Integer lit = clause.getLiterals().get(l);
				if (Math.abs(lit) == this.id) {
					if ((val && lit < 0) || (!val && lit > 0)) {
						Clause.ClauseState clauseState = watchedCopy.get(c).reWatch(variables, lit);
						switch (clauseState) {
							case EMPTY:
//								variables.get(this.id).watched = watchedCopy;
//								this.watched = watchedCopy;
								return clause;
							case UNIT:
								units.add(clause);
						}
					} else {
						watchedCopy.get(c).setClauseState(Clause.ClauseState.SAT);
					}
				}
			}
		}
		return null;
	}

	// ToDo: update Clause States!
	public void unassign() {
		this.state = State.OPEN;
	}

	@Override
	public String toString() {
		String res = "[" + state + " ";
		res += "\n\twatched List: " + watched;
		return res + "\n]";
	}
}