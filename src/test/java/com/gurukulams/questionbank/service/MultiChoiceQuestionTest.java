package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;
import static com.gurukulams.questionbank.service.QuestionService.OWNER_USER;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class MultiChoiceQuestionTest extends QuestionServiceTest {

    @Test
    void testInvalidQuestionCreation() {
        Question question = crateQuestion();

        question.getChoices().forEach(questionChoice -> questionChoice.setIsAnswer(false));

        // No Answer
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.CHOOSE_THE_BEST,
                        null,
                        OWNER_USER,
                        question)
                );

        question.getChoices().remove(0);
        question.getChoices().remove(0);
        question.getChoices().remove(0);

        // No Answer
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.CHOOSE_THE_BEST,
                        null,
                        OWNER_USER,
                        question)
        );

        question.setChoices(null);
        // No Answer
        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.create(List.of("c1",
                                "c2"),
                        null,
                        QuestionType.SINGLE_LINE,
                        null,
                        OWNER_USER,
                        question)
        );
    }

    @Test
    void testInvalidQuestionUpdate() throws SQLException {
        Question crateQuestion = crateQuestion();

        crateQuestion.getChoices().get(0).setIsAnswer(true);

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                OWNER_USER,
                crateQuestion);

        question.get().setChoices(null);

        Assertions.assertThrows(ConstraintViolationException.class, () ->
                questionService.update(question.get().getType(), question.get().getId(), null, question.get())
        );


    }

    @Override
    void testUpdate(final Question questionToUpdate,final Locale locale) throws SQLException {
        questionToUpdate.setQuestion("Updated");

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals("Updated",
                this.questionService.read(questionToUpdate.getId(),locale)
                        .get().getQuestion());

        QuestionChoice questionChoice = questionToUpdate.getChoices().get(0);

        questionChoice.setCValue("Updated");

        this.questionService.update(questionToUpdate.getType(),
                questionToUpdate.getId(),locale, questionToUpdate);

        Assertions.assertEquals("Updated",
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
    Question testCreate(Locale locale) throws SQLException {
        Question crateQuestion = crateQuestion();

        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.MULTI_CHOICE,
                locale,
                OWNER_USER,
                crateQuestion);

        String rightAnswer = question.get().getChoices().stream()
                .filter(QuestionChoice::getIsAnswer)
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));

        // Right Answer
        Assertions.assertTrue(answerService.answer(question.get().getId(),
                rightAnswer));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                rightAnswer+ "," + question.get().getChoices().stream()
                        .filter(choice -> !choice.getIsAnswer())
                        .findFirst().get().getId()));

        // Wrong Answer
        Assertions.assertFalse(answerService.answer(question.get().getId(),
                question.get().getChoices().stream()
                        .filter(choice -> !choice.getIsAnswer())
                        .findFirst().get().getId().toString()));

        return question.get();


    }

    @Override
    Question crateQuestion() {
        Question question = new Question();
        question.setQuestion("Choose 1");
        question.setExplanation("A Choose the best question");
        question.setChoices(new ArrayList<>());

        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("1");
        choice.setIsAnswer(true);
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("2");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("3");
        choice.setIsAnswer(true);
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("4");
        question.getChoices().add(choice);

        return question;
    }
}