package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;
import com.gurukulams.questionbank.util.TestUtil;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.gurukulams.questionbank.service.QuestionService.OWNER_USER;

abstract class QuestionServiceTest {

    protected final QuestionService questionService;


    protected final AnswerService answerService;

    QuestionServiceTest() {
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


    /**
     * Creates a VALID question.
     * @return
     */
    abstract Question crateQuestion() ;


    @Test
    void testDelete() throws SQLException {
        Question crateQuestion = crateQuestion();


        // Create a Question
        Optional<Question> question = questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                OWNER_USER,
                crateQuestion);


        questionService.delete(question.get().getId(), QuestionType.CHOOSE_THE_BEST);

        Assertions.assertTrue(questionService.read(question.get().getId(), null).isEmpty());

    }

    @Test
    void testList() throws SQLException {
        Question crateQuestion = crateQuestion();



        // Create a Question
        questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.CHOOSE_THE_BEST,
                null,
                OWNER_USER,
                crateQuestion);



        questionService.create(List.of("c1",
                        "c2"),
                null,
                QuestionType.MULTI_CHOICE,
                Locale.FRENCH,
                OWNER_USER,
                crateQuestion);

        Assertions.assertEquals(2,
                questionService.list(OWNER_USER, null, List.of("c1",
                        "c2")).size());

        Assertions.assertEquals(2,
                questionService.list(OWNER_USER, Locale.FRENCH, List.of("c1",
                        "c2")).size());

        Assertions.assertEquals(2,
                questionService.list("NEW_USER", null, List.of("c1",
                        "c2")).size());

        Assertions.assertEquals(2,
                questionService.list("NEW_USER", Locale.FRENCH, List.of("c1",
                        "c2")).size());

    }

}