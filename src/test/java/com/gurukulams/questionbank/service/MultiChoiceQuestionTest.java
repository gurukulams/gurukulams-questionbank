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
    String getCorrectAnswer(Question question) throws SQLException {
        return question.getChoices().stream()
                .filter(QuestionChoice::getIsAnswer)
                .map(choice -> choice.getId().toString())
                .collect(Collectors.joining(","));
    }

    @Override
    String getWrongAnswer(Question question) throws SQLException {
        return UUID.randomUUID().toString();
    }

    @Override
    Question getTestQuestion() {
        Question question = new Question();
        question.setType(QuestionType.MULTI_CHOICE);
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

    @Override
    List<Question> getInvalidQuestions() {

        List<Question> invalidQuestions  = new ArrayList<>();

        Question question = getTestQuestion();

        // Question without Answer
        question.getChoices().forEach(questionChoice -> questionChoice.setIsAnswer(false));

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