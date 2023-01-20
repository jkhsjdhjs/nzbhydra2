package org.nzbhydra.mapping.newznab;

/**
 * {@link NewznabParameters} specific assertions - Generated by CustomAssertionGenerator.
 * <p>
 * Although this class is not final to allow Soft assertions proxy, if you wish to extend it,
 * extend {@link AbstractNewznabParametersAssert} instead.
 */
@jakarta.annotation.Generated(value = "assertj-assertions-generator")
public class NewznabParametersAssert extends AbstractNewznabParametersAssert<NewznabParametersAssert, NewznabParameters> {

    /**
     * Creates a new <code>{@link NewznabParametersAssert}</code> to make assertions on actual NewznabParameters.
     *
     * @param actual the NewznabParameters we want to make assertions on.
     */
    public NewznabParametersAssert(NewznabParameters actual) {
        super(actual, NewznabParametersAssert.class);
    }

    /**
     * An entry point for NewznabParametersAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myNewznabParameters)</code> and get specific assertion with code completion.
     *
     * @param actual the NewznabParameters we want to make assertions on.
     * @return a new <code>{@link NewznabParametersAssert}</code>
     */
    @org.assertj.core.util.CheckReturnValue
    public static NewznabParametersAssert assertThat(NewznabParameters actual) {
        return new NewznabParametersAssert(actual);
    }
}