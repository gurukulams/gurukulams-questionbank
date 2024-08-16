package com.gurukulams.questionbank.service;

import com.gurukulams.questionbank.model.QuestionChoice;
import com.gurukulams.questionbank.payload.Question;
import com.gurukulams.questionbank.payload.QuestionType;
import com.gurukulams.questionbank.util.TestUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    void testCreateWtihExtraMatch() throws SQLException {
        Question question = newMTFWithExtraMatch();
        Optional<Question> optionalQuestion = this.questionService.create(
                List.of("c1", "c2"),
                null,
                QuestionType.MATCH_THE_FOLLOWING,
                null,
                OWNER_USER,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.assertEquals(optionalQuestion.get().getMatches().size(), optionalQuestion.get().getChoices().size()+1);
        int i=0;
        for(QuestionChoice match:optionalQuestion.get().getMatches()) {
            if ("Object Oriented".equals(match.getCValue())) {
                Assertions.assertEquals("Java", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("Relational Database".equals(match.getCValue())) {
                Assertions.assertEquals("Postgres", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("Document Database".equals(match.getCValue())) {
                Assertions.assertEquals("MongoDB", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("System Language".equals(match.getCValue())) {
                Assertions.assertEquals("C", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("Extra Match".equals(match.getCValue())) {
                Assertions.assertTrue(true); //no choice will be there
            } else {
                Assertions.fail();
            }
            i++;
        }
    }

    @Test
    void testCreateWtihExactMatch() throws SQLException {
        Question question = getQuestionExactMatches();
        Optional<Question> optionalQuestion = this.questionService.create(
                List.of("Computer Science", "Programming"),
                null,
                QuestionType.MATCH_THE_FOLLOWING,
                null,
                OWNER_USER,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.assertEquals(optionalQuestion.get().getMatches().size(), optionalQuestion.get().getChoices().size());

        int i=0;
        for(QuestionChoice match:optionalQuestion.get().getMatches()) {
            if ("Object Oriented".equals(match.getCValue())) {
                Assertions.assertEquals("Java", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("Relational Database".equals(match.getCValue())) {
                Assertions.assertEquals("Postgres", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("Document Database".equals(match.getCValue())) {
                Assertions.assertEquals("MongoDB", optionalQuestion.get().getChoices().get(i).getCValue());
            } else if ("System Language".equals(match.getCValue())) {
                Assertions.assertEquals("C", optionalQuestion.get().getChoices().get(i).getCValue());
            } else {
                Assertions.fail();
            }
            i++;
        }
    }

    @Test
    void testCreateWtihEmptyMatch() throws SQLException {
        Question question = getQuestionExactMatches();
        question.setMatches(new ArrayList<>());
        try {

            Optional<Question> optionalQuestion = this.questionService.create(
                    List.of("Computer Science", "Programming"),
                    null,
                    QuestionType.MATCH_THE_FOLLOWING,
                    null,
                    OWNER_USER,
                    question
            );
            Assertions.fail();
        } catch (ConstraintViolationException violationException) {
            Assertions.assertTrue(violationException.getMessage().contains("No matches are provided"));
        }
    }

    @Test
    void testCreateWtihEmptyChoice() throws SQLException {
        Question question = getQuestionExactMatches();
        question.setChoices(new ArrayList<>());
        try {

            Optional<Question> optionalQuestion = this.questionService.create(
                List.of("Computer Science", "Programming"),
                null,
                QuestionType.MATCH_THE_FOLLOWING,
                null,
                OWNER_USER,
                question
            );
            Assertions.fail();
        } catch (ConstraintViolationException violationException) {
            Assertions.assertTrue(violationException.getMessage().contains("No choices are provided"));
        }
    }

    @Test
    void testCreateWtihExtraChoices() throws SQLException {
        Question question = getQuestionExactMatches();
        question.getMatches().remove(2);
        try {

            Optional<Question> optionalQuestion = this.questionService.create(
                    List.of("Computer Science", "Programming"),
                    null,
                    QuestionType.MATCH_THE_FOLLOWING,
                    null,
                    OWNER_USER,
                    question
            );
            Assertions.fail();
        } catch (ConstraintViolationException violationException) {
            Assertions.assertTrue(violationException.getMessage().contains("Not Enough Matches"));
        }
    }

    @Test
    void testUpdateWtihExactMatch() throws SQLException {
        Question question = getQuestionExactMatches();
        Optional<Question> createdQuestion = this.questionService.create(
                List.of("Computer Science", "Programming"),
                null,
                QuestionType.MATCH_THE_FOLLOWING,
                null,
                OWNER_USER,
                question);
        question.setExplanation("Check the updated match the following");
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                createdQuestion.get().getId(),
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals("Check the updated match the following", optionalQuestion.get().getExplanation());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
    }

    @Test
    @Disabled
    void testUpdateWtihExtraMatch() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateWtihEmptyMatch() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateWtihEmptyChoices() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateWtihEmptyMatchAndEmptyChoices() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdatequestion() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateExplanation() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateAddExtraMatches() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateDeleteMatches() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testUpdateAddMatchesAndChoices() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }
    @Test
    @Disabled
    void testUpdateDeleteMatchesChoices() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testReadQuestionWithValidQuestionId() throws SQLException {

        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testReadQuestionWithInValidQuestionId() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testListQuestionByType() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testListQuestionByQuestion() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testDeleteQuestionById() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    @Test
    @Disabled
    void testDeleteQuestionByType() throws SQLException {
        Question question = newMTFWithExtraMatch();
        UUID questionId = new UUID(1234567890,6542310);
        Optional<Question> optionalQuestion = this.questionService.update(
                QuestionType.MATCH_THE_FOLLOWING,
                questionId,
                null,
                question
        );
        Assertions.assertTrue(optionalQuestion.isPresent());
        Assertions.assertEquals(question.getChoices().size(), optionalQuestion.get().getChoices().size());
        Assertions.assertEquals(question.getMatches().size(), optionalQuestion.get().getMatches().size());
        Assertions.fail();
    }

    Question newMTFWithExtraMatch() {
        Question question = getQuestionExactMatches();
        QuestionChoice choice = new QuestionChoice();
        choice.setCValue("Extra Match");
        question.getMatches().add(choice);
        return question;
    }

    private static Question getQuestionExactMatches() {
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