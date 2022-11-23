import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

class TestIntentions : DescribeSpec({
    val X = Var.of("X")
    val buySomething = Belief.of(Struct.of("buy", X))
    val eatSomething = Belief.of(Struct.of("eat", X))

    val activationRecord = ActivationRecord.of(listOf(AddBelief(buySomething), AddBelief(eatSomething)))
    val intention = Intention.of(recordStack = listOf(activationRecord))

    describe("An intention") {
        it("should return the next goal to satisfy with nextGoal() invocation") {
            val nextGoal = intention.nextGoal()
            nextGoal.shouldBeTypeOf<AddBelief>()
            nextGoal.belief shouldBe buySomething.head
        }

        it("should remove the right goal with pop() invocation") {
            val updatedIntention = intention.pop()
            updatedIntention.recordStack.size shouldBe 1
            updatedIntention.nextGoal().value shouldBe eatSomething.head
            updatedIntention.pop().recordStack shouldBe emptyList()
        }

        it("should add on top of the record stack after a push() invocation") {
            val newActivationRecord = ActivationRecord.of(listOf(Achieve(Atom.of("clean"))))
            val updatedIntention = intention.push(newActivationRecord)
            updatedIntention.nextGoal() shouldBe Achieve(Atom.of("clean"))
        }

        it("should apply a substitution on the actual Activation Record") {
            // val bb = BeliefBase.of(listOf(Belief.of(Struct.of(buy()))))

            val substitution = Substitution.of(X, Atom.of("chocolate"))
            val newIntention = Intention.of(
                intention.recordStack +
                    ActivationRecord.of(listOf(Achieve(Struct.of("clean", X))))
            )
            newIntention.recordStack.size shouldBe 2
            val computedIntention = newIntention.applySubstitution(substitution)
            computedIntention.recordStack.size shouldBe 2
            computedIntention.recordStack.first().goalQueue.forEach {
                it.value.args.first() shouldBe Atom.of("chocolate")
            }
            computedIntention.recordStack.last().goalQueue.forEach {
                it.value.args.first() shouldBe X
            }
        }
    }
})