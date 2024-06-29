package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;
import com.gurukulams.questionbank.util.TestUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.gurukulams.questionbank.service.QuestionService.OWNER_USER;


class MTFQuestionServiceTest {


    private final QuestionService questionService;


    private final AnswerService answerService;

    MTFQuestionServiceTest() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        this.questionService = new QuestionService(
                validator,
                TestUtil.questionBankManager());
        this.answerService = new AnswerService(this.questionService);
    }


    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException, SQLException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() throws SQLException {
        cleanUp();
    }

    private void cleanUp() throws SQLException {
        questionService.delete();
    }

    @Test
    void testCreate() throws SQLException {
        Question question = newMTF();
        Optional<Question> optionalQuestion = this.questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.MATCH_THE_FOLLOWING,
                null,
                OWNER_USER,
                question);

        Assertions.assertTrue(optionalQuestion.isPresent());
    }


    Question newMTF() {
        Question question = new Question();
        question.setQuestion("Match the Following");
        question.setExplanation("A Match the Following question");
        question.setChoices(new ArrayList<>());

        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("Java");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Postgres");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("MongoDB");
        question.getChoices().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("C");
        question.getChoices().add(choice);

        question.getChoices().add(null);


        question.setMatches(new ArrayList<>());

        choice = new QuestionChoice();
        choice.setCValue("Object Oriented");
        question.getMatches().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Relational Database");
        question.getMatches().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("Document Database");
        question.getMatches().add(choice);

        choice = new QuestionChoice();
        choice.setCValue("System Language");
        question.getMatches().add(choice);

        return question;
    }
}