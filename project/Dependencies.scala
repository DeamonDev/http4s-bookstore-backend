same[A](expected: A, found: A)(implicit eqA: Eq[A] = Eq.fromUniversalEquals[A], showA: Show[A] = Show.fromToString[A], loc: SourceLocation): Expectations

Same as eql but defaults to universal equality.
