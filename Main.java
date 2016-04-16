import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		ArrayList<Evaluatable> numbers = new ArrayList<Evaluatable>();
		int target = Integer.parseInt(args[0]);

		for (int i = 1; i < args.length; ++i) {
			System.out.print(args[i] + " ");
			numbers.add(new Number(Integer.parseInt(args[i])));
		}

		TheSolver solver = new TheSolver(numbers);
		Evaluatable result = solver.SolveForTarget(target);

		System.out.println("--> " + (result == null ? "none" : result) + " = " + target);
	}
}

class TheSolver {
	public TheSolver(ArrayList<Evaluatable> numbers) {
		this.numbers = numbers;
	}

	public Evaluatable SolveForTarget(int target) {
		return _RecursiveSearch(this.numbers, target);
	}

	private boolean _IsEqual(double value, int target) {
		return value - target < 1e-6 && target - value < 1e-6;
	}

	private Evaluatable _RecursiveSearch(ArrayList<Evaluatable> elist, int target) {
		if (elist.size() == 1) {
			if (_IsEqual(elist.get(0).Evaluate(), target)) return elist.get(0);
			else return null;
		} else {
			for (int op: _usableOperators) {
				for (int i = 0; i < elist.size(); ++i) {
					for (int j = 0; j < elist.size(); ++j) {
						if (i == j) continue;
						if ((op == Operator.PLUS || op == Operator.MULTIPLY) && i > j) continue;
						Evaluatable result = _RecursiveSearch(_BuildUp(elist, i, j, op), target);
						if (result != null) return result;
					}
				}
			}
		}
		return null;
	}

	private ArrayList<Evaluatable> _BuildUp(ArrayList<Evaluatable> from, int lIndex, int rIndex, int op) {
		ArrayList<Evaluatable> result = new ArrayList<Evaluatable>();
		result.add(new Operator(from.get(lIndex), from.get(rIndex), op));

		for (int i = 0; i < from.size(); ++i) {
			if (i == lIndex || i == rIndex) continue;
			result.add(from.get(i));
		}

		return result;
	}

	private ArrayList<Evaluatable> numbers;
	static private ArrayList<Integer> _usableOperators;
	static {
		_usableOperators = new ArrayList<Integer>();
		_usableOperators.add(Operator.PLUS);
		_usableOperators.add(Operator.MINUS);
		_usableOperators.add(Operator.DIVIDE);
		_usableOperators.add(Operator.MULTIPLY);
	}
}

interface Evaluatable {
	double Evaluate();
}

class Operator implements Evaluatable {
	public static final int PLUS = 0;
	public static final int MINUS = 1;
	public static final int DIVIDE = 2;
	public static final int MULTIPLY = 3;

	public Operator(Evaluatable lhs, Evaluatable rhs, int op) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.op = op;
	}

	public double Evaluate() {
		switch (this.op) {
			case PLUS:
				return lhs.Evaluate() + rhs.Evaluate();
			case MINUS:
				return lhs.Evaluate() - rhs.Evaluate();
			case DIVIDE:
				return lhs.Evaluate() / rhs.Evaluate();
			case MULTIPLY:
				return lhs.Evaluate() * rhs.Evaluate();
			default:
				throw new AssertionError("Invalid operator");
		}
	}

	public String toString() {
		String opString = "";
		switch (this.op) {
			case PLUS:
				opString = "+";
				break;
			case MINUS:
				opString = "-";
				break;
			case DIVIDE:
				opString = "/";
				break;
			case MULTIPLY:
				opString = "*";
				break;
			default:
				throw new AssertionError("Invalid operator");
		}

		boolean needsLParen = false;
		boolean needsRParen = false;

		if (this.lhs != null && this.lhs instanceof Operator) {
			if ((this.op == MULTIPLY || this.op == DIVIDE) &&
					((Operator) this.lhs).op == PLUS || ((Operator) this.lhs).op == MINUS) {
				needsLParen = true;
			}
		}

		if (this.rhs != null && this.rhs instanceof Operator) {
			if (this.op == DIVIDE) {
				needsRParen = true;
			} else if (this.op == MULTIPLY && (((Operator) this.rhs).op == PLUS || ((Operator) this.rhs).op == MINUS)) {
				needsRParen = true;
			} else if ((this.op == MINUS) && (((Operator) this.rhs).op == PLUS || ((Operator) this.rhs).op == MINUS)) {
				needsRParen = true;
			}
		}

		return (needsLParen ? "(" : "" ) + this.lhs.toString() + (needsLParen ? ")" : "") +
			" " + opString + " " + (needsRParen ? "(" : "") + this.rhs.toString() + (needsRParen ? ")" : "");
	}

	private boolean _NeedsParenthesis(Evaluatable e) {
		return( this.op == MULTIPLY || this.op == DIVIDE) && e != null &&
			e instanceof Operator && (((Operator) e).op == PLUS || ((Operator) e).op == MINUS);
	}

	private Evaluatable lhs;
	private Evaluatable rhs;
	private int op;
}

class Number implements Evaluatable {

	public Number(int value) {
		this.value = value;
	}

	public double Evaluate() {
		return this.value;
	}

	public void SetValue(int value) {
		this.value = value;
	}

	public String toString() {
		return Integer.toString(this.value);
	}

	private int value;
}

// vim: set ts=4 sw=4:
