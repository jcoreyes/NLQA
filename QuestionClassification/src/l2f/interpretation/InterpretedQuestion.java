package l2f.interpretation;

import l2f.interpretation.classification.QuestionCategory;

public class InterpretedQuestion implements Comparable<InterpretedQuestion> {

    private int id = 0;
    private static int numberInterpretedQuestions = 0;
    private final AnalyzedQuestion analyzedQuestion;
    private final QuestionCategory questionCategory;
    private QuestionCategory predictedQuestionCategory;

    public InterpretedQuestion(AnalyzedQuestion analyzedQuestion,
            QuestionCategory questionCategory) {
        numberInterpretedQuestions++;
        this.analyzedQuestion = analyzedQuestion;
        this.questionCategory = questionCategory;
        this.predictedQuestionCategory = QuestionCategory.VOID;
        this.id = numberInterpretedQuestions;

    }

    public int getId() {
        return id;
    }

    public AnalyzedQuestion getAnalyzedQuestion() {
        return analyzedQuestion;
    }

    public QuestionCategory getQuestionCategory() {
        return questionCategory;
    }

    public QuestionCategory getPredictedQuestionCategory() {
        return predictedQuestionCategory;
    }

    public void setPredictedQuestionCategory(QuestionCategory predictedQuestionCategory) {
        this.predictedQuestionCategory = predictedQuestionCategory;
    }

    
    

    public static void setNumberInterpretedQuestions(int numberInterpretedQuestions) {
        InterpretedQuestion.numberInterpretedQuestions = numberInterpretedQuestions;
    }

    

    public int compareTo(InterpretedQuestion iq) {
        if (iq.getId() > this.getId()) {
            return -1;
        }
        return 1;
    }
}
