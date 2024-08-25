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
import java.util.stream.Collectors;

class MatchTheFollowingTest extends ChoseTheBestTest {

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

        questionChoice.setCValue(updatedQuestionTxt);

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals(updatedQuestionTxt,
                this.questionService.read(questionToUpdate.getId(),locale).get()
                        .getChoices().stream()
                        .filter(questionChoice1 -> questionChoice1.getId().equals(questionChoice.getId()))
                        .findFirst().get().getCValue());

        int existingQuestions = questionToUpdate.getChoices().size();

        String cValue = UUID.randomUUID().toString();
        QuestionChoice choice = new QuestionChoice();
        choice.setIsAnswer(Boolean.FALSE);
        choice.setCValue(cValue);
        questionToUpdate.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setIsAnswer(Boolean.FALSE);
        choice.setCValue(cValue);
        questionToUpdate.getMatches().add(choice);

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        QuestionChoice choiceReturned = this.questionService.read(questionToUpdate.getId(),locale).get()
                .getChoices().stream()
                .filter(questionChoice1 -> questionChoice1.getCValue().equals(cValue))
                .findFirst().get();

        Assertions.assertTrue(
                choiceReturned.getCValue().equals(cValue));

        questionToUpdate.setChoices(this.questionService.read(questionToUpdate.getId(),locale)
                .get()
                .getChoices().stream()
                .filter(questionChoice1 -> !questionChoice1.getCValue().equals(cValue)).toList());

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals(existingQuestions,
                this.questionService.read(questionToUpdate.getId(),locale).get()
                        .getChoices().size());


    }

    @Override
    String getCorrectAnswer(Question question) {
        List<QuestionChoice> questionChoices = new ArrayList<>();

        questionChoices
                .addAll(question.getChoices());

        questionChoices
                .addAll(question.getMatches().subList(0, question.getChoices().size()));

        return questionChoices.stream()
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));
    }

    @Override
    List<Question> getInvalidQuestions() {

        List<Question> invalidQuestions = new ArrayList<>();

        Question question = getTestQuestion();
        //null matches
        question.setMatches(null);
        invalidQuestions.add(question);
        return invalidQuestions;
    }

    @Override
    Question getTestQuestion() {
        Question question = super.getTestQuestion();
        question.setType(QuestionType.MATCH_THE_FOLLOWING);

        question.setQuestion("Match the Following");

        question.getChoices()
                .forEach(questionChoice ->
                        questionChoice.setIsAnswer(false));

        question.setMatches(getMatches());

        return question;
    }

    private List<QuestionChoice> getMatches() {
        List<QuestionChoice> matches = new ArrayList<>();

        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("Object Oriented");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("System Language");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Regional Language");
        matches.add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Universal Language");
        matches.add(choice);

        return matches;
    }


}