package io.fintechlabs.testframework.sequence;

import java.util.List;

import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.testmodule.TestExecutionUnit;

public interface ConditionSequence extends TestExecutionUnit {

	void evaluate();

	List<TestExecutionUnit> getTestExecutionUnits();

	ConditionSequence replace(Class<? extends Condition> conditionToReplace, TestExecutionUnit builder);

	ConditionSequence butFirst(TestExecutionUnit... builders);

	ConditionSequence then(TestExecutionUnit... builders);

	ConditionSequence insertAfter(Class<? extends Condition> conditionToInsertAt, TestExecutionUnit builder);

	ConditionSequence insertBefore(Class<? extends Condition> conditionToInsertAt, TestExecutionUnit builder);

}
