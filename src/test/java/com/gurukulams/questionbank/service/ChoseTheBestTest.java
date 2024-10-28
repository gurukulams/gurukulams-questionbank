package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;

import org.junit.jupiter.api.Assertions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

class ChoseTheBestTest extends QuestionServiceTest {

    protected static final String C_LANGUAGE = "C";

    @Override
    void testUpdate(final Question questionToUpdate,final Locale locale) throws SQLException {

        final String updatedQuestionTxt = "Updated at " + System.currentTimeMillis();

        questionToUpdate.setQuestion(updatedQuestionTxt);

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals(updatedQuestionTxt,
                this.questionService.read(questionToUpdate.getId(),locale)
                        .get().getQuestion());

        QuestionChoice questionChoice = questionToUpdate.getChoices().get(0);

        UUID cId = questionChoice.id();
        questionToUpdate.getChoices().set(0,questionChoice.withCValue(updatedQuestionTxt));

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals(updatedQuestionTxt,
                this.questionService.read(questionToUpdate.getId(),locale).get()
                    .getChoices().stream()
                    .filter(questionChoice1 -> questionChoice1.id().equals(cId))
                    .findFirst().get().cValue());

        int existingQuestions = questionToUpdate.getChoices().size();

        String cValue = UUID.randomUUID().toString();
        QuestionChoice choice = new QuestionChoice(null,null,cValue,Boolean.FALSE);

        questionToUpdate.getChoices().add(choice);

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        QuestionChoice choiceReturned = this.questionService.read(questionToUpdate.getId(),locale).get()
                .getChoices().stream()
                .filter(questionChoice1 -> questionChoice1.cValue().equals(cValue))
                        .findFirst().get();

        Assertions.assertTrue(
                choiceReturned.cValue().equals(cValue));

        questionToUpdate.setChoices(this.questionService.read(questionToUpdate.getId(),locale)
                .get()
                .getChoices().stream()
                .filter(questionChoice1 -> !questionChoice1.cValue().equals(cValue)).toList());

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals(existingQuestions,
                this.questionService.read(questionToUpdate.getId(),locale).get()
                        .getChoices().size());


    }

    @Override
    String getCorrectAnswer(Question question) {
        return question.getChoices().stream()
                .filter(QuestionChoice::isAnswer)
                .findFirst()
                .get()
                .id().toString();
    }

    @Override
    List<String> getWrongAnswers(Question question) {
        List<String> answers = new ArrayList<>();
        answers.add(UUID.randomUUID().toString());
        return answers;
    }

    @Override
    Question getTestQuestion() {
        Question question = new Question();
        question.setType(QuestionType.CHOOSE_THE_BEST);

        question.setQuestion("Which one of the folloing is a Object Oriented Language?");
        question.setExplanation("Language that suppors class and objects");

        question.setChoices(new ArrayList<>());

        QuestionChoice choice = new QuestionChoice(null, null, "Java", true);
        question.getChoices().add(choice);

        choice = new QuestionChoice(null, null,C_LANGUAGE, false);
        question.getChoices().add(choice);

        choice = new QuestionChoice(null, null,"Tamil", false);
        question.getChoices().add(choice);

        choice = new QuestionChoice(null, null,"English", false);
        question.getChoices().add(choice);

        return question;
    }

    @Override
    List<Question> getInvalidQuestions() {

        List<Question> invalidQuestions  = new ArrayList<>();

        Question question = getTestQuestion();

        // Question without Answer
        for (int i = 0; i < question.getChoices().size(); i++) {
            question.getChoices().set(i, question.getChoices().get(i).withIsAnswer(false));
        }


        invalidQuestions.add(question);


        question = getTestQuestion();
        question.getChoices().remove(0);
        question.getChoices().remove(0);
        question.getChoices().remove(0);

        // Question with Only One Choice
        invalidQuestions.add(question);

        question = getTestQuestion();


        question.setChoices(null);

        // Question with Null Choice
        invalidQuestions.add(question);

        return invalidQuestions;
    }
}