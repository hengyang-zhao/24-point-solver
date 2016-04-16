import java.util.ArrayList;
import java.math.BigInteger;

public class Main {
	public static void main(String[] args) {
		ArrayList<Evaluatable> numbers = new ArrayList<Evaluatable>();
		Rational target = new Rational(args[0]);

		for (int i = 1; i < args.length; ++i)
			numbers.add(new Number(args[i]));

		TheSolver solver = new TheSolver(numbers);
		Evaluatable result = solver.SolveForTarget(target);

		System.out.println(result == null ? "none" : result);
	}
}

class TheSolver {
	public TheSolver(ArrayList<Evaluatable> numbers) {
		this.numbers = numbers;
	}

	public Evaluatable SolveForTarget(Rational target) {
		return _RecursiveSearch(this.numbers, target);
	}

	private Evaluatable _RecursiveSearch(ArrayList<Evaluatable> elist, Rational target) {
		if (elist.size() == 1) {
			if (elist.get(0).Evaluate().EqualsTo(target)) return elist.get(0);
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
	Rational Evaluate();
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

	public Rational Evaluate() {
		switch (this.op) {
			case PLUS:
				return lhs.Evaluate().Add(rhs.Evaluate());
			case MINUS:
				return lhs.Evaluate().Subtract(rhs.Evaluate());
			case DIVIDE:
				return lhs.Evaluate().Divide(rhs.Evaluate());
			case MULTIPLY:
				return lhs.Evaluate().Multiply(rhs.Evaluate());
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

	public Number(String value) {
		this.value = new Rational(value);
	}

	public Rational Evaluate() {
		return this.value;
	}

	public String toString() {
		return this.value.toString();
	}

	private Rational value;
}

class Rational {

	public Rational(String numer) {
		this.numer = new BigInteger(numer);
		this.denom = BigInteger.ONE;
	}

	public Rational(String numer, String denom) {
		this.numer = new BigInteger(numer);
		this.denom = new BigInteger(denom);

		_Simplify();
	}

	public Rational(BigInteger numer, BigInteger denom) {
		this.numer = numer;
		this.denom = denom;

		_Simplify();
	}

	public Rational Add(Rational that) {
		Rational ret = new Rational(
				this.numer.multiply(that.denom).add(this.denom.multiply(that.numer)),
				this.denom.multiply(that.denom));
		ret._Simplify();
		return ret;
	}

	public Rational Subtract(Rational that) {
		Rational ret = new Rational(
				this.numer.multiply(that.denom).subtract(this.denom.multiply(that.numer)),
				this.denom.multiply(that.denom));
		ret._Simplify();
		return ret;
	}

	public Rational Multiply(Rational that) {
		Rational ret = new Rational(
				this.numer.multiply(that.numer),
				this.denom.multiply(that.denom));
		ret._Simplify();
		return ret;
	}

	public Rational Divide(Rational that) {
		Rational ret = new Rational(
				this.numer.multiply(that.denom),
				this.denom.multiply(that.numer));
		ret._Simplify();
		return ret;
	}

	public String toString() {
		if (this.denom.equals(BigInteger.ONE)) {
			return this.numer.toString();
		} else {
			return this.numer.toString() + "/" + this.denom.toString();
		}
	}

	public boolean EqualsTo(Object o) {
		if (o instanceof Rational) {
			Rational r = (Rational) o;
			return this.numer.equals(r.numer) && this.denom.equals(r.denom);
		} else {
			return false;
		}
	}

	private void _Simplify() {
		BigInteger gcd = this.denom.gcd(this.numer);

		if (gcd.equals(BigInteger.ZERO)) return;

		if (this.denom.compareTo(BigInteger.ZERO) < 0) {
			this.denom = this.denom.divide(gcd.negate());
			this.numer = this.numer.divide(gcd.negate());
		} else {
			this.denom = this.denom.divide(gcd);
			this.numer = this.numer.divide(gcd);
		}
	}

	private BigInteger numer;
	private BigInteger denom;
}

// vim: set ts=4 sw=4:
